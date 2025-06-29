'use client';

export default function CarCatalog() {
  const cars = [
    {
      id: 1,
      name: "Toyota Camry",
      type: "Седан",
      price: "2500 ₽/день",
      image: "/api/placeholder/300/200",
      features: ["Автомат", "Кондиционер", "5 мест"]
    },
    {
      id: 2,
      name: "Honda CR-V",
      type: "SUV",
      price: "3500 ₽/день",
      image: "/api/placeholder/300/200",
      features: ["Полный привод", "7 мест", "Круиз-контроль"]
    },
    {
      id: 3,
      name: "BMW 3 Series",
      type: "Седан",
      price: "4500 ₽/день",
      image: "/api/placeholder/300/200",
      features: ["Премиум", "Спорт", "Навигация"]
    },
    {
      id: 4,
      name: "Mercedes-Benz GLC",
      type: "SUV",
      price: "5500 ₽/день",
      image: "/api/placeholder/300/200",
      features: ["Люкс", "Полный привод", "Панорамная крыша"]
    }
  ]

  return (
    <section className="py-16 bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center mb-12">
          <h2 className="text-3xl font-bold text-gray-900 mb-4">
            Доступные автомобили
          </h2>
          <p className="text-lg text-gray-600">
            Выберите автомобиль для вашего путешествия
          </p>
        </div>
        
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {cars.map((car) => (
            <div key={car.id} className="bg-white rounded-xl shadow-lg overflow-hidden hover:shadow-xl transition-shadow">
              <div className="h-48 bg-gray-200 flex items-center justify-center">
                <span className="text-gray-500">Фото автомобиля</span>
              </div>
              <div className="p-6">
                <h3 className="text-xl font-semibold text-gray-900 mb-2">
                  {car.name}
                </h3>
                <p className="text-gray-600 mb-2">{car.type}</p>
                <p className="text-2xl font-bold text-primary-600 mb-4">
                  {car.price}
                </p>
                <div className="space-y-2 mb-4">
                  {car.features.map((feature, index) => (
                    <div key={index} className="flex items-center text-sm text-gray-600">
                      <span className="w-2 h-2 bg-primary-500 rounded-full mr-2"></span>
                      {feature}
                    </div>
                  ))}
                </div>
                <button className="w-full bg-primary-600 text-white py-2 px-4 rounded-lg font-semibold hover:bg-primary-700 transition-colors">
                  Забронировать
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  )
} 