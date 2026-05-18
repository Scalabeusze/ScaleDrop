import { Typography, Box, Button } from '@mui/material';
import { Link } from 'react-router';
import { motion } from 'motion/react';

const containerVariants = {
  hidden: { opacity: 0 },
  visible: {
    opacity: 1,
    transition: {
      staggerChildren: 0.2,
      delayChildren: 0.1,
    },
  },
};

const itemVariants = {
  hidden: { opacity: 0, y: 30 },
  visible: { 
    opacity: 1, 
    y: 0, 
    transition: { type: 'spring', stiffness: 100, damping: 10 } 
  },
};

export const LandingPage = () => {
  return (
    <Box 
      component={motion.div}
      variants={containerVariants}
      initial="hidden"
      animate="visible"
      sx={{ textAlign: 'center', mt: 10, flexGrow: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}
    >
      <motion.div variants={itemVariants}>
        <Typography variant="h1" gutterBottom color="primary" sx={{ fontWeight: 900, letterSpacing: '-0.02em', background: 'linear-gradient(45deg, #1976d2, #9c27b0)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
          ScaleDrop FM
        </Typography>
      </motion.div>
      <motion.div variants={itemVariants}>
        <Typography variant="h4" color="text.secondary" paragraph sx={{ maxWidth: '600px', mx: 'auto', mb: 4 }}>
          Securely store, encrypt, and share your files with ease. Advanced protection for your digital life.
        </Typography>
      </motion.div>
      <motion.div variants={itemVariants} whileHover={{ scale: 1.05 }} whileTap={{ scale: 0.95 }}>
        <Button variant="contained" size="large" component={Link} to="/login" sx={{ mt: 2, px: 5, py: 1.5, borderRadius: '50px', fontSize: '1.2rem', boxShadow: '0 8px 16px rgba(25, 118, 210, 0.4)' }}>
          Get Started
        </Button>
      </motion.div>
    </Box>
  );
};

