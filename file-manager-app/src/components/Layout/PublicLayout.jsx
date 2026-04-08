import React, { useContext } from 'react';
import { Outlet, Link } from 'react-router';
import { AppBar, Toolbar, Typography, Button, Box, Switch, FormControlLabel } from '@mui/material';
import { ThemeContext } from '../../context/ThemeContext';

export const PublicLayout = () => {
  const { isDarkMode, toggleTheme } = useContext(ThemeContext);

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh', width: '100vw' }}>
      <AppBar position="static" color="transparent" elevation={0}>
        <Toolbar>
          <Typography variant="h6" component="div" sx={{ flexGrow: 1, fontWeight: 'bold' }}>
            FileManager Cloud
          </Typography>
          <FormControlLabel
            control={<Switch checked={isDarkMode} onChange={toggleTheme} color="primary" />}
            label="Dark Mode"
          />
          <Button color="inherit" component={Link} to="/login">Login</Button>
        </Toolbar>
      </AppBar>
      <Box sx={{ flexGrow: 1, p: 3, display: 'flex', flexDirection: 'column' }}>
        <Outlet />
      </Box>
    </Box>
  );
};
