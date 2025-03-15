import { useEffect, useState } from 'react';

const useCheckTurnier = (clanId) => {
    const [isTurnierReady, setIsTurnierReady] = useState(false);
    const [turnierId, setTurnierId] = useState(null);

    useEffect(() => {
        const fetchTurnierId = async () => {
            if (clanId) {
                try {
                    const response = await fetch(`http://localhost:8080/getTurnierId?clanId=${clanId}`);
                    const id = await response.json();
                    setTurnierId(id);
                } catch (error) {
                    console.error('Error fetching turnierId:', error);
                }
            }
        };

        fetchTurnierId();
    }, [clanId]);

    useEffect(() => {
        const checkTurnier = async () => {
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
    }, [turnierId]);

    return isTurnierReady;
};

export default useCheckTurnier;
