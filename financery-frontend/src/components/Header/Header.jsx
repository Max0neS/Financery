import React, { useState } from 'react';
import { Button } from 'antd';
import './Header.css';

const Header = () => {
    const [showAccountModal, setShowAccountModal] = useState(false);
    const [showSwitchAccountModal, setShowSwitchAccountModal] = useState(false);
    const [showCreateAccountModal, setShowCreateAccountModal] = useState(false);

    const user = { name: 'Name', email: 'e-mail@gmail.com' };
    const [accounts] = useState([
        { name: 'NAME', email: 'e-mail@gmail.com' },
        { name: 'NAME1', email: 'e-mail@gmail.com' },
        { name: 'NAME2', email: 'e-mail@gmail.com' },
    ]);
    const [newAccount, setNewAccount] = useState({ name: '', email: '' });

    const handleEditAccount = () => console.log('Редактировать аккаунт');
    const handleDeleteAccount = () => console.log('Удалить аккаунт');
    const handleSwitchAccount = () => setShowSwitchAccountModal(true);
    const handleCreateAccount = () => setShowCreateAccountModal(true);

    const handleSubmitCreateAccount = () => {
        console.log('Создан аккаунт:', newAccount);
        setShowCreateAccountModal(false);
        setNewAccount({ name: '', email: '' });
    };

    return (
        <header className="header">
            <div className="logo">Financery</div>
            <div className="account" onClick={() => setShowAccountModal(true)}>
                <div style={{ width: 40, height: 40, background: 'gray', borderRadius: '50%' }} />
                <span>Hello {user.name}</span>
            </div>
            {showAccountModal && (
                <div className="modal">
                    <div className="modal-content">
                        <button onClick={() => setShowAccountModal(false)}>X</button>
                        <h2>{user.name}</h2>
                        <p>{user.email}</p>
                        <Button onClick={handleEditAccount}>Редактировать аккаунт</Button>
                        <Button onClick={handleDeleteAccount} style={{ marginLeft: 10 }}>Удалить аккаунт</Button>
                        <Button onClick={handleSwitchAccount} style={{ marginLeft: 10 }}>Сменить аккаунт</Button>
                    </div>
                </div>
            )}
            {showSwitchAccountModal && (
                <div className="modal">
                    <div className="modal-content">
                        <button onClick={() => setShowSwitchAccountModal(false)}>X</button>
                        <div className="account-list">
                            {accounts.map((acc, index) => (
                                <div key={index} className="account-item">
                                    <div style={{ width: 40, height: 40, background: 'gray', borderRadius: '50%' }} />
                                    <div>
                                        <h3>{acc.name}</h3>
                                        <p>{acc.email}</p>
                                    </div>
                                </div>
                            ))}
                            <Button type="primary" onClick={handleCreateAccount}>
                                + {/* [REPLACE_WITH_PLUS_ICON] - Замени на изображение плюса */}
                            </Button>
                        </div>
                    </div>
                </div>
            )}
            {showCreateAccountModal && (
                <div className="modal">
                    <div className="modal-content">
                        <button onClick={() => setShowCreateAccountModal(false)}>X</button>
                        <h2>Имя</h2>
                        <input
                            type="text"
                            value={newAccount.name}
                            onChange={(e) => setNewAccount({ ...newAccount, name: e.target.value })}
                            placeholder="Имя"
                        />
                        <h2>Почта</h2>
                        <input
                            type="text"
                            value={newAccount.email}
                            onChange={(e) => setNewAccount({ ...newAccount, email: e.target.value })}
                            placeholder="Почта"
                        />
                        <Button type="primary" onClick={handleSubmitCreateAccount} style={{ marginTop: 20 }}>
                            Создать аккаунт
                        </Button>
                    </div>
                </div>
            )}
        </header>
    );
};

export default Header;