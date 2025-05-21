import React, { useState, useEffect } from 'react';
import { getAllUsers, getUserBalance, getUserTransactions } from '../../services/api';
import './MainContent.css';

const MainContent = () => {
    const [users, setUsers] = useState([]);
    const [selectedUserId, setSelectedUserId] = useState(null);
    const [balance, setBalance] = useState(0);
    const [income, setIncome] = useState(0);
    const [expenses, setExpenses] = useState(0);

    useEffect(() => {
        getAllUsers().then(response => setUsers(response.data));
    }, []);

    useEffect(() => {
        if (selectedUserId) {
            getUserBalance(selectedUserId).then(response => {
                setBalance(response.data.balance);
            });
            getUserTransactions(selectedUserId).then(response => {
                const transactions = response.data;
                setIncome(transactions.filter(t => t.type).reduce((sum, t) => sum + t.amount, 0));
                setExpenses(transactions.filter(t => !t.type).reduce((sum, t) => sum + t.amount, 0));
            });
        }
    }, [selectedUserId]);

    return (
        <div className="main-content">
            <select onChange={(e) => setSelectedUserId(e.target.value)} value={selectedUserId || ''}>
                <option value="">Выберите пользователя</option>
                {users.map(user => (
                    <option key={user.id} value={user.id}>{user.name}</option>
                ))}
            </select>
            {selectedUserId && (
                <div className="user-info">
                    <h1>Баланс: {balance} ₽</h1>
                    <div className="stats">
                        <span className="expense">Расходы: {expenses} ₽</span>
                        <span className="income">Доходы: {income} ₽</span>
                    </div>
                </div>
            )}
        </div>
    );
};

export default MainContent;