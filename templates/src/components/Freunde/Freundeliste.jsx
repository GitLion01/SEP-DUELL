import React, { useState, useEffect } from 'react';
import Modal from 'react-modal';
import './Freundeliste.css';
import '../ProfileForFriends/ProfileFriends.css'
import BackButton from '../BackButton';
Modal.setAppElement('#root'); // Set the root element for accessibility

function App() {
  const [users, setUsers] = useState([]);
  const [friends, setFriends] = useState([]);
  const [selectedFriend, setSelectedFriend] = useState(null); // State for selected friend
  const [selectedFriendFriends, setSelectedFriendFriends] = useState([]); 
  const [modalIsOpen, setModalIsOpen] = useState(false); // State for modal open/close
  const [isChecked, setIsChecked] = useState(false);
  const [friendslistPrivacy, setFriendslistPrivacy] = useState(false); // State for friendslist privacy

    useEffect(() => {
    fetchUsers();
    fetchFriends();
    setCheckBox();
    const userId = localStorage.getItem('id'); // Get user ID from local storage

  console.log("Current user ID:", userId);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [friendsResponse, usersResponse] = await Promise.all([
          fetchFriends(),
          fetchUsers()
        ]);

        setFriends(friendsResponse);

        const filteredUsers =
            usersResponse.filter(user =>
                user.id.toString() !== userId.toString() &&
                !friendsResponse.some(friend => friend.id.toString() === user.id.toString())
            );

        setUsers(filteredUsers);
      } catch (error) {
        console.error('Fetch failed:', error.message);
      }
    };

    fetchData();
  }, []);

  const fetchUsers = async () => {
    try {
      const url = `http://localhost:8080/registration/users`;
      const response = await fetch(url);
      if (!response.ok) {
        throw new Error('Network response was not ok');
      }
      const data = await response.json();
      setUsers(data);
    } catch (error) {
      console.error('Fetch failed:', error.message);
    }
  };

  const fetchFriends = async () => {
    try {
      const url = `http://localhost:8080/friendlist/${userId}`;
      const response = await fetch(url);
      if (!response.ok) {
        throw new Error('Network response was not ok');
      }
      const data = await response.json();
      setFriends(data);
    } catch (error) {
      console.error('Fetch failed:', error.message);
    }
  };

  const sendFriendRequest = (friendId) => {
    const url = 'http://localhost:8080/friendlist/add';
    const requestData = {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify([userId, friendId])
    };

    fetch(url, requestData)
      .then(response => {
        if (!response.ok) {
          throw new Error('Network response was not ok');
        }
        console.log(`Freundschaftsanfrage an ${friendId} gesendet`);
        alert('Freundschaftsanfrage wurde gesendet')


      })
      .catch(error => {
        console.error('There was a problem with the network request:', error);
      });
  };

  const fetchFriendsOfFriend = async (userId, friendId) => {
    try {
      const url = `http://localhost:8080/friendlist/${userId}/friends/${friendId}`;
      const response = await fetch(url);
      if (!response.ok) {
        if (response.status === 403) {
          console.warn('Zugriff auf die Freundesliste dieses Freundes ist nicht erlaubt.');
          setSelectedFriendFriends([]);
          return;
        }
        throw new Error('Network response was not ok');
      }
      const data = await response.json();
      setSelectedFriendFriends(data);
    } catch (error) {
      console.error('Fetch failed:', error.message);
      setSelectedFriendFriends([]);
    }
  };


  const removeFriend = (friendId) => {
    const url = 'http://localhost:8080/friendlist/remove';
    const requestData = {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify([userId, friendId])
    };

    fetch(url, requestData)
        .then(response => {
          if (!response.ok) {
            throw new Error('Network response was not ok');
          }
          return response.json();
        })
        .then(data => {
          console.log('Backend response:', data);
          const updatedFriends = friends.filter(f => f.id !== friendId);
          setFriends(updatedFriends);
          console.log(`Freund mit ID ${friendId} wurde aus der Freundesliste entfernt`);
          alert('Freund wurde aus der Freundesliste entfernt');
        })
        .catch(error => {
          console.error('There was a problem with the network request:', error);
        });
  };

  const openModal = (friend) => {
    setSelectedFriend(friend);
    fetchFriendsOfFriend(userId,friend.id);
    setModalIsOpen(true);
  };

  const closeModal = () => {
    setSelectedFriend(null);
    setModalIsOpen(false);
    setSelectedFriendFriends([]);
  };

  const handleCheckboxChange = (event) => {
      const url = `http://localhost:8080/friendlist/setFriendslistPrivacy?userId=${userId}`;
      //to update the value of isChecked
      const newIsChecked = event.target.checked; // Get the new checkbox value
      setIsChecked(newIsChecked); // Update the isChecked state
      fetch(url, {
        method: 'PUT',
      })
          .then(response => {
            if (!response.ok) {
              throw new Error('Network response was not ok');
            }
            console.log('Privacy setting updated');
          })
          .catch((error) => {
            console.error('Error:', error);
          });
  };

  const setCheckBox = () => {
    const url = `http://localhost:8080/friendlist/getFriendslistPrivacy?userId=${userId}`;
    fetch(url, {
      method: 'GET',
    })
        .then(response => response.json())
        .then(data=>
            {setIsChecked(data)
            })
        .catch((error) => {
      console.error('Error:', error);
    },[]); // Empty dependency array means this effect will only run once, when the component mounts
  };


  return (
    <div className="BFF">
      <BackButton />
      <div className="UserList-hülle">
        <h2>Nutzerliste</h2>
        <div className="UserList">
          <ul>
            {users.map(user => (
              <li key={user.id}>
                {user.username}
                <button onClick={() => sendFriendRequest(user.id)}>Add</button>
              </li>
            ))}
          </ul>
        </div>
      </div>
      <div className="FriendList-hülle">
        <h2>Meine Freunde</h2>
        <div className="FriendList">
          <ul>
            {friends.map(friend => (
                <li key={friend.id}>
                  <span onClick={() => openModal(friend)}
                        style={{cursor: 'pointer', color: 'blue'}}>{friend.username}</span>
                  <button onClick={() => removeFriend(friend.id)}>Freund entfernen</button>
                </li>
            ))}
          </ul>
        </div>
        <div className="form-check form-switch">
          <input className="form-check-input" type="checkbox" role="switch" id="flexSwitchCheckDefault"
                 onChange={handleCheckboxChange} checked={isChecked}/>
          <label className="form-check-label" htmlFor="flexSwitchCheckDefault">Privat</label>
        </div>
      </div>

      <Modal
          isOpen={modalIsOpen}
          onRequestClose={closeModal}
          contentLabel="Friend Profile"
          className="Modal"
          overlayClassName="Overlay"
      >
        {selectedFriend && (
            <div className={'profile-container'}>
              <div className={'profile-header'}>
              <h2>Profil von {selectedFriend.username}</h2>
            </div>
            <div className={'profile-info'}>
              <img className={'profile-picture'} src={`data:image/jpeg;base64,${selectedFriend.image}`} alt="Profilbild" />
              <p><strong>Benutzername:</strong> {selectedFriend.username}</p>
              <p><strong>Vorname:</strong> {selectedFriend.firstName}</p>
              <p><strong>Nachname:</strong> {selectedFriend.lastName}</p>
              <p><strong>Leaderboard-Punkte:</strong> {selectedFriend.leaderboardPoints}</p>
            </div>
            <div className="friends-of-friend">
              <h4>Freunde von {selectedFriend.username}</h4>
              <div className="friends-list">
                <ul>
                  {selectedFriendFriends.map(friend => (
                    <li key={friend.id}>{friend.username}</li>
                  ))}
                </ul>
              </div>
            </div>
            <button onClick={closeModal}>Schließen</button>
          </div>
        )}
      </Modal>
    </div>
  );
}

export default App;
