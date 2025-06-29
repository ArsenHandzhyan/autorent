import AuthForm from './AuthForm';

export default function RegisterPage() {
  return (
    <div style={{ maxWidth: 400, margin: '0 auto', padding: 32 }}>
      <h1>Регистрация</h1>
      <AuthForm mode="register" onSubmit={console.log} />
    </div>
  );
} 