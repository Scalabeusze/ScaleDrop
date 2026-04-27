import { Typography, Box, Button, Paper, Divider } from '@mui/material';
import { useNavigate } from 'react-router';
import { useAuth } from '../../context/AuthContext';

export const LoginPage = () => {
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleMockLogin = () => {
    login('user');
    navigate('/user/my-files');
  };

  const handleOAuthLogin = () => {
    // Replace with actual Google OAuth 2.0 Client ID
    const clientId = 'YOUR_GOOGLE_CLIENT_ID'; 
    const redirectUri = encodeURIComponent(window.location.origin + '/oauth-callback');
    const scope = encodeURIComponent('openid email profile');
    const responseType = 'code';
    const state = 'standard_oauth_state_string_mock'; 

    // Construct the Google authorization URL
    const googleAuthUrl = `https://accounts.google.com/o/oauth2/v2/auth?client_id=${clientId}&redirect_uri=${redirectUri}&response_type=${responseType}&scope=${scope}&state=${state}`;

    // Redirect the user to the Google OAuth consent screen
    window.location.href = googleAuthUrl;
  };

  return (
    <Box sx={{ display: 'flex', justifyContent: 'center', mt: 10 }}>
      <Paper sx={{ p: 4, textAlign: 'center', width: '400px' }}>
        <Typography variant="h4" gutterBottom>
          Login
        </Typography>
        <Typography variant="body1" color="text.secondary" paragraph>
          Mock authentication for development.
        </Typography>
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 4 }}>
          <Button variant="contained" color="primary" onClick={() => handleMockLogin('user')}>
            Login as User
          </Button>
          
          <Divider sx={{ my: 1 }}>OR</Divider>

          <Button
            variant="outlined"
            color="primary"
            onClick={handleOAuthLogin}
          >
            Sign In with Google
          </Button>
        </Box>
      </Paper>
    </Box>
  );
};
