import React, { useState } from 'react';
import { Box, Button, Typography, CircularProgress } from '@mui/material';

export const FileUpload = () => {
  const [file, setFile] = useState(null);
  const [uploading, setUploading] = useState(false);

  const handleFileChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      setFile(e.target.files[0]);
    }
  };

  const handleUpload = async () => {
    if (!file) return;
    setUploading(true);

    try {
      // 1. Send metadata to backend to request a signed URL
      const requestResponse = await fetch('/api/files/upload-request', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          filename: file.name,
          contentType: file.type,
          size: file.size,
        })
      });
      
      if (!requestResponse.ok) throw new Error('Failed to get signed URL');
      const { signedUrl } = await requestResponse.json();

      // 2. Upload file directly to the provided Signed URL
      const uploadResponse = await fetch(signedUrl, {
        method: 'PUT',
        headers: {
          'Content-Type': file.type,
        },
        body: file,
      });

      if (!uploadResponse.ok) throw new Error('Failed to upload file to storage');

      alert('File uploaded successfully!');
      setFile(null); // Clear selection
    } catch (error) {
      console.error('Error during upload process:', error);
      alert('Upload failed. Please try again.');
    } finally {
      setUploading(false);
    }
  };

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, alignItems: 'flex-start', mt: 3 }}>
      <Typography variant="h6">Upload a File</Typography>
      
      <Button 
        variant="outlined" 
        component="label"
        disabled={uploading}
      >
        Select File
        <input type="file" hidden onChange={handleFileChange} />
      </Button>
      {file && <Typography variant="body2">Selected: {file.name}</Typography>}

      <Button 
        variant="contained" 
        color="primary" 
        onClick={handleUpload} 
        disabled={!file || uploading}
      >
        {uploading ? <CircularProgress size={24} color="inherit" /> : 'Upload'}
      </Button>
    </Box>
  );
};
