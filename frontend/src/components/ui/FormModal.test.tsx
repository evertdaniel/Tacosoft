import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { FormModal } from './FormModal';

describe('FormModal', () => {
  it('does not render when isOpen is false', () => {
    render(
      <FormModal isOpen={false} title="Form" onClose={() => {}}>
        <input />
      </FormModal>
    );

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
  });

  it('renders the title and children when isOpen is true', () => {
    render(
      <FormModal isOpen title="Section Form" onClose={() => {}}>
        <input aria-label="Name" />
      </FormModal>
    );

    expect(screen.getByRole('dialog')).toBeInTheDocument();
    expect(screen.getByText('Section Form')).toBeInTheDocument();
    expect(screen.getByLabelText('Name')).toBeInTheDocument();
  });

  it('calls onClose when the cancel button is clicked', async () => {
    const handleClose = vi.fn();
    render(
      <FormModal isOpen title="Form" onClose={handleClose}>
        <input />
      </FormModal>
    );

    await userEvent.click(screen.getByRole('button', { name: /cancel/i }));

    expect(handleClose).toHaveBeenCalledTimes(1);
  });
});
