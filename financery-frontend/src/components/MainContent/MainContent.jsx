import React, { useState, useEffect } from 'react';
import { Button } from 'antd';
import { getAllUsers, getBillBalance, getBillTransactions, getUserBills } from '../../services/api';
import './MainContent.css';

const MainContent = () => {
    const [users, setUsers] = useState([]);
    const [selectedUserId, setSelectedUserId] = useState(null);
    const [selectedBillIndex, setSelectedBillIndex] = useState(0);
    const [balance, setBalance] = useState(0);
    const [income, setIncome] = useState(0);
    const [expenses, setExpenses] = useState(0);
    const [bills, setBills] = useState([]);
    const [error, setError] = useState(null);
    const [showSettingsModal, setShowSettingsModal] = useState(false);

    useEffect(() => {
        getAllUsers()
            .then(response => setUsers(response.data))
            .catch(err => {
                console.error('Ошибка при загрузке пользователей:', err);
                setError('Не удалось загрузить список пользователей.');
            });
    }, []);

    useEffect(() => {
        if (selectedUserId) {
            getUserBills(selectedUserId)
                .then(response => setBills(response.data))
                .catch(err => setError('Не удалось загрузить счета.'));
        }
    }, [selectedUserId]);

    useEffect(() => {
        if (selectedUserId && bills.length > 0) {
            const billId = bills[selectedBillIndex]?.id;
            if (billId) {
                getBillBalance(billId) // Используем getBillBalance вместо getUserBalance
                    .then(response => setBalance(response.data.balance))
                    .catch(err => setError('Не удалось загрузить баланс.'));
                getBillTransactions(billId) // Используем getBillTransactions вместо getUserTransactions
                    .then(response => {
                        const transactions = response.data;
                        setIncome(transactions.filter(t => t.type).reduce((sum, t) => sum + t.amount, 0));
                        setExpenses(transactions.filter(t => !t.type).reduce((sum, t) => sum + t.amount, 0));
                    })
                    .catch(err => setError('Не удалось загрузить транзакции.'));
            }
        }
    }, [selectedUserId, selectedBillIndex, bills]);

    const handleRefresh = () => {
        if (selectedUserId && bills.length > 0) {
            const billId = bills[selectedBillIndex].id;
            getBillBalance(billId).then(response => setBalance(response.data.balance));
            getBillTransactions(billId).then(response => {
                const transactions = response.data;
                setIncome(transactions.filter(t => t.type).reduce((sum, t) => sum + t.amount, 0));
                setExpenses(transactions.filter(t => !t.type).reduce((sum, t) => sum + t.amount, 0));
            });
        }
    };

    const handlePrevBill = () => {
        if (selectedBillIndex > 0) {
            setSelectedBillIndex(selectedBillIndex - 1);
        }
    };

    const handleNextBill = () => {
        if (selectedBillIndex < bills.length - 1) {
            setSelectedBillIndex(selectedBillIndex + 1);
        } else {
            setSelectedBillIndex(0); // Возвращаемся к началу и показываем кнопку создания
        }
    };

    return (
        <div className="main-content">
            <select onChange={(e) => {
                setSelectedUserId(e.target.value);
                setSelectedBillIndex(0); // Сбрасываем индекс счета при смене пользователя
            }} value={selectedUserId || ''}>
                <option value="">Выберите пользователя</option>
                {users.map(user => (
                    <option key={user.id} value={user.id}>{user.name}</option>
                ))}
            </select>
            {error && <p style={{ color: 'red' }}>{error}</p>}
            {selectedUserId && bills.length > 0 && (
                <div className="bill-card">
                    <button className="nav-button prev" onClick={handlePrevBill}>←</button>
                    <div className="card-content">
                        <h2>{bills[selectedBillIndex]?.name || 'Название счета'}</h2>
                        <h1>{balance} BYN</h1>
                        <div className="stats">
                            <span className="income">Доходы: {income} BYN</span>
                            <span className="expense">Расходы: {expenses} BYN</span>
                        </div>
                        <div className="buttons">
                            <Button type="primary" onClick={handleRefresh} style={{ marginRight: 10 }}>
                                Обновить
                            </Button>
                            <Button onClick={() => setShowSettingsModal(true)}>
                                {/* [REPLACE_WITH_SETTINGS_ICON] - Замени на изображение шестеренки */}
                                <div style={{ width: 20, height: 20, background: 'gray' }} />
                            </Button>
                        </div>
                    </div>
                    <button className="nav-button next" onClick={handleNextBill}>→</button>
                </div>
            )}
            {selectedUserId && bills.length === 0 && (
                <Button type="primary" onClick={() => console.log('Создать новый счет')}>
                    + {/* [REPLACE_WITH_PLUS_ICON] - Замени на изображение плюса */}
                </Button>
            )}
            {showSettingsModal && (
                <div className="modal">
                    <div className="modal-content">
                        <button onClick={() => setShowSettingsModal(false)}>X</button>
                    </div>
                </div>
            )}
        </div>
    );
};

export default MainContent;