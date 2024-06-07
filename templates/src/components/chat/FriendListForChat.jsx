import React, { useEffect, useState, useCallback } from 'react';
import './FriendListForChat.css';

function FriendListForChat({ onSelect, onCreateGroupClick, }) {
  const [friends, setFriends] = useState([]);
  const [groups, setGroups] = useState([]);
  const userId = localStorage.getItem('id');

  const fetchFriends = useCallback(async () => {
    const url = `http://localhost:8080/friendlist/${userId}`;
    const response = await fetch(url);
    if (!response.ok) throw new Error('Network response was not ok');
    return await response.json();
  }, [userId]);

  const fetchGroups = useCallback(async () => {
    const url = `http://localhost:8080/grouplist/${userId}`;
    const response = await fetch(url);
    if (!response.ok) throw new Error('Network response was not ok');
    return await response.json();
  }, [userId]);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const friendsResponse = await fetchFriends();
        setFriends(friendsResponse);
        const groupsResponse = await fetchGroups();
        setGroups(groupsResponse);
      } catch (error) {
        console.error('Fetch failed:', error.message);
      }
    };

    fetchData();
  }, [fetchFriends, fetchGroups]);

  return (
    <div className="friend-list-for-chat">
      <div className="friend-list-section">
        <h2>Meine Freunde</h2>
        <ul>
          {friends.map(friend => (
            <li key={friend.id} onClick={() => onSelect(friend, 'friend')}>
              {friend.username}
            </li>
          ))}
        </ul>
      </div>
      <div className="friend-list-section">
        <h2>Meine Gruppen</h2>
        <ul>
          {groups.map(group => (
            <li key={group.id} onClick={() => onSelect(group, 'group')}>
              {group.name}
            </li>
          ))}
        </ul>
        <button onClick={onCreateGroupClick}>Gruppe erstellen</button>
      </div>
    </div>
  );
}

export default FriendListForChat;
