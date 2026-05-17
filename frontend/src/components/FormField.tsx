import type { ReactNode } from 'react';

interface Props {
  label: string;
  error?: string;
  children: ReactNode;
}

export default function FormField({ label, error, children }: Props) {
  return (
    <div className="mb-3">
      <label className="label">{label}</label>
      {children}
      {error && <p className="text-xs text-red-600 mt-1">{error}</p>}
    </div>
  );
}
