import React, { useState } from 'react';
import { Typography, Box, Button, Paper, Divider, Alert } from '@mui/material';
import { useNavigate } from 'react-router';
import { useAuth } from '../../context/useAuth';
import { GoogleLogin } from '@react-oauth/google';
import { motion } from 'motion/react';
import { useAppSwal } from '../../hooks/useAppSwal';

export const LoginPage = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [errorMsg, setErrorMsg] = useState('');
  const { swal } = useAppSwal();

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
        swal.fire({
          title: 'Login Failed',
          text: errData.message || 'Logowanie w BFF nie powiodło się',
          icon: 'error'
        });
        throw new Error(errData.message || 'Logowanie w BFF nie powiodło się');
      }

      const data = await response.json();
      login(data);
      navigate('/user/my-files');
    } catch (error) {
      console.error('Błąd podczas logowania Google:', error);
      swal.fire({
        title: 'Login Failed',
        text: error.message || 'Logowanie z Google zakończone wewnętrznym błędem paczki',
        icon: 'error'
      });
      setErrorMsg(error.message);
    }
  };

  return (
    <Box 
      component={motion.div} 
      initial={{ opacity: 0, scale: 0.95, y: 20 }} 
      animate={{ opacity: 1, scale: 1, y: 0 }} 
      transition={{ type: 'spring', stiffness: 100, damping: 15 }}
      sx={{ display: 'flex', justifyContent: 'center', mt: 10, px: 2 }}
    >
      <Paper sx={{ p: { xs: 4, md: 6 }, textAlign: 'center', width: '450px', borderRadius: 4, boxShadow: '0 12px 40px rgba(0,0,0,0.08)', border: '1px solid', borderColor: 'divider' }}>
        <Typography variant="h3" gutterBottom sx={{ fontWeight: 800, background: 'linear-gradient(45deg, #1976d2, #9c27b0)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent', mb: 1 }}>
          Welcome Back
        </Typography>
        <Typography variant="body1" color="text.secondary" paragraph sx={{ mb: 4 }}>
          Zaloguj się, aby uzyskać dostęp do swoich plików.
        </Typography>

        {errorMsg && (
          <Alert severity="error" sx={{ mb: 3, borderRadius: 2 }}>
            {errorMsg}
          </Alert>
        )}

        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5, mt: 2 }}>
          <Button variant="contained" color="primary" size="large" onClick={() => handleMockLogin('user')} sx={{ borderRadius: '50px', py: 1.5, fontSize: '1.1rem', boxShadow: '0 8px 16px rgba(25, 118, 210, 0.3)' }}>
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
