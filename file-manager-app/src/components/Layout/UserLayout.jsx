import React, { useContext } from 'react';
import { Outlet, Link, useNavigate } from 'react-router';
import { AppBar, Toolbar, Typography, Button, Box, Switch, FormControlLabel } from '@mui/material';
import { ThemeContext } from '../../context/ThemeContext';
import { useAuth } from '../../context/AuthContext';

export const UserLayout = () => {
  const { isDarkMode, toggleTheme } = useContext(ThemeContext);
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
            FileManager - User Area
          </Typography>
          
          <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
            <Button color="inherit" component={Link} to="/user/my-files">My Files</Button>
            <Button color="inherit" component={Link} to="/user/shared-files">Shared With Me</Button>
            <Button color="inherit" component={Link} to="/user/profile">Profile</Button>
            
            <FormControlLabel
              control={<Switch checked={isDarkMode} onChange={toggleTheme} color="default" />}
              label="Dark Mode"
              sx={{ ml: 2 }}
            />
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
