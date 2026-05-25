import { createTheme } from '@mui/material/styles';

const baseTypography = {
  fontFamily: "system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif",
  htmlFontSize: 16,
};

const lightPalette = {
  mode: 'light',
  primary: {
    main: '#2563EB', // Blue 600
    light: '#60A5FA', // Blue 400
    dark: '#1D4ED8', // Blue 700
    contrastText: '#FFFFFF',
  },
  secondary: {
    main: '#0EA5E9', // Sky 500
    light: '#38BDF8', // Sky 400
    dark: '#0369A1', // Sky 700
    contrastText: '#FFFFFF',
  },
  success: {
    main: '#10B981', // Emerald 500
  },
  warning: {
    main: '#F59E0B', // Amber 500
  },
  error: {
    main: '#EF4444', // Red 500
  },
  info: {
    main: '#3B82F6', // Blue 500
  },
  background: {
    default: '#F8FAFC', // Slate 50
    paper: '#FFFFFF',
  },
  text: {
    primary: '#0F172A', // Slate 900
    secondary: '#475569', // Slate 600
    disabled: '#94A3B8', // Slate 400
  },
  divider: '#E2E8F0', // Slate 200
  gradients: {
    primary: 'linear-gradient(45deg, #2563EB, #8B5CF6)',
    avatar: 'linear-gradient(135deg, #2563EB, #8B5CF6)',
  },
  customShadows: {
    button: '0 4px 10px rgba(37, 99, 235, 0.2)',
    buttonHover: '0 8px 16px rgba(37, 99, 235, 0.4)',
    paper: '0 12px 40px rgba(0,0,0,0.08)',
    paperLight: '0 8px 24px rgba(0,0,0,0.05)',
  }
};

const darkPalette = {
  mode: 'dark',
  primary: {
    main: '#3B82F6', // Blue 500
    light: '#60A5FA', // Blue 400
    dark: '#2563EB', // Blue 600
    contrastText: '#FFFFFF',
  },
  secondary: {
    main: '#38BDF8', // Sky 400
    light: '#7DD3FC', // Sky 300
    dark: '#0284C7', // Sky 600
    contrastText: '#0F172A',
  },
  success: {
    main: '#34D399', // Emerald 400
  },
  warning: {
    main: '#FBBF24', // Amber 400
  },
  error: {
    main: '#F87171', // Red 400
  },
  info: {
    main: '#7DD3FC', // Sky 300
  },
  background: {
    default: '#0F172A', // Slate 900
    paper: '#1E293B', // Slate 800
  },
  text: {
    primary: '#F8FAFC', // Slate 50
    secondary: '#94A3B8', // Slate 400
    disabled: '#475569', // Slate 600
  },
  divider: '#334155', // Slate 700
  gradients: {
    primary: 'linear-gradient(45deg, #60A5FA, #A78BFA)',
    avatar: 'linear-gradient(135deg, #60A5FA, #A78BFA)',
  },
  customShadows: {
    button: '0 4px 10px rgba(0, 0, 0, 0.4)',
    buttonHover: '0 8px 16px rgba(0, 0, 0, 0.6)',
    paper: '0 12px 40px rgba(0,0,0,0.5)',
    paperLight: '0 8px 24px rgba(0,0,0,0.3)',
  }
};

const components = {
  MuiButton: {
    styleOverrides: {
      root: {
        textTransform: 'none',
        fontWeight: 500,
        borderRadius: '8px',
        boxShadow: 'none',
        '&:hover': {
          boxShadow: 'none',
        },
      },
    },
  },
  MuiCard: {
    styleOverrides: {
      root: {
        borderRadius: '12px',
        boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1)',
      },
    },
  },
  MuiPaper: {
    styleOverrides: {
      root: {
        backgroundImage: 'none',
      },
    },
  },
};

export const lightTheme = createTheme({
  palette: lightPalette,
  typography: baseTypography,
  components,
});

export const darkTheme = createTheme({
  palette: darkPalette,
  typography: baseTypography,
  components: {
    ...components,
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: '12px',
          boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.5), 0 2px 4px -2px rgb(0 0 0 / 0.5)',
          border: '1px solid #334155',
        },
      },
    },
  },
});
