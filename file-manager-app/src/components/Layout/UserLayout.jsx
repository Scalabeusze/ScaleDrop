import React from 'react';
import { Outlet, Link, useNavigate } from 'react-router';
import { AppBar, Toolbar, Typography, Button, Box } from '@mui/material';
import { ThemeSwitcher } from '../Shared/ThemeSwitcher';
import { useAuth } from '../../context/AuthContext';

export const UserLayout = () => {
  const { logout, user } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh', width: '100vw' }}>
      <AppBar position="static">
        <Toolbar>
          <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
            ScaleDrop FM - User Area
          </Typography>
          
          <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
            <Button color="inherit" component={Link} to="/user/my-files">My Files</Button>
            <Button color="inherit" component={Link} to="/user/shared-files">Shared With Me</Button>
            <Button color="inherit" component={Link} to="/user/profile">Profile</Button>
            
            <ThemeSwitcher color="default" sx={{ ml: 2 }} />
            <Typography variant="body2" sx={{ mr: 2 }}>{user?.name}</Typography>
            <Button color="inherit" onClick={handleLogout} variant="outlined">Logout</Button>
          </Box>
        </Toolbar>
      </AppBar>
      <Box component="main" sx={{ flexGrow: 1, p: 3, display: 'flex', flexDirection: 'column' }}>
        <Outlet />
      </Box>
    </Box>
  );
};
