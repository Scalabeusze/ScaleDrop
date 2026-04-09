import { Box, Typography, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow } from '@mui/material';

const mockLogs = [
  { id: 1, timestamp: '2026-04-06 10:00:01', service: 'AuthService', level: 'INFO', message: 'User Alice logged in successfully.' },
  { id: 2, timestamp: '2026-04-06 10:05:22', service: 'StorageService', level: 'WARNING', message: 'High I/O latency detected on node 3.' },
  { id: 3, timestamp: '2026-04-06 10:12:45', service: 'PaymentService', level: 'ERROR', message: 'Connection to payment gateway failed.' },
];

export const SystemLogs = () => {
  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        System Logs
      </Typography>
      <TableContainer component={Paper} sx={{ mt: 3 }}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>ID</TableCell>
              <TableCell>Timestamp</TableCell>
              <TableCell>Service</TableCell>
              <TableCell>Level</TableCell>
              <TableCell>Message</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {mockLogs.map((log) => (
              <TableRow key={log.id}>
                <TableCell>{log.id}</TableCell>
                <TableCell>{log.timestamp}</TableCell>
                <TableCell>{log.service}</TableCell>
                <TableCell>{log.level}</TableCell>
                <TableCell>{log.message}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
};
