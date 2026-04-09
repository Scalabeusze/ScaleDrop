
import { Box, Typography, Paper, List, ListItem, ListItemText, ListItemIcon } from '@mui/material';
import { FilePresent } from '@mui/icons-material';

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
              <FilePresent />
            </ListItemIcon>
            <ListItemText primary="Project_Proposal_v2.pdf" secondary="Shared by: Alice (alice@example.com)" />
          </ListItem>
        </List>
      </Paper>
    </Box>
  );
};
