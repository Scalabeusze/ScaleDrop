import React, { useState } from 'react';
import { Typography, Box, Paper, Button, TextField, List, ListItem, ListItemButton, ListItemIcon, ListItemText, Breadcrumbs, Link, IconButton } from '@mui/material';
import { DiskUsageBar } from '../../components/Shared/DiskUsageBar';
import { FileUpload } from '../../components/FileUpload';
import { FileIcon } from '../../components/Shared/FileIcon';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';

export const MyFilesPage = () => {
  // Mock usage: ~2.5 GB
  const usedBytes = 4.8 * 1024 * 1024 * 1024;
  
  const [currentPath, setCurrentPath] = useState([]);
  const [items, setItems] = useState([]);
  const [newFolderName, setNewFolderName] = useState('');

  const currentPathString = '/' + currentPath.join('/');

  const handleCreateFolder = () => {
    if (!newFolderName.trim()) return;
    setItems(prev => [...prev, {
      id: Date.now().toString(),
      type: 'folder',
      name: newFolderName.trim(),
      path: [...currentPath]
    }]);
    setNewFolderName('');
  };

  const handleUploadSuccess = (fileData) => {
    setItems(prev => [...prev, {
      id: Date.now().toString(),
      type: 'file',
      name: fileData.name,
      path: [...currentPath]
    }]);
  };

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

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        My Files
      </Typography>
      <DiskUsageBar usedBytes={usedBytes} />

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
          <FileUpload onUploadSuccess={handleUploadSuccess} />
        </Box>
      </Box>

      <Paper sx={{ mt: 4, p: 2 }}>
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

        {visibleItems.length === 0 ? (
          <Typography variant="body1" color="text.secondary" sx={{ p: 2 }}>
            This folder is empty.
          </Typography>
        ) : (
          <List>
            {visibleItems.map(item => item.type === 'folder' ? (
              <ListItemButton key={item.id} onClick={() => navigateToFolder(item.name)}>
                <ListItemIcon>
                  <FileIcon isFolder={true} />
                </ListItemIcon>
                <ListItemText primary={item.name} />
              </ListItemButton>
            ) : (
              <ListItem key={item.id}>
                <ListItemIcon>
                  <FileIcon filename={item.name} />
                </ListItemIcon>
                <ListItemText primary={item.name} />
              </ListItem>
            ))}
          </List>
        )}
      </Paper>
    </Box>
  );
};
