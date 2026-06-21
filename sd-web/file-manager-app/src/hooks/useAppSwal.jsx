import { useMemo, useContext } from 'react';
import Swal from 'sweetalert2';
import { ThemeContext } from '../context/themeContext';
import { useTheme } from '@mui/material/styles';

export const useAppSwal = () => {
  const { isDarkMode } = useContext(ThemeContext);
  const muiTheme = useTheme();

  return useMemo(() => {
    const baseConfig = {
      // Tell SweetAlert2 the desired theme
      theme: isDarkMode ? 'dark' : 'light',
      background: muiTheme.palette.background.paper,
      color: muiTheme.palette.text.primary,
      confirmButtonColor: muiTheme.palette.primary.main,
      cancelButtonColor: muiTheme.palette.error.main,
    };

    return {
      swal: Swal.mixin(baseConfig),
      toast: Swal.mixin({
        ...baseConfig,
        toast: true,
        position: 'top-end',
        showConfirmButton: false,
        timer: 3000,
        timerProgressBar: true,
      })
    };
  }, [isDarkMode, muiTheme]);
};