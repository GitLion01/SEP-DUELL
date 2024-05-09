import axios from 'axios';

const axiosInstance = axios.create({
  baseURL: 'http://localhost:8080' // Passe die Base-URL entsprechend deiner Anwendung an
});

export default axiosInstance;