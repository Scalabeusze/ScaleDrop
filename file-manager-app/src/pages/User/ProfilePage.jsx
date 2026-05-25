import { Box, Typography, Paper, Avatar, Divider, Button } from '@mui/material';
import { useAuth } from '../../context/useAuth';
import { motion } from 'motion/react';

export const ProfilePage = () => {
  const { user } = useAuth();

  return (
    <Box 
      component={motion.div} 
      initial={{ opacity: 0, scale: 0.95, y: 20 }} 
      animate={{ opacity: 1, scale: 1, y: 0 }} 
      transition={{ type: 'spring', stiffness: 100, damping: 15 }}
      sx={{ maxWidth: 800, mx: 'auto', width: '100%', mt: { xs: 4, md: 8 }, p: { xs: 2, md: 4 } }}
    <Typography variant="h3" gutterBottom sx={{ fontWeight: 800, background: (theme) => theme.palette.gradients.primary, WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent', textAlign: 'center', mb: 5 }}>
      Profile Settings
    </Typography>

    <Paper sx={{ p: { xs: 3, md: 5 }, borderRadius: 4, boxShadow: (theme) => theme.palette.customShadows.paper, border: '1px solid', borderColor: 'divider' }}>
      <Box sx={{ display: 'flex', flexDirection: { xs: 'column', md: 'row' }, gap: 4, alignItems: { xs: 'center', md: 'flex-start' } }}>
        <Avatar sx={{ width: 80, height: 80, fontSize: '2rem', background: (theme) => theme.palette.gradients.avatar, boxShadow: '0 8px 16px rgba(25, 118, 210, 0.25)' }}>{user?.name?.[0]}</Avatar>
          <Box>
            <Typography variant="h4" sx={{ fontWeight: 700 }}>{user?.name}</Typography>
            <Typography variant="subtitle1" color="text.secondary">Premium Member</Typography>
          </Box>
        </Box>
        <Divider sx={{ my: 3 }} />
        <Typography variant="h6" sx={{ fontWeight: 600, mb: 2 }}>
          Account Details
        </Typography>
        <Typography variant="body1" sx={{ mb: 1, fontSize: '1.1rem' }}>
          <strong>Email:</strong> {user?.name?.toLowerCase().replace(/\s/g, '')}@example.com
        </Typography>
        <Box sx={{ mt: 5, display: 'flex', justifyContent: 'flex-end' }}>
          <Button variant="outlined" color="error" size="large" sx={{ borderRadius: '50px', px: 4, py: 1 }}>
            Delete Account
          </Button>
        </Box>
      </Paper>
    </Box>
  );
};
