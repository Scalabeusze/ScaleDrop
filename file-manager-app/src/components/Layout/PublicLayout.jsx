import React from 'react';
import { Outlet, Link } from 'react-router';
import { AppBar, Toolbar, Typography, Button, Box } from '@mui/material';
import { ThemeSwitcher } from '../Shared/ThemeSwitcher';

export const PublicLayout = () => {
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh', width: '100vw' }}>
      <AppBar position="static" color="transparent" elevation={0}>
        <Toolbar>
          <Typography variant="h6" component="div" sx={{ flexGrow: 1, fontWeight: 'bold' }}>
            ScaleDrop FM
          </Typography>
          <ThemeSwitcher color="primary" />
        </Toolbar>
      </AppBar>
      <Box sx={{ flexGrow: 1, p: 3, display: 'flex', flexDirection: 'column' }}>
        <Outlet />
      </Box>
    </Box>
  );
};
