import { createTheme } from '@mui/material/styles';

const lightPalette = {
  mode: 'light',
  primary: {
    main: '#5550F2',
  },
  secondary: {
    main: '#04BF9D',
  },
  error: {
    main: '#F2B33D',
  },
  background: {
    default: '#F2F2F2',
    paper: '#FFFFFF',
  },
  text: {
    primary: '#027368',
    secondary: '#5550F2',
  },
};

const darkPalette = {
  mode: 'dark',
  primary: {
    main: '#6B8DF2',
  },
  secondary: {
    main: '#5A73BF',
  },
  info: {
    main: '#91CCD9',
  },
  background: {
    default: '#0B0B0D',
    paper: '#2F3940',
  },
  text: {
    primary: '#91CCD9',
    secondary: '#6B8DF2',
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
