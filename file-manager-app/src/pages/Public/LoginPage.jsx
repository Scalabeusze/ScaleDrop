import React, { useState } from 'react';
import { Typography, Box, Button, Paper, Divider, Alert } from '@mui/material';
import { useNavigate } from 'react-router';
import { useAuth } from '../../context/AuthContext';
import { GoogleLogin } from '@react-oauth/google';

export const LoginPage = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [errorMsg, setErrorMsg] = useState('');

  const handleMockLogin = () => {
    login('user');
    navigate('/user/my-files');
  };

  const handleGoogleSuccess = async (credentialResponse) => {
    setErrorMsg('');
    try {
      const apiUrl = import.meta.env.VITE_API_URL;
      const response = await fetch(`${apiUrl}/api/v1/login`, {
        method: 'POST',
        headers: {
          'accept': 'application/json',
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ googleIdToken: credentialResponse.credential })
      });

      if (!response.ok) {
        const errData = await response.json().catch(() => ({}));
        throw new Error(errData.message || 'Logowanie w BFF nie powiodło się');
      }

      const data = await response.json();
      login(data);
      navigate('/user/my-files');
    } catch (error) {
      console.error('Błąd podczas logowania Google:', error);
      setErrorMsg(error.message);
    }
  };

  return (
    <Box sx={{ display: 'flex', justifyContent: 'center', mt: 10 }}>
      <Paper sx={{ p: 4, textAlign: 'center', width: '400px' }}>
        <Typography variant="h4" gutterBottom>
          Login
        </Typography>
        <Typography variant="body1" color="text.secondary" paragraph>
          Zaloguj się, aby uzyskać dostęp do swoich plików.
        </Typography>

        {errorMsg && (
          <Alert severity="error" sx={{ mb: 3 }}>
            {errorMsg}
          </Alert>
        )}

        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 4 }}>
          <Button variant="contained" color="primary" onClick={() => handleMockLogin('user')}>
            Login as User
          </Button>
          
          <Divider sx={{ my: 1 }}>OR</Divider>

          <Box sx={{ display: 'flex', justifyContent: 'center' }}>
            <GoogleLogin
              onSuccess={handleGoogleSuccess}
              onError={() => setErrorMsg('Logowanie z Google zakończone wewnętrznym błędem paczki')}
            />
          </Box>
        </Box>
      </Paper>
    </Box>
  );
};
