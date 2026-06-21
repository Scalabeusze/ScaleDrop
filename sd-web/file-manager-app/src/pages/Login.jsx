import React, { useState } from 'react';
import { Box, Button, TextField, Typography, Paper, Divider } from '@mui/material';
import { useNavigate } from 'react-router';

const Login = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  // Mock login handler
  const handleMockLogin = (e) => {
    e.preventDefault();
    
    localStorage.setItem('userRole', 'user');
    
    localStorage.setItem('isAuthenticated', 'true');
    navigate('/dashboard');
  };

  // Proper OAuth 2.0 redirect handler
  const handleOAuthLogin = () => {
    // Replace with your actual Google OAuth 2.0 Client ID
    const clientId = 'YOUR_GOOGLE_CLIENT_ID'; 
    const redirectUri = encodeURIComponent(window.location.origin + '/oauth-callback');
    const scope = encodeURIComponent('openid email profile');
    const responseType = 'code';
    // State parameter is highly recommended to prevent CSRF attacks
    const state = 'standard_oauth_state_string_mock'; 

    // Construct the Google authorization URL
    const googleAuthUrl = `https://accounts.google.com/o/oauth2/v2/auth?client_id=${clientId}&redirect_uri=${redirectUri}&response_type=${responseType}&scope=${scope}&state=${state}`;

    // Redirect the user to the Google OAuth consent screen
    window.location.href = googleAuthUrl;
  };

  return (
    <Box sx={{ display: 'flex', height: '100vh', justifyContent: 'center', alignItems: 'center', p: 2 }}>
      <Paper elevation={3} sx={{ p: 4, width: '100%', maxWidth: 400, display: 'flex', flexDirection: 'column', gap: 2 }}>
        <Typography variant="h4" component="h1" align="center" gutterBottom>
          Sign In
        </Typography>

        <form onSubmit={handleMockLogin} style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          <TextField
            label="Email"
            type="email"
            variant="outlined"
            fullWidth
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
          <TextField
            label="Password"
            type="password"
            variant="outlined"
            fullWidth
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
          <Button type="submit" variant="contained" color="primary" size="large" fullWidth>
            Mock Login
          </Button>
        </form>

        <Divider sx={{ my: 2 }}>OR</Divider>

        <Button
          variant="outlined"
          color="secondary"
          size="large"
          fullWidth
          onClick={handleOAuthLogin}
        >
          Sign In with Google
        </Button>
      </Paper>
    </Box>
  );
};

export default Login;