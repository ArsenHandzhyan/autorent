'use client';

import { useState } from 'react';

export default function ApiTestSection() {
  const [apiResponse, setApiResponse] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const testApi = async (endpoint: string) => {
    setLoading(true);
    setError('');
    setApiResponse('');

    try {
      const response = await fetch(`http://localhost:8080/api${endpoint}`);
      const data = await response.json();
      setApiResponse(JSON.stringify(data, null, 2));
    } catch (err) {
      setError(`Ошибка подключения к API: ${err instanceof Error ? err.message : 'Неизвестная ошибка'}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="bg-gray-100 py-16">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center mb-8">
          <h2 className="text-3xl font-bold text-gray-900 mb-4">Тестирование API</h2>
          <p className="text-lg text-gray-600">Проверьте работу backend API</p>
        </div>
        
        <div className="bg-white rounded-lg shadow-lg p-8 max-w-4xl mx-auto">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
            <button
              onClick={() => testApi('/cars')}
              disabled={loading}
              className="bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400 text-white px-4 py-2 rounded-lg font-semibold transition-colors duration-300"
            >
              Получить автомобили
            </button>
            
            <button
              onClick={() => testApi('/users')}
              disabled={loading}
              className="bg-green-600 hover:bg-green-700 disabled:bg-gray-400 text-white px-4 py-2 rounded-lg font-semibold transition-colors duration-300"
            >
              Получить пользователей
            </button>
          </div>
          
          {loading && (
            <div className="text-center py-4">
              <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
              <p className="mt-2 text-gray-600">Загрузка...</p>
            </div>
          )}
          
          {error && (
            <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
              {error}
            </div>
          )}
          
          {apiResponse && (
            <div className="bg-gray-50 border border-gray-300 rounded-lg p-4">
              <h4 className="text-lg font-semibold mb-2">Ответ API:</h4>
              <pre className="text-sm text-gray-800 overflow-x-auto max-h-96 overflow-y-auto">
                {apiResponse}
              </pre>
            </div>
          )}
        </div>
      </div>
    </section>
  );
} 