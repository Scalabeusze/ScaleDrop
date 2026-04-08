import React, { useContext } from 'react';
import { Outlet, Link, useNavigate } from 'react-router';
import { AppBar, Toolbar, Typography, Button, Box, Drawer, List, ListItem, ListItemButton, ListItemText, Switch, FormControlLabel } from '@mui/material';
import { ThemeContext } from '../../context/ThemeContext';
import { useAuth } from '../../context/AuthContext';

const drawerWidth = 240;

export const AdminLayout = () => {
  const { isDarkMode, toggleTheme } = useContext(ThemeContext);
  const { logout, user } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh', width: '100vw' }}>
      <AppBar position="fixed" sx={{ zIndex: (theme) => theme.zIndex.drawer + 1, backgroundColor: 'primary.dark' }}>
        <Toolbar>
          <Typography variant="h6" noWrap component="div" sx={{ flexGrow: 1 }}>
            Admin Console
          </Typography>
          <FormControlLabel
            control={<Switch checked={isDarkMode} onChange={toggleTheme} color="default" />}
            label="Dark Mode"
            sx={{ ml: 2 }}
          />
          <Typography variant="body2" sx={{ mr: 2 }}>{user?.name}</Typography>
          <Button color="inherit" onClick={handleLogout} variant="outlined">Logout</Button>
        </Toolbar>
      </AppBar>
      <Drawer
        variant="permanent"
        sx={{
          width: drawerWidth,
          flexShrink: 0,
          [`& .MuiDrawer-paper`]: { width: drawerWidth, boxSizing: 'border-box' },
        }}
      >
        <Toolbar />
        <Box sx={{ overflow: 'auto' }}>
          <List>
            <ListItem disablePadding>
              <ListItemButton component={Link} to="/admin/dashboard">
                <ListItemText primary="Dashboard" />
              </ListItemButton>
            </ListItem>
            <ListItem disablePadding>
              <ListItemButton component={Link} to="/admin/logs">
                <ListItemText primary="System Logs" />
              </ListItemButton>
            </ListItem>
          </List>
        </Box>
      </Drawer>
      <Box component="main" sx={{ flexGrow: 1, p: 3, display: 'flex', flexDirection: 'column' }}>
        <Toolbar />
        <Outlet />
      </Box>
    </Box>
  );
};
