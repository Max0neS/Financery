import axios from 'axios';

const API_URL = 'http://localhost:8080'; // URL твоего Spring backend

export const getAllUsers = () => axios.get(`${API_URL}/users/get-all-users`);
export const getUserBalance = (userId) => axios.get(`${API_URL}/users/search-by-id/${userId}`);
export const getUserTransactions = (userId) => axios.get(`${API_URL}/transactions/get-all-user-transactions/${userId}`);
