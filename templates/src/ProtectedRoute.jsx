import React from 'react'; 
import { Navigate } from 'react-router-dom';

const ProtectedRoute = ({element: Component, requiredRole, ...rest}) => {
    const isAuthenticated = localStorage.getItem('id') !== null; 
    const userRole = localStorage.getItem('userRole'); 

    if (!isAuthenticated) {
        return <Navigate to="/"/>;
    }

    if (requiredRole && userRole !== requiredRole) {
        return <Navigate to="/startseite"/>;
    }

    return <Component {...rest} /> 

}; 

export default ProtectedRoute; 