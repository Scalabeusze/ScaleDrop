import { Box, Typography, Grid, Paper } from '@mui/material';

export const AdminDashboard = () => {
  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Dashboard
      </Typography>
      <Grid container spacing={3}>
        <Grid xs={12} sm={4}>
          <Paper sx={{ p: 3, textAlign: 'center' }}>
            <Typography variant="h6">Total Users</Typography>
            <Typography variant="h3" color="primary">1,024</Typography>
          </Paper>
        </Grid>
        <Grid xs={12} sm={4}>
          <Paper sx={{ p: 3, textAlign: 'center' }}>
            <Typography variant="h6">Total Storage Used</Typography>
            <Typography variant="h3" color="secondary">4.2 TB</Typography>
          </Paper>
        </Grid>
        <Grid xs={12} sm={4}>
          <Paper sx={{ p: 3, textAlign: 'center' }}>
            <Typography variant="h6">Active Sessions</Typography>
            <Typography variant="h3" color="error">42</Typography>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
};
