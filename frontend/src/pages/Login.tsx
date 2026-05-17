import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { extractApiError } from '../services/api';
import FormField from '../components/FormField';

const schema = z.object({
  email: z.string().email('Email invalido.'),
  senha: z.string().min(4, 'Minimo 4 caracteres.'),
});

type FormData = z.infer<typeof schema>;

export default function Login() {
  const { user, login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [serverError, setServerError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormData>({ resolver: zodResolver(schema) });

  if (user) {
    const from = (location.state as { from?: { pathname: string } } | null)?.from?.pathname;
    return <Navigate to={from && from !== '/login' ? from : '/dashboard'} replace />;
  }

  const onSubmit = async (data: FormData) => {
    setServerError(null);
    try {
      await login(data.email, data.senha);
      navigate('/dashboard');
    } catch (err) {
      setServerError(extractApiError(err));
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-100">
      <div className="bg-white shadow-md rounded-lg w-full max-w-md p-8 mx-4">
        <h1 className="text-2xl font-bold text-brand text-center">City Transporte</h1>
        <p className="text-sm text-slate-500 text-center mb-6">Gestao de Terminais</p>

        <form onSubmit={handleSubmit(onSubmit)} noValidate>
          <FormField label="Email" error={errors.email?.message}>
            <input
              type="email"
              autoComplete="email"
              className="input"
              {...register('email')}
            />
          </FormField>
          <FormField label="Senha" error={errors.senha?.message}>
            <input
              type="password"
              autoComplete="current-password"
              className="input"
              {...register('senha')}
            />
          </FormField>

          {serverError && (
            <div className="mb-3 p-3 rounded bg-red-50 border border-red-200 text-sm text-red-700">
              {serverError}
            </div>
          )}

          <button type="submit" className="btn-primary w-full" disabled={isSubmitting}>
            {isSubmitting ? 'Entrando...' : 'Entrar'}
          </button>
        </form>

        <div className="mt-6 text-xs text-slate-500 border-t pt-3">
          <p className="font-semibold mb-1">Credenciais de demonstracao:</p>
          <ul className="space-y-0.5">
            <li>admin@city.com / admin123</li>
            <li>supervisor@city.com / sup123</li>
            <li>operador@city.com / oper123</li>
            <li>seguranca@city.com / seg123</li>
          </ul>
        </div>
      </div>
    </div>
  );
}
