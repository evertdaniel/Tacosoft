import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Input } from './Input';

describe('Input', () => {
  it('renders with a label', () => {
    render(<Input label="Name" id="name" />);
    expect(screen.getByLabelText('Name')).toBeInTheDocument();
  });

  it('renders the provided value', () => {
    render(<Input label="Name" id="name" value="John" onChange={() => {}} />);
    expect(screen.getByRole('textbox')).toHaveValue('John');
  });

  it('calls onChange when typing', async () => {
    const handleChange = vi.fn();
    render(<Input label="Name" id="name" value="" onChange={handleChange} />);

    await userEvent.type(screen.getByRole('textbox'), 'Jane');

    expect(handleChange).toHaveBeenCalledTimes(4);
  });

  it('displays an error message', () => {
    render(<Input label="Name" id="name" error="Required" />);
    expect(screen.getByText('Required')).toBeInTheDocument();
  });

  it('is disabled when disabled prop is true', () => {
    render(<Input label="Name" id="name" disabled />);
    expect(screen.getByRole('textbox')).toBeDisabled();
  });
});
