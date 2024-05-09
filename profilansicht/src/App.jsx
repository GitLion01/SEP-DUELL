import Profile from './components/Profile';
import './index.css'
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Startseite from './startseite';

function App() {
  return (
          <Router>
             <Routes>
              <Route path="/profileinsicht" element={<Profile/>}></Route>
              <Route path="/startseite" element={<Startseite/>}></Route>
            </Routes>
          </Router>
  );
}

export default App;
