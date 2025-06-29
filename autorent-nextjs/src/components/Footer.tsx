'use client';

export default function Footer() {
  return (
    <footer className="bg-gray-900 text-white">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          <div>
            <h3 className="text-xl font-bold mb-4">AutoRent</h3>
            <p className="text-gray-400">
              Ваш надежный партнер в аренде автомобилей
            </p>
          </div>
          
          <div>
            <h4 className="text-lg font-semibold mb-4">Услуги</h4>
            <ul className="space-y-2 text-gray-400">
              <li>Аренда автомобилей</li>
              <li>Доставка</li>
              <li>Страхование</li>
              <li>Техподдержка</li>
            </ul>
          </div>
          
          <div>
            <h4 className="text-lg font-semibold mb-4">Поддержка</h4>
            <ul className="space-y-2 text-gray-400">
              <li>Помощь</li>
              <li>FAQ</li>
              <li>Контакты</li>
              <li>Обратная связь</li>
            </ul>
          </div>
          
          <div>
            <h4 className="text-lg font-semibold mb-4">Контакты</h4>
            <div className="space-y-2 text-gray-400">
              <p>+7 (999) 123-45-67</p>
              <p>info@autorent.ru</p>
              <p>Москва, ул. Примерная, 1</p>
            </div>
          </div>
        </div>
        
        <div className="border-t border-gray-800 mt-8 pt-8 text-center text-gray-400">
          <p>&copy; 2024 AutoRent. Все права защищены.</p>
        </div>
      </div>
    </footer>
  );
} 