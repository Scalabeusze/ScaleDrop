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
    // Redirect the user to the backend's OAuth2 authorization endpoint.
    window.location.href = 'http://sd-alb-dev-1442333574.eu-north-1.elb.amazonaws.com/sd-iam/api/v1/session/google';
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
