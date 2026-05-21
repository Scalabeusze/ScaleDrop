import React, { useState, useEffect } from 'react';
import { Typography, Box, Paper, Button, TextField, List, ListItem, ListItemButton, ListItemIcon, ListItemText, Breadcrumbs, Link, IconButton, Dialog, DialogTitle, DialogContent, DialogActions } from '@mui/material';
import DownloadIcon from '@mui/icons-material/Download';
import ShareIcon from '@mui/icons-material/Share';
import DeleteIcon from '@mui/icons-material/Delete';
import HistoryIcon from '@mui/icons-material/History';
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
  const [historyOpen, setHistoryOpen] = useState(false);
  const [historyItem, setHistoryItem] = useState(null);
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
                downloadCount: latest.downloads || 0,
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
  }, []);

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
              downloadCount: latest.downloads || fileData.downloadCount || 0,
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
        setItems(prev => prev.map(i => i.id === itemId ? { ...i, downloadCount: (i.downloadCount || 0) + 1 } : i));
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
      setItems(prev => prev.map(i => i.id === decryptItemId ? { ...i, downloadCount: (i.downloadCount || 0) + 1 } : i));
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

  const openHistory = (itemId) => {
    const item = items.find(i => i.id === itemId);
    if (!item) return;
    setHistoryItem(item);
    setHistoryOpen(true);
  };

  const closeHistory = () => { setHistoryOpen(false); setHistoryItem(null); };

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
      initial={{ opacity: 0, y: 15 }} 
      animate={{ opacity: 1, y: 0 }} 
      transition={{ duration: 0.3 }}
    >
      <Typography variant="h4" gutterBottom>
        My Files
      </Typography>

      <Box sx={{ mt: 4, display: 'flex', gap: 2, alignItems: 'flex-start', flexWrap: 'wrap' }}>
        <Box sx={{ flex: 1, minWidth: '300px', mt: 3 }}>
          <Typography variant="h6" gutterBottom>
            Create Folder
          </Typography>
          <Box sx={{ display: 'flex', gap: 1 }}>
            <TextField 
              size="small" 
              label="Folder Name" 
              value={newFolderName}
              onChange={(e) => setNewFolderName(e.target.value)}
            />
            <Button variant="outlined" onClick={handleCreateFolder} disabled={!newFolderName.trim()}>
              New Folder
            </Button>
          </Box>
        </Box>
        <Box sx={{ flex: 1, minWidth: '300px' }}>
          <FileUpload onUploadSuccess={handleUploadSuccess} currentPath={currentPath} />
        </Box>
      </Box>

      <Paper component={motion.div} layout transition={{ layout: { duration: 0.4, type: 'spring', stiffness: 200, damping: 20 } }} sx={{ mt: 4, p: 2 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
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
                    <ListItemButton onClick={() => navigateToFolder(item.name)}>
                      <ListItemIcon>
                        <FileIcon isFolder={true} />
                      </ListItemIcon>
                      <ListItemText primary={item.name} />
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
                    <Box>
                      <IconButton edge="end" onClick={() => handleDownload(item.id)} title="Download"><DownloadIcon /></IconButton>
                      <IconButton edge="end" onClick={() => handleShare(item.id)} title="Share"><ShareIcon /></IconButton>
                      <IconButton edge="end" onClick={() => openHistory(item.id)} title="History"><HistoryIcon /></IconButton>
                      <IconButton edge="end" onClick={() => handleDelete(item.id)} title="Delete"><DeleteIcon /></IconButton>
                    </Box>
                  )}>
                    <ListItemIcon>
                      <FileIcon filename={item.name} />
                    </ListItemIcon>
                    <ListItemText primary={item.name} secondary={`${(item.size||0)} bytes — downloads: ${item.downloadCount||0}`} />
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
                if (historyItem && historyItem.id === target.fileId) setHistoryItem(prev => ({ ...prev, versions: meta.versions }));
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
                  <ListItemText primary={`${s.name || s.fileId} → ${s.recipient}`} secondary={`Versions: ${s.versionKeys ? s.versionKeys.join(', ') : s.includeVersions}`} />
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

      <Dialog open={historyOpen} onClose={closeHistory}>
        <DialogTitle>File History</DialogTitle>
        <DialogContent>
          {historyItem ? (
            <Box>
              <Typography><strong>Name:</strong> {historyItem.name}</Typography>
              <Typography><strong>Uploaded:</strong> {historyItem.uploadDate}</Typography>
              <Typography><strong>Downloads:</strong> {historyItem.downloadCount || 0}</Typography>
              <Box sx={{ mt: 2 }}>
                <Typography variant="subtitle2">Versions</Typography>
                {(historyItem.versions && historyItem.versions.length > 0) ? (
                  <List>
                    {historyItem.versions.map(v => (
                      <ListItem key={v.versionKey}>
                        <ListItemText primary={`Version ${v.versionId}`} secondary={`${v.uploadedAt} — ${v.size || 'N/A'} bytes`} />
                        <Box sx={{ display: 'flex', gap: 1 }}>
                          <Button size="small" variant="outlined" onClick={() => {
                            setDecryptItemId(historyItem.id);
                            setDecryptVersionKey(v.versionKey);
                            setDecryptPassword('');
                            setDecryptOpen(true);
                            setHistoryOpen(false);
                          }}>Download</Button>
                          <Button size="small" color="error" variant="outlined" onClick={() => {
                            setDeleteVersionTarget({ fileId: historyItem.id, versionKey: v.versionKey });
                            setConfirmDeleteVersionOpen(true);
                            setHistoryOpen(false);
                          }}>Delete</Button>
                        </Box>
                      </ListItem>
                    ))}
                  </List>
                ) : (
                  <Typography>No versions available.</Typography>
                )}
              </Box>
            </Box>
          ) : null}
        </DialogContent>
        <DialogActions>
          <Button onClick={closeHistory}>Close</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};
