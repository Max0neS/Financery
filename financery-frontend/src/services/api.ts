const BASE_URL = import.meta.env.REACT_APP_API_URL || 'http://localhost:8080';

const apiCall = async (endpoint: string, options: RequestInit = {}) => {
  try {
    const response = await fetch(`${BASE_URL}${endpoint}`, {
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
      },
      ...options,
    });

    console.log(`API call to ${endpoint} returned status: ${response.status} at ${new Date().toLocaleString('en-US', { timeZone: 'America/Chicago' })}`);

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`HTTP error! status: ${response.status}, message: ${errorText}`);
    }

    // Для DELETE-запросов ожидаем 1 в теле ответа
    if (options.method === 'DELETE') {
      const data = await response.json();
      if (data !== 1) {
        throw new Error('Unexpected response from DELETE request: expected 1');
      }
      return data;
    }

    // Для остальных запросов возвращаем JSON
    return await response.json();
  } catch (error) {
    console.error(`API call failed at ${new Date().toLocaleString('en-US', { timeZone: 'America/Chicago' })}:`, error);
    throw error;
  }
};

export const api = {
  users: {
    getAll: () => apiCall('/users/get-all-users'),
    getById: (id: number) => apiCall(`/users/search-by-id/${id}`),
    create: (userData: any) => apiCall('/users/create', {
      method: 'POST',
      body: JSON.stringify(userData),
    }),
    update: (id: number, userData: any) => apiCall(`/users/update-by-id/${id}`, {
      method: 'PUT',
      body: JSON.stringify(userData),
    }),
    delete: (id: number) => apiCall(`/users/delete-by-id/${id}`, {
      method: 'DELETE',
    }),
  },
  accounts: {
    getAll: () => apiCall('/bills/get-all-bills'),
    getByUserId: (userId: number) => apiCall(`/bills/get-all-user-bills/${userId}`),
    create: (accountData: any) => apiCall('/bills/create', {
      method: 'POST',
      body: JSON.stringify(accountData),
    }),
    update: (id: number, accountData: any) => apiCall(`/bills/update-by-id/${id}`, {
      method: 'PUT',
      body: JSON.stringify(accountData),
    }),
    delete: (id: number) => apiCall(`/bills/delete-by-id/${id}`, {
      method: 'DELETE',
    }),
  },
  transactions: {
    getAll: () => apiCall('/transactions/get-all-transactions'),
    getByUserId: (userId: number) => apiCall(`/transactions/get-all-user-transactions/${userId}`),
    getByAccountId: (accountId: number) => apiCall(`/transactions/get-all-bill-transactions/${accountId}`),
    create: (transactionData: any) => apiCall('/transactions/create', {
      method: 'POST',
      body: JSON.stringify(transactionData),
    }),
    update: (id: number, transactionData: any) => apiCall(`/transactions/update-by-id/${id}`, {
      method: 'PUT',
      body: JSON.stringify(transactionData),
    }),
    delete: (id: number) => apiCall(`/transactions/delete-by-id/${id}`, {
      method: 'DELETE',
    }),
  },
  tags: {
    getAll: () => apiCall('/tags/get-all-tags'),
    getByUserId: (userId: number) => apiCall(`/tags/get-all-user-tags/${userId}`),
    create: (tagData: any) => apiCall('/tags/create', {
      method: 'POST',
      body: JSON.stringify(tagData),
    }),
    update: (id: number, tagData: any) => apiCall(`/tags/update-by-id/${id}`, {
      method: 'PUT',
      body: JSON.stringify(tagData),
    }),
    delete: (id: number) => apiCall(`/tags/delete-by-id/${id}`, {
      method: 'DELETE',
    }),
  },
};