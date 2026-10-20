import axios from "axios";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";

const client = axios.create({
  baseURL: API_BASE_URL,
});

// Attach the JWT (if we have one) to every outgoing request.
client.interceptors.request.use((config) => {
  const token = localStorage.getItem("skripty_token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// If the backend says our token is invalid/expired, clear it and send the user to login.
client.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem("skripty_token");
      localStorage.removeItem("skripty_user");
      if (window.location.pathname !== "/login") {
        window.location.href = "/login";
      }
    }
    return Promise.reject(error);
  }
);

export default client;
