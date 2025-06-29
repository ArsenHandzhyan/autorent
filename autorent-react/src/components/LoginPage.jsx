import AuthForm from './AuthForm';

export default function LoginPage() {
  return (
    <div style={{ maxWidth: 400, margin: '0 auto', padding: 32 }}>
      <h1>Вход</h1>
      <AuthForm mode="login" onSubmit={console.log} />
    </div>
  );
} 