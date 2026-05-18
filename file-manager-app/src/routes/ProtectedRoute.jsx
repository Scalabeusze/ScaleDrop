import { Navigate, Outlet } from 'react-router';
import { useAuth } from '../context/useAuth';

export const ProtectedRoute = () => {
  const { user } = useAuth();

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  return <Outlet />;
};
