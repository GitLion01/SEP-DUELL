import axios from 'axios';

const axiosInstance = axios.create({
  baseURL: '/' // Beispiel-Base-URL, passe sie entsprechend deiner Anwendung an
});

export default axiosInstance;