import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

const useCheckTurnier = () => {
    const [isTurnierReady, setIsTurnierReady] = useState(false);

    useEffect(() => {
        const checkTurnier = async () => {
            const turnierId = localStorage.getItem('turnierId'); // Assuming the turnierId is stored in localStorage
            if (turnierId) {
                try {
                    const response = await fetch(`http://localhost:8080/checkTurnier?turnierId=${turnierId}`);
                    const result = await response.json();
                    setIsTurnierReady(result);
                } catch (error) {
                    console.error('Error checking turnier:', error);
                }
            }
        };

        checkTurnier();
    }, []);

    return isTurnierReady;
};

export default useCheckTurnier;
