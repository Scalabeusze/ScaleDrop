import { Box, Typography, Paper, Avatar, Divider, Button } from '@mui/material';
import { useAuth } from '../../context/useAuth';
import { motion } from 'motion/react';

export const ProfilePage = () => {
  const { user } = useAuth();

  return (
    <Box 
      component={motion.div} 
      initial={{ opacity: 0, y: 15 }} 
      animate={{ opacity: 1, y: 0 }} 
      transition={{ duration: 0.3 }}
      sx={{ maxWidth: 600, mx: 'auto', width: '100%', mt: 4 }}
    >
      <Paper sx={{ p: 4 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
          <Avatar sx={{ width: 64, height: 64, mr: 3 }}>{user?.name?.[0]}</Avatar>
          <Box>
            <Typography variant="h5">{user?.name}</Typography>
          </Box>
        </Box>
        <Divider sx={{ my: 2 }} />
        <Typography variant="h6" gutterBottom>
          Account Details
        </Typography>
        <Typography variant="body1">
          <strong>Email:</strong> {user?.name?.toLowerCase().replace(/\s/g, '')}@example.com
        </Typography>
        <Box sx={{ mt: 4 }}>
          <Button variant="outlined" color="error">
            Delete Account
          </Button>
        </Box>
      </Paper>
    </Box>
  );
};
