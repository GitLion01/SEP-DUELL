import React, { useState } from 'react';
import './Freundeliste.css';

function App() {
    const [friends, setFriends] = useState([
        { name: 'John Doe', completed: false },
        { name: 'Jane Doe', completed: false },
        { name: 'Bob Smith', completed: false },
        { name: 'Alice Johnson', completed: false },
    ]);

    const [searchTerm, setSearchTerm] = useState('');

    const handleSearch = (e) => {
        setSearchTerm(e.target.value);
    };

    const strike = (index) => {
        const newFriends = [...friends];
        newFriends[index].completed = !newFriends[index].completed;
        setFriends(newFriends);
    };

    const sortedFriends = friends.filter((friend) => {
        return friend.name.toLowerCase().includes(searchTerm.toLowerCase());
    }).sort((a, b) => {
        return a.name.localeCompare(b.name);
    });

    return (
        <div className="friend-list">
            <h1>Friend List</h1>
            <input
                type="text"
                value={searchTerm}
                onChange={handleSearch}
                placeholder="Search friends..."
            />
            <ul>
                {sortedFriends.map((friend, index) => (
                    <li
                        key={index}
                        onClick={() => strike(index)}
                        className={friend.completed ? 'is-done' : ''}
                    >
                        {friend.name}
                    </li>
                ))}
            </ul>
            <style>{`
        .is-done {
          text-decoration: line-through;
        }
      `}</style>
        </div>
    );
}

export default App;