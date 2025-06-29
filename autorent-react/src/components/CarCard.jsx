import React from 'react';

export default function CarCard({ brand, model, price }) {
  return (
    <div>
      <h3>{brand} {model}</h3>
      <p>Цена: {price}₽/сутки</p>
    </div>
  );
} 