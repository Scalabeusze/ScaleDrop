import React from 'react';
import { Typography, Box, Paper, Button } from '@mui/material';
import { DiskUsageBar } from '../../components/Shared/DiskUsageBar';

export const MyFilesPage = () => {
  // Mock usage: ~2.5 GB
  const usedBytes = 4.8 * 1024 * 1024 * 1024;

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        My Files
      </Typography>
      <DiskUsageBar usedBytes={usedBytes} />

      <Box sx={{ mt: 4 }}>
        <Typography variant="h6" gutterBottom>
          Actions
        </Typography>
        <Button variant="contained" sx={{ mr: 2 }}>Upload File</Button>
        <Button variant="outlined">New Folder</Button>
      </Box>

      <Paper sx={{ mt: 4, p: 3 }}>
        <Typography variant="body1" color="text.secondary">
          No files uploaded yet.
        </Typography>
      </Paper>
    </Box>
  );
};
