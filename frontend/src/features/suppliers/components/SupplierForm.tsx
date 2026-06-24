import { useState, FormEvent } from 'react';
import { Input } from '@/components/ui/Input';
import { Button } from '@/components/ui/Button';
import type { SupplierDto, CreateSupplierBody, UpdateSupplierBody } from '@/types/domain.types';

interface SupplierFormProps {
  initialData?: SupplierDto | null;
  onSubmit: (body: CreateSupplierBody | UpdateSupplierBody) => void;
  isLoading?: boolean;
}

export function SupplierForm({ initialData, onSubmit, isLoading = false }: SupplierFormProps) {
  const [name, setName] = useState(initialData?.name ?? '');
  const [contactName, setContactName] = useState(initialData?.contactName ?? '');
  const [email, setEmail] = useState(initialData?.email ?? '');
  const [phone, setPhone] = useState(initialData?.phone ?? '');
  const [address, setAddress] = useState(initialData?.address ?? '');
  const [taxId, setTaxId] = useState(initialData?.taxId ?? '');
  const [isActive, setIsActive] = useState(initialData?.isActive ?? true);
  const [error, setError] = useState<string | null>(null);

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);

    if (!name.trim()) {
      setError('Name is required');
      return;
    }

    const body: CreateSupplierBody | UpdateSupplierBody = {
      name: name.trim(),
      contactName: contactName.trim() || undefined,
      email: email.trim() || undefined,
      phone: phone.trim() || undefined,
      address: address.trim() || undefined,
      taxId: taxId.trim() || undefined,
    };

    if (initialData) {
      (body as UpdateSupplierBody).isActive = isActive;
    }

    onSubmit(body);
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <Input
        label="Name"
        id="supplier-name"
        value={name}
        onChange={(event) => setName(event.target.value)}
        disabled={isLoading}
        error={error ?? undefined}
      />
      <Input
        label="Contact name"
        id="supplier-contact-name"
        value={contactName}
        onChange={(event) => setContactName(event.target.value)}
        disabled={isLoading}
      />
      <Input
        label="Email"
        id="supplier-email"
        type="email"
        value={email}
        onChange={(event) => setEmail(event.target.value)}
        disabled={isLoading}
      />
      <Input
        label="Phone"
        id="supplier-phone"
        value={phone}
        onChange={(event) => setPhone(event.target.value)}
        disabled={isLoading}
      />
      <Input
        label="Address"
        id="supplier-address"
        value={address}
        onChange={(event) => setAddress(event.target.value)}
        disabled={isLoading}
      />
      <Input
        label="Tax ID"
        id="supplier-tax-id"
        value={taxId}
        onChange={(event) => setTaxId(event.target.value)}
        disabled={isLoading}
      />
      {initialData && (
        <label className="flex items-center gap-2 text-sm text-neutral-700">
          <input
            type="checkbox"
            checked={isActive}
            onChange={(event) => setIsActive(event.target.checked)}
            disabled={isLoading}
          />
          Active
        </label>
      )}
      <div className="flex justify-end">
        <Button type="submit" disabled={isLoading}>{isLoading ? 'Saving…' : 'Save'}</Button>
      </div>
    </form>
  );
}
