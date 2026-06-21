import React, { useState, useEffect } from 'react';
import { Typography, Box, Paper, Button, TextField, List, ListItem, ListItemButton, ListItemIcon, ListItemText, Breadcrumbs, Link, IconButton, Dialog, DialogTitle, DialogContent, DialogActions } from '@mui/material';
import DownloadIcon from '@mui/icons-material/Download';
import ShareIcon from '@mui/icons-material/Share';
import DeleteIcon from '@mui/icons-material/Delete';
import InfoIcon from '@mui/icons-material/Info';
import { motion, AnimatePresence } from 'motion/react';
import { FileUpload } from '../../components/FileUpload';
import { FileIcon } from '../../components/Shared/FileIcon';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import { decryptData, arrayBufferToBlob } from '../../utils/crypto';
import { getEncryptedFile, listAllFileMetas, getFileMeta, deleteEncryptedFile, saveFileMeta, deleteFileMeta } from '../../utils/idb';
import ShareForm from '../../components/Shared/ShareForm';
import { useAppSwal } from '../../hooks/useAppSwal';

export const MyFilesPage = () => {
  const [currentPath, setCurrentPath] = useState([]);
  const [items, setItems] = useState([]);
  const [newFolderName, setNewFolderName] = useState('');
  const [createFolderOpen, setCreateFolderOpen] = useState(false);
  const [detailsOpen, setDetailsOpen] = useState(false);
  const [detailsItem, setDetailsItem] = useState(null);
  const [decryptOpen, setDecryptOpen] = useState(false);
  const [decryptItemId, setDecryptItemId] = useState(null);
  const [decryptPassword, setDecryptPassword] = useState('');
  const [decryptVersionKey, setDecryptVersionKey] = useState(null);
  const [confirmDeleteOpen, setConfirmDeleteOpen] = useState(false);
  const [deleteTargetId, setDeleteTargetId] = useState(null);
  const [confirmDeleteVersionOpen, setConfirmDeleteVersionOpen] = useState(false);
  const [deleteVersionTarget, setDeleteVersionTarget] = useState(null);
  const [shareOpen, setShareOpen] = useState(false);
  const [shareItemId, setShareItemId] = useState(null);
  const [manageSharesOpen, setManageSharesOpen] = useState(false);
  const [sharesList, setSharesList] = useState([]);
  const [revokeConfirmOpen, setRevokeConfirmOpen] = useState(false);
  const [revokeTargetId, setRevokeTargetId] = useState(null);
  const [confirmDeleteFolderOpen, setConfirmDeleteFolderOpen] = useState(false);
  const [deleteFolderTarget, setDeleteFolderTarget] = useState(null);
  const { swal, toast } = useAppSwal();

  const currentPathString = '/' + currentPath.join('/');

  const handleCreateFolder = async () => {
    if (!newFolderName.trim()) return;
    const folderId = Date.now().toString();
    const newFolder = {
      id: folderId,
      type: 'folder',
      name: newFolderName.trim(),
      path: [...currentPath]
    };

    try {
      await saveFileMeta(folderId, newFolder);
    } catch (err) {
      console.error('Failed to save folder meta', err);
      swal.fire({
        title: 'Failed to save folder meta',
        text: err.message,
        icon: 'error'
      })
    }

    setItems(prev => [...prev, newFolder]);
    setNewFolderName('');
    setCreateFolderOpen(false);
    toast.fire({
      icon: 'success',
      title: 'Folder Created',
      text: `Folder "${newFolder.name}" has been successfully created.`
    });
  };

  useEffect(() => {
    // Restore files meta persisted in IndexedDB (logical files + versions)
    (async () => {
      try {
        const metas = await listAllFileMetas();
        if (!metas || metas.length === 0) return;
        setItems(prev => {
          const existingIds = new Set(prev.map(p => p.id));
          const toAdd = metas
            .filter(m => !existingIds.has(m.id))
            .map(m => {
              const meta = m.value || {};
              if (meta.type === 'folder') {
                return {
                  id: m.id,
                  type: 'folder',
                  name: meta.name || `folder-${m.id}`,
                  path: meta.path || [],
                };
              }
              const latest = (meta.versions && meta.versions[meta.versions.length - 1]) || {};
              return {
                id: m.id,
                fileId: m.id,
                type: 'file',
                name: meta.name || `file-${m.id}`,
                path: meta.path || [],
                size: latest.size || meta.size || 0,
                encrypted: !!meta.versions,
                ivBase64: latest.ivBase64 || null,
                saltBase64: latest.saltBase64 || null,
                uploadDate: latest.uploadedAt || meta.uploadDate || new Date().toISOString(),
                downloadCount: meta.downloadCount || latest.downloads || 0,
                versions: meta.versions || [],
              };
            });
          return [...prev, ...toAdd];
        });
      } catch (err) {
        console.error('Failed to restore file metas from IDB', err);
        swal.fire({
          title: 'Failed to restore file metadata',
          text: err.message,
          icon: 'error'
        })
      }
    })();
  }, [swal]);

  const handleUploadSuccess = (fileData) => {
    // If fileId present, load the file meta from IDB to populate versions and latest info
    (async () => {
      if (fileData && fileData.fileId) {
        try {
          const meta = await getFileMeta(fileData.fileId).catch(() => null);
          const latest = (meta && meta.versions && meta.versions[meta.versions.length - 1]) || {};
          setItems(prev => {
            // replace if exists
            const others = prev.filter(p => p.id !== fileData.fileId);
            return [...others, {
              id: fileData.fileId,
              fileId: fileData.fileId,
              type: 'file',
              name: (meta && meta.name) || fileData.name,
              path: (meta && meta.path) || [...currentPath],
              size: latest.size || fileData.size || 0,
              encrypted: !!(meta && meta.versions),
              ivBase64: latest.ivBase64 || fileData.ivBase64,
              saltBase64: latest.saltBase64 || fileData.saltBase64,
              uploadDate: latest.uploadedAt || fileData.uploadDate || new Date().toISOString(),
              downloadCount: (meta && meta.downloadCount) || latest.downloads || fileData.downloadCount || 0,
              versions: (meta && meta.versions) || [],
            }];
          });
        } catch (err) {
          console.error('Failed to read file meta after upload', err);
          swal.fire({
            title: 'Failed to read file metadata after upload',
            text: err.message,
            icon: 'error'
          })
        }
      } else {
        setItems(prev => [...prev, {
          id: Date.now().toString(),
          type: 'file',
          name: fileData.name,
          path: [...currentPath],
          size: fileData.size || 0,
          encrypted: !!fileData.encrypted,
          ciphertextBase64: fileData.ciphertextBase64,
          ivBase64: fileData.ivBase64,
          saltBase64: fileData.saltBase64,
          fileId: fileData.fileId,
          dataUrl: fileData.dataUrl,
          uploadDate: fileData.uploadDate || new Date().toISOString(),
          downloadCount: fileData.downloadCount || 0,
        }]);
      }
    })();
  };

  const saveSharesToLocal = (share) => {
    const key = 'shared_files';
    const existing = JSON.parse(localStorage.getItem(key) || '[]');
    existing.push(share);
    localStorage.setItem(key, JSON.stringify(existing));
  };

  const loadSharesFromLocal = () => {
    const key = 'shared_files';
    const existing = JSON.parse(localStorage.getItem(key) || '[]');
    setSharesList(existing);
  };

  const getSharesCount = (fileId) => {
    const existing = JSON.parse(localStorage.getItem('shared_files') || '[]');
    return existing.filter(s => s.fileId === fileId).length;
  };

  const revokeShare = (shareId) => {
    const key = 'shared_files';
    const existing = JSON.parse(localStorage.getItem(key) || '[]');
    const filtered = existing.filter(s => s.id !== shareId);
    localStorage.setItem(key, JSON.stringify(filtered));
    setSharesList(filtered);
  };

  const handleRevokeClick = (shareId) => {
    setRevokeTargetId(shareId);
    setRevokeConfirmOpen(true);
  };

  const handleConfirmRevoke = () => {
    if (revokeTargetId) revokeShare(revokeTargetId);
    setRevokeConfirmOpen(false);
    setRevokeTargetId(null);
    toast.fire({
      icon: 'success',
      title: 'Share Revoked',
      text: 'Access has been removed for the recipient.'
    });
  };

  const handleCancelRevoke = () => { setRevokeConfirmOpen(false); setRevokeTargetId(null); };

  const incrementDownloadCount = async (itemId) => {
    try {
      const meta = await getFileMeta(itemId).catch(() => null);
      if (meta) {
        meta.downloadCount = (meta.downloadCount || 0) + 1;
        await saveFileMeta(itemId, meta);
      }
    } catch (e) {
      console.error('Failed to update download count in IDB', e);
    }
    setItems(prev => prev.map(i => i.id === itemId ? { ...i, downloadCount: (i.downloadCount || 0) + 1 } : i));
  };

  const handleDownload = (itemId) => {
    const item = items.find(i => i.id === itemId);
    if (!item) return;
    if (item.encrypted) {
      // open decrypt dialog
      setDecryptItemId(itemId);
      setDecryptPassword('');
      setDecryptOpen(true);
      return;
    }
    // non-encrypted: download immediately
    (async () => {
      try {
        let blob = null;
        if (item.dataUrl) {
          const res = await fetch(item.dataUrl);
          blob = await res.blob();
        } else {
          blob = new Blob([`Mock content of ${item.name}`], { type: 'text/plain' });
        }
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = item.name;
        document.body.appendChild(a);
        a.click();
        a.remove();
        URL.revokeObjectURL(url);
        await incrementDownloadCount(itemId);
        toast.fire({
          icon: 'success',
          title: 'Download Started',
        });
      } catch (err) {
        console.error('Download failed', err);
        swal.fire({
          title: 'Download failed',
          text: err.message,
          icon: 'error'
        })
      }
    })();
  };

  const handleShare = (itemId) => {
    setShareItemId(itemId);
    setShareOpen(true);
  };

  const handleShareConfirm = ({ recipient, fileSelections, includeVersionsGlobal }) => {
    if (!recipient || !fileSelections || fileSelections.length === 0) return;
    for (const sel of fileSelections) {
      const fid = sel.fileId;
      const item = items.find(i => i.id === fid) || { name: fid };
      const share = {
        id: Date.now().toString() + '-' + fid,
        fileId: fid,
        name: item.name,
        recipient,
        includeVersions: sel.versionKeys ? sel.versionKeys : (includeVersionsGlobal ? 'all' : 'latest'),
        versionKeys: sel.versionKeys || null,
        sharedBy: 'you',
        date: new Date().toISOString()
      };
      saveSharesToLocal(share);
    }
    setShareOpen(false);
    toast.fire({
      icon: 'success',
      title: 'Files Shared',
      text: `Successfully shared with ${recipient}.`
    });
  };

  const handleDecryptConfirm = async () => {
    const item = items.find(i => i.id === decryptItemId);
    if (!item) return setDecryptOpen(false);
    try {
      let ciphertextInput = null;
      let iv = item.ivBase64;
      let salt = item.saltBase64;

      if (decryptVersionKey) {
        const rec = await getEncryptedFile(decryptVersionKey).catch(() => null);
        if (!rec || !rec.ciphertext) {
          alert('Encrypted file content not available locally for demo.');
          setDecryptOpen(false);
          setDecryptVersionKey(null);
          return;
        }
        ciphertextInput = rec.ciphertext;
        iv = iv || (rec.meta && rec.meta.ivBase64);
        salt = salt || (rec.meta && rec.meta.saltBase64);
      } else if (item.ciphertextBase64) {
        ciphertextInput = item.ciphertextBase64;
      } else if (item.fileId) {
        // fetch file meta to find latest version
        const meta = await getFileMeta(item.fileId).catch(() => null);
        const latest = (meta && meta.versions && meta.versions[meta.versions.length - 1]);
        const keyToFetch = latest && latest.versionKey;
        if (!keyToFetch) {
          alert('Encrypted file content not available locally for demo.');
          setDecryptOpen(false);
          return;
        }
        const rec = await getEncryptedFile(keyToFetch).catch(() => null);
        if (!rec || !rec.ciphertext) {
          alert('Encrypted file content not available locally for demo.');
          setDecryptOpen(false);
          return;
        }
        ciphertextInput = rec.ciphertext; // ArrayBuffer
        iv = iv || (rec.meta && rec.meta.ivBase64);
        salt = salt || (rec.meta && rec.meta.saltBase64);
      } else {
        alert('Encrypted file content not available locally for demo.');
        setDecryptOpen(false);
        return;
      }

      const plain = await decryptData(ciphertextInput, decryptPassword, iv, salt);
      const blob = arrayBufferToBlob(plain, item.type || 'application/octet-stream');
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = item.name;
      document.body.appendChild(a);
      a.click();
      a.remove();
      URL.revokeObjectURL(url);
      await incrementDownloadCount(decryptItemId);
      toast.fire({
        icon: 'success',
        title: 'Decryption Successful',
        text: 'The file has been decrypted and the download has started.'
      });
    } catch (err) {
      console.error('Decryption failed', err);
      swal.fire({
        title: 'Decryption failed',
        text: err.message,
        icon: 'error'
      });
    } finally {
      setDecryptOpen(false);
      setDecryptPassword('');
      setDecryptItemId(null);
      setDecryptVersionKey(null);
    }
  };

  const handleDelete = (itemId) => {
    setDeleteTargetId(itemId);
    setConfirmDeleteOpen(true);
  };

  const handleConfirmDelete = async () => {
    const fileId = deleteTargetId;
    if (!fileId) {
      setConfirmDeleteOpen(false);
      return;
    }
    try {
      const meta = await getFileMeta(fileId).catch(() => null);
      if (meta && meta.versions) {
        for (const v of meta.versions) {
          try { await deleteEncryptedFile(v.versionKey); } catch (e) { console.error('Failed deleting version ciphertext', e); }
        }
      }
      try { await deleteFileMeta(fileId); } catch (e) { console.error('Failed deleting file meta', e); }
      setItems(prev => prev.filter(i => i.id !== fileId));
      toast.fire({
        icon: 'success',
        title: 'Deleted!',
        text: 'The file has been permanently deleted.'
      });
    } catch (err) {
      console.error('Error deleting file', err);
      swal.fire({
        title: 'Failed to delete file',
        text: err.message,
        icon: 'error'
      });
    } finally {
      setConfirmDeleteOpen(false);
      setDeleteTargetId(null);
    }
  };

  const handleCancelDelete = () => { setConfirmDeleteOpen(false); setDeleteTargetId(null); };

  const handleDeleteFolderClick = (folderItem) => {
    setDeleteFolderTarget(folderItem);
    setConfirmDeleteFolderOpen(true);
  };

  const handleConfirmDeleteFolder = async () => {
    const target = deleteFolderTarget;
    if (!target) return;

    const targetPath = [...target.path, target.name];
    const isInsideFolder = (itemPath, folderPath) => {
      if (itemPath.length < folderPath.length) return false;
      return folderPath.every((p, i) => itemPath[i] === p);
    };

    const itemsToDelete = items.filter(item => item.id === target.id || isInsideFolder(item.path, targetPath));

    try {
      for (const item of itemsToDelete) {
        if (item.type === 'file') {
          const meta = await getFileMeta(item.id).catch(() => null);
          if (meta && meta.versions) {
            for (const v of meta.versions) {
              try { await deleteEncryptedFile(v.versionKey); } catch (e) { console.error('Failed deleting version', e); }
            }
          }
        }
        try { await deleteFileMeta(item.id); } catch (e) { console.error('Failed deleting meta', e); }
      }
      setItems(prev => prev.filter(i => !itemsToDelete.find(td => td.id === i.id)));
      toast.fire({
        icon: 'success',
        title: 'Folder Deleted!',
        text: 'The folder and all its contents were removed.'
      });
    } catch (err) {
      console.error('Error deleting folder', err);
      swal.fire({
        title: 'Failed to delete folder',
        text: err.message,
        icon: 'error'
      });
    } finally {
      setConfirmDeleteFolderOpen(false);
      setDeleteFolderTarget(null);
    }
  };

  const openDetails = (itemId) => {
    const item = items.find(i => i.id === itemId);
    if (!item) return;
    setDetailsItem(item);
    setDetailsOpen(true);
  };

  const closeDetails = () => { setDetailsOpen(false); setDetailsItem(null); };

  const navigateToFolder = (folderName) => {
    setCurrentPath(prev => [...prev, folderName]);
  };

  const navigateUp = () => {
    setCurrentPath(prev => prev.slice(0, -1));
  };

  const navigateToBreadcrumb = (index) => {
    setCurrentPath(prev => prev.slice(0, index + 1));
  };

  const navigateToRoot = () => {
    setCurrentPath([]);
  };

  const visibleItems = items.filter(item => {
    if (item.path.length !== currentPath.length) return false;
    return item.path.every((p, i) => p === currentPath[i]);
  });

  const shareItem = items.find(i => i.id === shareItemId);

  return (
    <Box 
      component={motion.div} 
      initial={{ opacity: 0, y: 20 }} 
      animate={{ opacity: 1, y: 0 }} 
      transition={{ duration: 0.4, type: 'spring', stiffness: 100 }}
      sx={{ maxWidth: 1400, mx: 'auto', p: { xs: 2, md: 4 } }}
    >
      <Typography variant="h3" gutterBottom sx={{ fontWeight: 800, background: (theme) => theme.palette.gradients.primary, WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent', mb: 1 }}>
        My Files
      </Typography>
      <Typography variant="body1" color="text.secondary" paragraph sx={{ mb: 4 }}>
        Securely manage, organize, and encrypt your personal documents.
      </Typography>

      <Box sx={{ mt: 2, display: 'flex', justifyContent: 'center' }}>
        <Box sx={{ width: '100%', maxWidth: 600, borderRadius: 4, overflow: 'hidden', boxShadow: '0 8px 24px rgba(0,0,0,0.05)', border: '1px solid', borderColor: 'divider', bgcolor: 'background.paper' }}>
          <FileUpload onUploadSuccess={handleUploadSuccess} currentPath={currentPath} />
        </Box>
      </Box>

      <Paper component={motion.div} layout transition={{ layout: { duration: 0.4, type: 'spring', stiffness: 200, damping: 20 } }} sx={{ mt: 5, p: 3, borderRadius: 4, boxShadow: '0 12px 32px rgba(0,0,0,0.05)', border: '1px solid', borderColor: 'divider' }}>
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            <IconButton onClick={navigateUp} disabled={currentPath.length === 0} sx={{ mr: 1 }}>
              <ArrowBackIcon />
            </IconButton>
            <Breadcrumbs aria-label="breadcrumb">
              <Link underline="hover" color="inherit" component="button" onClick={navigateToRoot}>
                Root
              </Link>
              {currentPath.map((part, index) => (
                <Link 
                  key={index}
                  underline="hover" 
                  color={index === currentPath.length - 1 ? "text.primary" : "inherit"} 
                  component="button" 
                  onClick={() => navigateToBreadcrumb(index)}
                >
                  {part}
                </Link>
              ))}
            </Breadcrumbs>
          </Box>
          <Button variant="contained" onClick={() => setCreateFolderOpen(true)} sx={{ borderRadius: '50px', px: 3, py: 1, boxShadow: '0 4px 10px rgba(25, 118, 210, 0.2)' }}>
            + New Folder
          </Button>
        </Box>

        <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mb: 2, ml: 1 }}>
          Current Location: {currentPathString}
        </Typography>

        <List component={motion.ul} layout>
          <AnimatePresence>
            {visibleItems.length === 0 ? (
              <motion.div
                key="empty-state"
                initial={{ opacity: 0 }}
                animate={{ opacity: 1, transition: { delay: 0.4, duration: 0.3 } }}
                exit={{ opacity: 0, transition: { duration: 0.2, delay: 0.1 } }}
                layout
              >
                <Typography variant="body1" color="text.secondary" sx={{ p: 2 }}>
                  This folder is empty.
                </Typography>
              </motion.div>
            ) : (
              visibleItems.map((item, index) => item.type === 'folder' ? (
                <motion.div
                  key={item.id}
                  initial={{ opacity: 0, x: -20 }}
                  animate={{ opacity: 1, x: 0, transition: { opacity: { delay: 0.4 + index * 0.05, duration: 0.3 }, x: { delay: 0.4 + index * 0.05, type: 'spring', stiffness: 200, damping: 20 } } }}
                  exit={{ opacity: 0, x: 20, transition: { duration: 0.2, delay: 0.1 } }}
                  layout
                >
                  <ListItem disablePadding secondaryAction={
                    <IconButton edge="end" onClick={() => handleDeleteFolderClick(item)} title="Delete Folder">
                      <DeleteIcon />
                    </IconButton>
                  }>
                    <ListItemButton onClick={() => navigateToFolder(item.name)} sx={{ pr: 6 }}>
                      <ListItemIcon>
                        <FileIcon isFolder={true} />
                      </ListItemIcon>
                      <ListItemText primary={item.name} primaryTypographyProps={{ noWrap: true, title: item.name }} />
                    </ListItemButton>
                  </ListItem>
                </motion.div>
              ) : (
                <motion.div
                  key={item.id}
                  initial={{ opacity: 0, x: -20 }}
                  animate={{ opacity: 1, x: 0, transition: { opacity: { delay: 0.4 + index * 0.05, duration: 0.3 }, x: { delay: 0.4 + index * 0.05, type: 'spring', stiffness: 200, damping: 20 } } }}
                  exit={{ opacity: 0, x: 20, transition: { duration: 0.2, delay: 0.1 } }}
                  layout
                >
                  <ListItem secondaryAction={(
                    <Box sx={{ display: 'flex' }}>
                      <IconButton edge="end" onClick={() => handleDownload(item.id)} title="Download"><DownloadIcon /></IconButton>
                      <IconButton edge="end" onClick={() => handleShare(item.id)} title="Share"><ShareIcon /></IconButton>
                      <IconButton edge="end" onClick={() => openDetails(item.id)} title="Version Details"><InfoIcon /></IconButton>
                      <IconButton edge="end" onClick={() => handleDelete(item.id)} title="Delete"><DeleteIcon /></IconButton>
                    </Box>
                  )}>
                    <ListItemIcon>
                      <FileIcon filename={item.name} />
                    </ListItemIcon>
                    <ListItemText 
                      primary={item.name} 
                      primaryTypographyProps={{ noWrap: true, title: item.name }} 
                      sx={{ pr: { xs: 16, sm: 20 } }} 
                    />
                  </ListItem>
                </motion.div>
              ))
            )}
          </AnimatePresence>
        </List>
        <Box sx={{ mt: 2, display: 'flex', gap: 1 }}>
          <Button variant="outlined" onClick={() => { loadSharesFromLocal(); setManageSharesOpen(true); }}>Manage Shares</Button>
        </Box>
      </Paper>

      <Dialog open={createFolderOpen} onClose={() => setCreateFolderOpen(false)} PaperProps={{ sx: { borderRadius: 3, p: 1 } }}>
        <DialogTitle sx={{ fontWeight: 700 }}>Create New Folder</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            label="Folder Name"
            fullWidth
            variant="outlined"
            value={newFolderName}
            onChange={(e) => setNewFolderName(e.target.value)}
            sx={{ mt: 1, '& .MuiOutlinedInput-root': { borderRadius: 2 } }}
          />
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={() => setCreateFolderOpen(false)} sx={{ borderRadius: '50px', px: 3 }}>Cancel</Button>
          <Button onClick={handleCreateFolder} variant="contained" disabled={!newFolderName.trim()} sx={{ borderRadius: '50px', px: 4, boxShadow: '0 4px 10px rgba(25, 118, 210, 0.2)' }}>Create</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={decryptOpen} onClose={() => setDecryptOpen(false)}>
        <DialogTitle>Decrypt File</DialogTitle>
        <DialogContent>
          <TextField
            label="Password"
            type="password"
            fullWidth
            value={decryptPassword}
            onChange={(e) => setDecryptPassword(e.target.value)}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDecryptOpen(false)}>Cancel</Button>
          <Button onClick={handleDecryptConfirm} variant="contained">Decrypt & Download</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={confirmDeleteOpen} onClose={handleCancelDelete}>
        <DialogTitle>Confirm Delete</DialogTitle>
        <DialogContent>
          <Typography>Are you sure you want to delete this file and all its versions? This action cannot be undone in the mocked demo.</Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCancelDelete}>Cancel</Button>
          <Button color="error" variant="contained" onClick={handleConfirmDelete}>Delete</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={confirmDeleteFolderOpen} onClose={() => setConfirmDeleteFolderOpen(false)}>
        <DialogTitle>Confirm Delete Folder</DialogTitle>
        <DialogContent>
          <Typography>Are you sure you want to delete this folder and all of its contents? This action cannot be undone.</Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmDeleteFolderOpen(false)}>Cancel</Button>
          <Button color="error" variant="contained" onClick={handleConfirmDeleteFolder}>Delete Folder</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={confirmDeleteVersionOpen} onClose={() => { setConfirmDeleteVersionOpen(false); setDeleteVersionTarget(null); }}>
        <DialogTitle>Confirm Delete Version</DialogTitle>
        <DialogContent>
          <Typography>Delete this version's ciphertext from IndexedDB? This cannot be undone in the mocked demo.</Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => { setConfirmDeleteVersionOpen(false); setDeleteVersionTarget(null); }}>Cancel</Button>
          <Button color="error" variant="contained" onClick={async () => {
            const target = deleteVersionTarget;
            if (!target) { setConfirmDeleteVersionOpen(false); return; }
            try {
              await deleteEncryptedFile(target.versionKey);
              const meta = await getFileMeta(target.fileId).catch(() => null);
              if (meta && meta.versions) {
                meta.versions = meta.versions.filter(v => v.versionKey !== target.versionKey);
                await saveFileMeta(target.fileId, meta);
                setItems(prev => prev.map(i => i.id === target.fileId ? { ...i, versions: meta.versions, size: (meta.versions && meta.versions[meta.versions.length-1] && meta.versions[meta.versions.length-1].size) || i.size } : i));
                if (detailsItem && detailsItem.id === target.fileId) setDetailsItem(prev => ({ ...prev, versions: meta.versions }));
                toast.fire({
                  icon: 'success',
                  title: 'Version Deleted'
                });
              }
            } catch (err) { console.error('Failed to delete version', err); }
            setConfirmDeleteVersionOpen(false);
            setDeleteVersionTarget(null);
          }}>Delete Version</Button>
        </DialogActions>
      </Dialog>

      <ShareForm
        open={shareOpen}
        onClose={() => setShareOpen(false)}
        fileOptions={shareItem ? [shareItem] : []}
        defaultSelected={shareItemId ? [shareItemId] : []}
        onConfirm={handleShareConfirm}
      />

      <Dialog open={manageSharesOpen} onClose={() => setManageSharesOpen(false)} fullWidth maxWidth="md">
        <DialogTitle>Manage Shares</DialogTitle>
        <DialogContent>
          {sharesList.length === 0 ? (
            <Typography>No shares created yet.</Typography>
          ) : (
            <List>
              {sharesList.map(s => (
                <ListItem key={s.id} secondaryAction={(
                  <Button color="error" size="small" variant="outlined" onClick={() => handleRevokeClick(s.id)}>Revoke</Button>
                )}>
                  <ListItemText primary={`${s.name || s.fileId} → ${s.recipient}`} secondary={`Shared on: ${new Date(s.date).toLocaleString()}`} />
                </ListItem>
              ))}
            </List>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setManageSharesOpen(false)}>Close</Button>
        </DialogActions>
      </Dialog>
      <Dialog open={revokeConfirmOpen} onClose={handleCancelRevoke}>
        <DialogTitle>Revoke Share</DialogTitle>
        <DialogContent>
          <Typography>Are you sure you want to revoke this share? This will remove access for the recipient.</Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCancelRevoke}>Cancel</Button>
          <Button color="error" variant="contained" onClick={handleConfirmRevoke}>Revoke</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={detailsOpen} onClose={closeDetails}>
        <DialogTitle>File Version Details</DialogTitle>
        <DialogContent>
          {detailsItem ? (
            <Box>
              <Box sx={{ mb: 3, p: 2, bgcolor: 'background.default', borderRadius: 2 }}>
                <Typography variant="body1" sx={{ mb: 1 }}><strong>Name:</strong> {detailsItem.name}</Typography>
                <Typography variant="body1" sx={{ mb: 1 }}><strong>Uploaded:</strong> {new Date(detailsItem.uploadDate).toLocaleString()}</Typography>
                <Typography variant="body1" sx={{ mb: 1 }}><strong>Total Downloads:</strong> {detailsItem.downloadCount || 0}</Typography>
                <Typography variant="body1" sx={{ mb: 1 }}><strong>Total Shares:</strong> {getSharesCount(detailsItem.id)}</Typography>
                <Typography variant="body1"><strong>File Size:</strong> {detailsItem.size || 0} bytes</Typography>
              </Box>
            </Box>
          ) : null}
        </DialogContent>
        <DialogActions>
          <Button onClick={closeDetails}>Close</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

