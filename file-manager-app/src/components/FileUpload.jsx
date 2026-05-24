import React, { useState, useRef } from 'react';
import { Box, Button, Typography, CircularProgress, TextField, InputAdornment, IconButton } from '@mui/material';
import Visibility from '@mui/icons-material/Visibility';
import VisibilityOff from '@mui/icons-material/VisibilityOff';
import { encryptFile, hashBuffer } from '../utils/crypto';
import { saveEncryptedFile, saveFileMeta, getFileMeta, listAllFileMetas } from '../utils/idb';
import { useAppSwal } from '../hooks/useAppSwal';

const API_BASE_URL = import.meta.env.VITE_API_URL;

export const FileUpload = ({ onUploadSuccess, currentPath = [] }) => {
  const [file, setFile] = useState(null);
  const fileInputRef = useRef(null);
  const [uploading, setUploading] = useState(false);
  const [customName, setCustomName] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const { swal, toast } = useAppSwal();

  const handleFileChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      setFile(e.target.files[0]);
      // prefill suggested name
      setCustomName('');
    }
  };

  const handleUpload = async () => {
    if (!file) return;
    setUploading(true);

    const finalName = customName.trim() || file.name;
    // If a logical file with same name exists in currentPath, reuse its fileId
    let fileId = null;
    try {
      const metas = await listAllFileMetas().catch(() => []);
      const match = metas.find(m => {
        const meta = m.value || {};
        const nameMatch = (meta.name === finalName);
        const pathMatch = JSON.stringify(meta.path || []) === JSON.stringify(currentPath || []);
        return nameMatch && pathMatch;
      });
      fileId = match ? match.id : Date.now().toString();
    } catch {
      fileId = Date.now().toString();
    }

    try {
      // 1. Send metadata to backend to request a signed URL
      const token = localStorage.getItem('jwt_token');

      const fileBuffer = await file.arrayBuffer();
      const fileHash = await hashBuffer(fileBuffer);

      const requestResponse = await fetch(`${API_BASE_URL}/api/v1/upload`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
        },
        body: JSON.stringify({
          location: '/',
          name: finalName,
          type: 'FILE',
          contentType: file.type,
          size: file.size,
          hash: fileHash
        })
      });

      if (!requestResponse.ok) {
        swal.fire({
          title: 'Failed to get signed URL',
          text: 'The server rejected the upload request.',
          icon: 'error',
        })
        throw new Error('Failed to get signed URL');
      }
      const { uploadUrl, fileId } = await requestResponse.json();

      // If password provided, encrypt file locally before uploading
      let uploadBlob = file;
      let cryptoMeta = null;
      if (password) {
        const { ciphertext, ivBase64, saltBase64 } = await encryptFile(file, password);
        // compute hash over ciphertext to detect versions
        const hash = await hashBuffer(ciphertext);
        const versionId = Date.now().toString();
        const versionKey = `${fileId}:${versionId}`;
        // ciphertext is an ArrayBuffer — create Blob directly
        uploadBlob = new Blob([ciphertext], { type: 'application/octet-stream' });
        cryptoMeta = { ivBase64, saltBase64, versionId, versionKey, hash };
        // persist ciphertext to IndexedDB under versionKey so downloads work even for large files
        try {
          await saveEncryptedFile(versionKey, { name: finalName, type: file.type, size: file.size, ivBase64, saltBase64, versionId, hash }, ciphertext);
          // update files_meta store
          const existing = await getFileMeta(fileId).catch(() => null);
          const fileMeta = existing || { fileId, name: finalName, path: [...currentPath], versions: [] };
          fileMeta.name = finalName; // keep name updated
          fileMeta.versions = fileMeta.versions || [];
          // Always record a new version for this upload (allow identical files to create new versions)
          fileMeta.versions.push({ versionId, versionKey, uploadedAt: new Date().toISOString(), size: file.size, hash });
          await saveFileMeta(fileId, fileMeta);
        } catch (err) {
          console.error('Failed to save encrypted file to IndexedDB:', err);
          swal.fire({
            title: 'Failed to save encrypted file to IndexedDB',
            text: err.message,
            icon: 'error'
          })
        }
      }


      // 2. Upload file directly to the provided Signed URL
      const uploadResponse = await fetch(uploadUrl, {
        method: 'PUT',
        headers: {
          'Content-Type': file.type || 'application/octet-stream',
        },
        body: uploadBlob
      });

      if (!uploadResponse.ok) {
        swal.fire({
          title: 'Failed to upload file to storage',
          text: 'An error occurred while uploading to the storage bucket.',
          icon: 'error',
        }) 
        throw new Error('Failed to upload file to storage');
      }

      // 3. Confirm the upload with the backend
      const confirmResponse = await fetch(`${API_BASE_URL}/api/v1/upload/${fileId}/confirm`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...(token ? { 'Authorization': `Bearer ${token}` } : {})
        }
      });
      if (!confirmResponse.ok) {
        swal.fire({
          title: 'Failed to confirm upload',
          text: 'The server could not confirm the file upload.',
          icon: 'error',
        })
        throw new Error('Failed to confirm upload');
      }

      toast.fire({
        icon: 'success',
        title: 'File upload confirmed!'
      });
      if (onUploadSuccess) {
        // Provide metadata including crypto info and fileId for client-side download/decrypt
        onUploadSuccess({ fileId, name: finalName, type: file.type, size: uploadBlob.size, encrypted: !!cryptoMeta, cryptoMeta, versionId: cryptoMeta && cryptoMeta.versionId });
      }
      setFile(null); // Clear selection
      if (fileInputRef.current) fileInputRef.current.value = '';
      setCustomName('');
      setPassword('');
    } catch (error) {
      console.error('Error during upload process:', error);
      swal.fire({
        title: 'Upload Failed',
        text: error.message,
        icon: 'error'
      })
      // Fallback: mock local storage of file content for UI/demo
      let localMeta = { encrypted: false };
      try {
        if (password) {
          const { ciphertext, ivBase64, saltBase64 } = await encryptFile(file, password);
          // For small files only, keep base64 in-memory for demo; avoid OOM on large files
          const MAX_LOCAL_STORE = 5 * 1024 * 1024; // 5 MB
          if (file.size <= MAX_LOCAL_STORE) {
            const bytes = new Uint8Array(ciphertext);
            let binary = '';
            for (let i = 0; i < bytes.byteLength; i++) binary += String.fromCharCode(bytes[i]);
            const ciphertextBase64 = btoa(binary);
            // also generate a version and persist
            const hash = await hashBuffer(ciphertext);
            const versionId = Date.now().toString();
            const versionKey = `${fileId}:${versionId}`;
            localMeta = { fileId, encrypted: true, ciphertextBase64, ivBase64, saltBase64, versionId, versionKey, hash };
            try {
              await saveEncryptedFile(versionKey, { name: finalName, type: file.type, size: file.size, ivBase64, saltBase64, versionId, hash }, ciphertext);
              // ensure files_meta is updated for fallback/mock path as well
              const existing = await getFileMeta(fileId).catch(() => null);
              const fileMeta = existing || { fileId, name: finalName, path: [...currentPath], versions: [] };
              fileMeta.name = finalName;
              fileMeta.versions = fileMeta.versions || [];
              fileMeta.versions.push({ versionId, versionKey, uploadedAt: new Date().toISOString(), size: file.size, hash });
              await saveFileMeta(fileId, fileMeta);
            } catch (err) { 
              console.error('IDB save failed:', err); 
              swal.fire({
                title: 'Failed to save encrypted file to IndexedDB',
                text: err.message,
                icon: 'error'
              });
            }
          } else {
            const hash = await hashBuffer(ciphertext);
            const versionId = Date.now().toString();
            const versionKey = `${fileId}:${versionId}`;
            localMeta = { fileId, encrypted: true, ivBase64, saltBase64, ciphertextAvailable: false, versionId, versionKey, hash };
            try {
              await saveEncryptedFile(versionKey, { name: finalName, type: file.type, size: file.size, ivBase64, saltBase64, versionId, hash }, ciphertext);
              const existing = await getFileMeta(fileId).catch(() => null);
              const fileMeta = existing || { fileId, name: finalName, path: [...currentPath], versions: [] };
              fileMeta.name = finalName;
              fileMeta.versions = fileMeta.versions || [];
              fileMeta.versions.push({ versionId, versionKey, uploadedAt: new Date().toISOString(), size: file.size, hash });
              await saveFileMeta(fileId, fileMeta);
            } catch (err) { 
              console.error('IDB save failed:', err); 
              swal.fire({
                title: 'Failed to save encrypted file to IndexedDB',
                text: err.message,
                icon: 'error'
              });
            }
          }
        } else {
          // read file as data URL (small files are ok)
          const reader = await new Promise((res, rej) => {
            const r = new FileReader();
            r.onload = () => res(r.result);
            r.onerror = rej;
            r.readAsDataURL(file);
          });
          localMeta = { encrypted: false, dataUrl: reader };
        }
      } catch (err) {
        console.error('Local mock storage failed:', err);
        swal.fire({
          title: 'Failed to save file to local storage',
          text: err.message,
          icon: 'error'
        })
      }

      if (onUploadSuccess) {
        onUploadSuccess({ fileId, name: finalName, type: file.type, size: file.size, ...localMeta, uploadDate: new Date().toISOString(), downloadCount: 0 });
      }
      setFile(null); // Clear selection
      if (fileInputRef.current) fileInputRef.current.value = '';
      setCustomName('');
      setPassword('');
    } finally {
      setUploading(false);
    }
  };

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5, alignItems: 'center', p: 4, width: '100%', boxSizing: 'border-box' }}>
      <Typography variant="h6" sx={{ fontWeight: 700, mb: 1 }}>Upload a File</Typography>

      <Button 
        variant="outlined" 
        component="label"
        disabled={uploading}
        sx={{ borderRadius: '50px', px: 4, py: 1 }}
      >
        Select File
        <input ref={fileInputRef} type="file" hidden onChange={handleFileChange} onClick={(e) => { e.target.value = null; }} />
      </Button>
      {file && (
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, width: '100%', maxWidth: 320, mt: 1 }}>
          <Typography variant="body2" sx={{ textAlign: 'center', color: 'text.secondary' }}>
            Selected: {file.name}
          </Typography>
          <TextField 
            label="Upload as (optional)" 
            variant="outlined" 
            size="medium" 
            value={customName}
            onChange={(e) => setCustomName(e.target.value)}
            disabled={uploading}
            fullWidth
            sx={{ '& .MuiOutlinedInput-root': { borderRadius: 2 } }}
          />
          <TextField
            label="Encryption password (optional)"
            variant="outlined"
            size="medium"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            disabled={uploading}
            type={showPassword ? 'text' : 'password'}
            InputProps={{
              endAdornment: (
                <InputAdornment position="end">
                  <IconButton onClick={() => setShowPassword(s => !s)} edge="end">
                    {showPassword ? <VisibilityOff /> : <Visibility />}
                  </IconButton>
                </InputAdornment>
              )
            }}
            fullWidth
            sx={{ '& .MuiOutlinedInput-root': { borderRadius: 2 } }}
          />
        </Box>
      )}

      <Button 
        variant="contained" 
        color="primary" 
        onClick={handleUpload} 
        disabled={!file || uploading}
        sx={{ borderRadius: '50px', px: 5, py: 1.5, mt: 1, fontWeight: 600, boxShadow: '0 4px 10px rgba(25, 118, 210, 0.2)' }}
      >
        {uploading ? <CircularProgress size={24} color="inherit" /> : 'Upload'}
      </Button>
    </Box>
  );
};
