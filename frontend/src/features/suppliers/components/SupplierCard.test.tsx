import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { SupplierCard } from './SupplierCard';
import { suppliersFixture } from '@/test/fixtures';

describe('SupplierCard', () => {
  it('renders supplier name and contact details', () => {
    render(<SupplierCard supplier={suppliersFixture[0]} onEdit={() => {}} onToggle={() => {}} />);

    expect(screen.getByText(suppliersFixture[0].name)).toBeInTheDocument();
    expect(screen.getByText(suppliersFixture[0].contactName)).toBeInTheDocument();
    expect(screen.getByText(suppliersFixture[0].email)).toBeInTheDocument();
  });

  it('shows active status for active suppliers', () => {
    render(<SupplierCard supplier={suppliersFixture[0]} onEdit={() => {}} onToggle={() => {}} />);

    expect(screen.getByText('Active')).toBeInTheDocument();
  });

  it('shows inactive status for inactive suppliers', () => {
    render(<SupplierCard supplier={suppliersFixture[1]} onEdit={() => {}} onToggle={() => {}} />);

    expect(screen.getByText('Inactive')).toBeInTheDocument();
  });

  it('calls onEdit when edit button is clicked', async () => {
    const handleEdit = vi.fn();
    render(<SupplierCard supplier={suppliersFixture[0]} onEdit={handleEdit} onToggle={() => {}} />);

    await userEvent.click(screen.getByRole('button', { name: /edit/i }));

    expect(handleEdit).toHaveBeenCalledWith('supplier-1');
  });

  it('calls onToggle with deactivate for active suppliers', async () => {
    const handleToggle = vi.fn();
    render(<SupplierCard supplier={suppliersFixture[0]} onEdit={() => {}} onToggle={handleToggle} />);

    await userEvent.click(screen.getByRole('button', { name: /deactivate/i }));

    expect(handleToggle).toHaveBeenCalledWith('supplier-1');
  });

  it('calls onToggle with activate for inactive suppliers', async () => {
    const handleToggle = vi.fn();
    render(<SupplierCard supplier={suppliersFixture[1]} onEdit={() => {}} onToggle={handleToggle} />);

    await userEvent.click(screen.getByRole('button', { name: /activate/i }));

    expect(handleToggle).toHaveBeenCalledWith('supplier-2');
  });
});
