import AuthForm from '../components/AuthForm';

export default {
  title: 'AutoRent/AuthForm',
  component: AuthForm,
  argTypes: {
    mode: {
      control: { type: 'radio' },
      options: ['login', 'register'],
      description: 'Режим формы: вход или регистрация',
    },
    onSubmit: { action: 'submitted' },
  },
};

export const Login = {
  args: {
    mode: 'login',
    onSubmit: (data) => alert('Вход: ' + JSON.stringify(data)),
  },
};

export const Register = {
  args: {
    mode: 'register',
    onSubmit: (data) => alert('Регистрация: ' + JSON.stringify(data)),
  },
}; 