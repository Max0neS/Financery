import React from 'react';
import './Header.css';

const Header = () => {
    return (
        <header className="header">
            <div className="logo">Financery</div>
            <div className="menu-toggle">☰</div> {/* Простой бургер */}
        </header>
    );
};

export default Header;