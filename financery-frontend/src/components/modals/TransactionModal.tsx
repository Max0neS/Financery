import React, { useState, useEffect } from 'react';
import { api } from '@/services/api.ts';

const TransactionModal = ({ isOpen, onClose, type, account, transaction, state, dispatch }) => {
  const [name, setName] = useState('');
  const [amount, setAmount] = useState('');
  const [description, setDescription] = useState('');
  const [date, setDate] = useState('');
  const [selectedTags, setSelectedTags] = useState([]);
  const [isEditing, setIsEditing] = useState(false);

  const { tags, currentUser } = state;

  const formatDateToInput = (dateStr) => {
    if (!dateStr) return '';
    if (dateStr.includes('.')) {
      const [day, month, year] = dateStr.split('.');
      return `${year}-${month.padStart(2, '0')}-${day.padStart(2, '0')}`;
    }
    return dateStr;
  };

  const formatDateToAPI = (dateStr) => {
    if (!dateStr) return '';
    const [year, month, day] = dateStr.split('-');
    return `${day}.${month}.${year}`;
  };

  useEffect(() => {
    if (isOpen) {
      if (transaction) {
        setIsEditing(true);
        setName(transaction.name || '');
        setAmount(Math.abs(transaction.amount).toString());
        setDescription(transaction.description || '');
        setDate(formatDateToInput(transaction.date));
        setSelectedTags(
            transaction.tags && Array.isArray(transaction.tags)
                ? transaction.tags.map((tag) => tag.id || tag)
                : []
        );
      } else {
        setIsEditing(false);
        setName('');
        setAmount('');
        setDescription('');
        setDate(new Date().toISOString().split('T')[0]);
        setSelectedTags([]);
      }
    }
  }, [isOpen, transaction]);

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!name.trim() || !amount || !description.trim()) {
      alert('Пожалуйста, заполните все обязательные поля');
      return;
    }

    const parsedAmount = parseFloat(amount);
    if (isNaN(parsedAmount) || parsedAmount <= 0) {
      alert('Сумма должна быть числом больше 0');
      return;
    }
    if (parsedAmount > 1_000_000) {
      alert('Сумма не может превышать 1,000,000');
      return;
    }

    const formattedDate = formatDateToAPI(date);
    if (!formattedDate.match(/^\d{2}\.\d{2}\.\d{4}$/)) {
      alert('Неверный формат даты');
      return;
    }

    const transactionData = {
      name: name.trim(),
      amount: Math.abs(parseFloat(amount)),
      description: description.trim(),
      date: formattedDate,
      type: isEditing ? (transaction.type === 'income' ? true : false) : type === 'income',
      userId: currentUser.id,
      billId: isEditing ? transaction.accountId : account.id,
      tagIds: selectedTags || [],
    };

    console.log('Отправляемые данные:', transactionData);

    try {
      let updatedData;
      if (isEditing) {
        updatedData = await api.transactions.update(transaction.id, transactionData);
        dispatch({ type: 'UPDATE_TRANSACTION', payload: updatedData });
      } else {
        updatedData = await api.transactions.create(transactionData);
        dispatch({ type: 'ADD_TRANSACTION', payload: updatedData });
      }

      // Получаем обновленные данные с сервера
      const updatedAccounts = await api.accounts.getByUserId(currentUser.id);
      const updatedTransactions = await api.transactions.getTransactionsByUserId(currentUser.id);
      dispatch({ type: 'SET_ACCOUNTS', payload: updatedAccounts });
      dispatch({ type: 'SET_TRANSACTIONS', payload: updatedTransactions });
      onClose();
    } catch (error) {
      console.error('Failed to save transaction:', error);
      alert(`Ошибка при сохранении транзакции: ${error.message}`);
    }
  };

  const handleDelete = async () => {
    if (window.confirm('Вы уверены, что хотите удалить эту транзакцию?')) {
      try {
        await api.transactions.delete(transaction.id);
        dispatch({ type: 'DELETE_TRANSACTION', payload: transaction.id });

        // Получаем обновленные данные с сервера
        const updatedAccounts = await api.accounts.getByUserId(currentUser.id);
        const updatedTransactions = await api.transactions.getTransactionsByUserId(currentUser.id);
        dispatch({ type: 'SET_ACCOUNTS', payload: updatedAccounts });
        dispatch({ type: 'SET_TRANSACTIONS', payload: updatedTransactions });
        onClose();
      } catch (error) {
        console.error('Failed to delete transaction:', error);
        alert('Ошибка при удалении транзакции');
      }
    }
  };

  const toggleTag = (tagId) => {
    setSelectedTags((prev) =>
        prev.includes(tagId) ? prev.filter((id) => id !== tagId) : [...prev, tagId]
    );
  };

  const transactionType = isEditing ? transaction.type : type;

  return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
        <div className="bg-white rounded-lg max-w-md w-full max-h-[90vh] overflow-y-auto">
          <div className="p-4 pl-6 border-b border-gray-200">
            <div className="flex justify-between items-center">
              <h2 className="text-xl font-semibold text-gray-800">
                {isEditing
                    ? 'Редактировать транзакцию'
                    : `Добавить ${transactionType === 'income' ? 'Доход' : 'Расход'}`}
              </h2>
              <button
                  onClick={onClose}
                  className="text-gray-400 hover:text-gray-600 transition-colors"
              >
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>
          </div>

          <form onSubmit={handleSubmit} className="p-4 pl-6 space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Название</label>
              <input
                  type="text"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="Введите название"
                  maxLength={50}
                  required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Сумма</label>
              <input
                  type="number"
                  step="0.01"
                  value={amount}
                  onChange={(e) => setAmount(e.target.value)}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="0.00"
                  required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Описание</label>
              <input
                  type="text"
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="Введите описание"
                  required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Дата</label>
              <input
                  type="date"
                  value={date}
                  onChange={(e) => setDate(e.target.value)}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                  required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Теги</label>
              <div className="flex flex-wrap gap-2 max-h-32 overflow-y-auto">
                {tags.map((tag) => (
                    <button
                        key={tag.id}
                        type="button"
                        onClick={() => toggleTag(tag.id)}
                        className={`px-3 py-1 rounded-full text-sm transition-colors ${
                            selectedTags.includes(tag.id)
                                ? 'bg-[#003464] text-white'
                                : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                        }`}
                    >
                      {tag.title}
                    </button>
                ))}
              </div>
            </div>

            <div className="flex space-x-2 pt-1">
              <button
                  type="submit"
                  className="flex-1 bg-[#003464] text-white py-2 rounded-lg hover:bg-blue-700 transition-colors"
              >
                {isEditing ? 'Обновить' : 'Добавить'} транзакцию
              </button>
              {isEditing && (
                  <button
                      type="button"
                      onClick={handleDelete}
                      className="px-4 bg-red-600 text-white py-2 rounded-lg hover:bg-red-700 transition-colors"
                  >
                    Удалить
                  </button>
              )}
              <button
                  type="button"
                  onClick={onClose}
                  className="px-4 border border-gray-300 text-gray-700 py-2 rounded-lg hover:bg-gray-50 transition-colors"
              >
                Отмена
              </button>
            </div>
          </form>
        </div>
      </div>
  );
};

export default TransactionModal;