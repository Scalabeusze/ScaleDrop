import { createTheme } from '@mui/material/styles';

const lightPalette = {
  mode: 'light',
  primary: {
    main: '#4B75F2',
  },
  secondary: {
    main: '#578BF2',
  },
  background: {
    default: '#C5D0D9',
    paper: '#FFFFFF',
  },
  text: {
    primary: '#5550F2',
    secondary: '#2d4ba6',
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
