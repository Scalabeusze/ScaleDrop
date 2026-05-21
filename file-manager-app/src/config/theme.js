import { createTheme } from '@mui/material/styles';

const lightPalette = {
  mode: 'light',
  primary: {
    main: '#4B75F2',
  },
  secondary: {
    main: '#0EA5E9',
  },
  background: {
    default: '#F1F5F9',
    paper: '#FFFFFF',
  },
  text: {
    primary: '#1E293B',
    secondary: '#64748B',
  },
};

const darkPalette = {
  mode: 'dark',
  primary: {
    main: '#6B8DF2',
  },
  secondary: {
    main: '#38BDF8',
  },
  info: {
    main: '#91CCD9',
  },
  background: {
    default: '#0B0B0D',
    paper: '#1E293B',
  },
  text: {
    primary: '#E2E8F0',
    secondary: '#94A3B8',
  },
};

export const lightTheme = createTheme({
  palette: lightPalette,
  typography: {
    htmlFontSize: 16,
  },
});

export const darkTheme = createTheme({
  palette: darkPalette,
  typography: {
    htmlFontSize: 16,
  },
});
