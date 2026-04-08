import React from 'react';
import { Typography, Box, Button } from '@mui/material';
import { Link } from 'react-router';

export const LandingPage = () => {
  return (
    <Box sx={{ textAlign: 'center', mt: 10 }}>
      <Typography variant="h2" gutterBottom color="primary">
        Welcome to FileManager Cloud
      </Typography>
      <Typography variant="h5" color="text.secondary" paragraph>
        Securely store, encrypt, and share your files with ease.
      </Typography>
      <Button variant="contained" size="large" component={Link} to="/login" sx={{ mt: 2 }}>
        Get Started
      </Button>
    </Box>
  );
};
