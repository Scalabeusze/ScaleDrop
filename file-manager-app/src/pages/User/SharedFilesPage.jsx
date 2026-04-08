import React from 'react';
import { Box, Typography, Paper, List, ListItem, ListItemText, ListItemIcon } from '@mui/material';
import DescriptionIcon from '@mui/material/Icon'; // fallback generic icon or standard MUI icon like FolderIcon

export const SharedFilesPage = () => {
  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Shared With Me
      </Typography>
      <Paper sx={{ mt: 3, p: 3 }}>
        <List>
          {/* Mocked empty state or single item */}
          <ListItem divider>
            <ListItemIcon>
              {/* Temporary placeholder for an icon */}
              <Box sx={{ width: 24, height: 24, bgcolor: 'primary.main', borderRadius: 1 }} />
            </ListItemIcon>
            <ListItemText primary="Project_Proposal_v2.pdf" secondary="Shared by: Alice (alice@example.com)" />
          </ListItem>
        </List>
      </Paper>
    </Box>
  );
};
