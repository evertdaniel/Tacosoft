import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { LoginForm } from './LoginForm';

const mockLogin = vi.fn();

describe('LoginForm', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders username and password fields and submit button', () => {
    render(<LoginForm onSubmit={mockLogin} isPending={false} error={null} />);

    expect(screen.getByLabelText(/username/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument();
  });

  it('submits credentials when form is valid', async () => {
    render(<LoginForm onSubmit={mockLogin} isPending={false} error={null} />);

    await userEvent.type(screen.getByLabelText(/username/i), 'admin');
    await userEvent.type(screen.getByLabelText(/password/i), 'secret');
    await userEvent.click(screen.getByRole('button', { name: /sign in/i }));

    expect(mockLogin).toHaveBeenCalledWith({ username: 'admin', password: 'secret' });
  });

  it('does not submit when fields are empty', async () => {
    render(<LoginForm onSubmit={mockLogin} isPending={false} error={null} />);

    await userEvent.click(screen.getByRole('button', { name: /sign in/i }));

    expect(mockLogin).not.toHaveBeenCalled();
    expect(screen.getByText(/username and password are required/i)).toBeInTheDocument();
  });

  it('disables the submit button while pending', () => {
    render(<LoginForm onSubmit={mockLogin} isPending={true} error={null} />);

    expect(screen.getByRole('button', { name: /signing in/i })).toBeDisabled();
  });

  it('displays an error message when login fails', () => {
    const error = new Error('Invalid credentials');
    render(<LoginForm onSubmit={mockLogin} isPending={false} error={error} />);

    expect(screen.getByRole('alert')).toHaveTextContent(/invalid credentials/i);
  });
});
