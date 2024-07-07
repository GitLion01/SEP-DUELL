import React, { useEffect, useState } from 'react';
import { Navigate } from 'react-router-dom';
import useCheckTurnier from './components/Turnier/checkTurnier';

const ProtectedRoute = ({ element: Component, requiredRole, checkTurnier, ...rest }) => {
    const isAuthenticated = localStorage.getItem('id') !== null; 
    const userRole = localStorage.getItem('userRole'); 
    const clanId = localStorage.getItem('clanId'); 
    const [isTurnierReady, setIsTurnierReady] = useState(true);
    const [isLoading, setIsLoading] = useState(true);

    const turnierReady = useCheckTurnier(clanId);

    useEffect(() => {
        if (checkTurnier) {
            setIsTurnierReady(turnierReady);
            setIsLoading(false);
        } else {
            setIsLoading(false);
        }
    }, [checkTurnier, turnierReady]);

    if (!isAuthenticated) {
        return <Navigate to="/" />;
    }

    if (requiredRole && userRole !== requiredRole) {
        return <Navigate to="/startseite" />;
    }

    if (checkTurnier && isLoading) {
        return <div>Loading...</div>;
    }

    if (checkTurnier && !isTurnierReady) {
        return <Navigate to="/startseite" />;
    }

    return <Component {...rest} />;
};

export default ProtectedRoute;
