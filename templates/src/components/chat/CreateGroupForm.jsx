// components/chat/CreateGroupForm.jsx
import React, { useState, useEffect, useCallback } from 'react';
import './CreateGroupForm.css';

function CreateGroupForm({ onCreateGroup }) {
  const [groupName, setGroupName] = useState('');
  const [selectedFriends, setSelectedFriends] = useState([]);
  const [friends, setFriends] = useState([]);

  const userId = localStorage.getItem('id');

  const fetchFriends = useCallback(async () => {
    const url = `http://localhost:8080/friendlist/${userId}`;
    const response = await fetch(url);
    if (!response.ok) throw new Error('Network response was not ok');
    return await response.json();
  }, [userId]);

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

  const handleFriendToggle = (friend) => {
    setSelectedFriends((prevSelectedFriends) => {
      if (prevSelectedFriends.includes(friend)) {
        return prevSelectedFriends.filter(f => f !== friend);
      } else {
        return [...prevSelectedFriends, friend];
      }
    });
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    if (groupName.trim() !== '' && selectedFriends.length > 0) {
      onCreateGroup({ name: groupName, members: selectedFriends });
      setGroupName('');
      setSelectedFriends([]);
    }
  };

  return (
    <form className="create-group-form" onSubmit={handleSubmit}>
      <h2>Neue Gruppe erstellen</h2>
      <input
        type="text"
        value={groupName}
        onChange={(e) => setGroupName(e.target.value)}
        placeholder="Gruppenname"
        required
      />
      <div className="friend-selection">
        {friends.map(friend => (
          <label key={friend.id}>
            <input
              type="checkbox"
              value={friend.id}
              onChange={() => handleFriendToggle(friend)}
              checked={selectedFriends.includes(friend)}
            />
            {friend.username}
          </label>
        ))}
      </div>
      <button type="submit">Gruppe erstellen</button>
    </form>
  );
}

export default CreateGroupForm;
