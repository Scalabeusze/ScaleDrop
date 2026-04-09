import { Navigate, Outlet } from 'react-router';
import { useAuth } from '../context/AuthContext';

export const ProtectedRoute = ({ allowedRoles }) => {
  const { user } = useAuth();

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    // If logged in but not correct role, redirect to their home
    return <Navigate to={user.role === 'admin' ? '/admin/dashboard' : '/user/my-files'} replace />;
  }

  return <Outlet />;
};
