import React, { useEffect, useState, useCallback } from 'react';
import './FriendListForChat.css';

function FriendListForChat({ onFriendSelect }) {
  const [friends, setFriends] = useState([]);
  const userId = localStorage.getItem('id');



   // useCallback wird verwendet, um die Funktion fetchFriends zu memoizieren
  // Dies verhindert, dass die Funktion bei jedem Rendern neu erstellt wird, 
  // es sei denn, userId ändert sich 
  const fetchFriends = useCallback(async () => {
    const url = `http://localhost:8080/friendlist/${userId}`;
    const response = await fetch(url);
    if (!response.ok) throw new Error('Network response was not ok');
    return await response.json();
  }, [userId]);
  // Die Funktion hängt von userId ab, sodass sie neu erstellt wird, wenn sich userId ändert


  
  useEffect(() => {
    const fetchData = async () => {
      try {
        const friendsResponse = await fetchFriends();
        setFriends(friendsResponse);
      } catch (error) {
        console.error('Fetch failed:', error.message);
      }
    };

    fetchData();
  }, [fetchFriends]);

  return (
    <div className="friend-list-for-chat">
      <h2>Meine Freunde</h2>
      <ul>
        {friends.map(friend => (
          <li key={friend.id} onClick={() => onFriendSelect(friend)}>
            {friend.username}
          </li>
        ))}
      </ul>
    </div>
  );
}

export default FriendListForChat;
