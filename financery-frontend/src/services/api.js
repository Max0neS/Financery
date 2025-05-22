import axios from 'axios';

const API_URL = 'http://localhost:8080';

export const getAllUsers = () => axios.get(`${API_URL}/users/get-all-users`);
export const getUserBalance = (userId) => axios.get(`${API_URL}/users/search-by-id/${userId}`);
export const getUserTransactions = (userId) => axios.get(`${API_URL}/transactions/get-all-user-transactions/${userId}`);
export const getUserBills = (userId) => axios.get(`${API_URL}/bills/get-all-user-bills/${userId}`);
export const getBillBalance = (billId) => axios.get(`${API_URL}/bills/get-bill-balance/${billId}`); // Новый метод
export const getBillTransactions = (billId) => axios.get(`${API_URL}/transactions/get-all-bill-transactions/${billId}`); // Новый метод