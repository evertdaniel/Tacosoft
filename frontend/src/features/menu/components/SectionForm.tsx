import { useState } from 'react';
import { Input } from '@/components/ui/Input';
import { Button } from '@/components/ui/Button';
import type { SectionDto, CreateSectionBody, UpdateSectionBody } from '@/types/domain.types';

interface SectionFormProps {
  initialData?: SectionDto | null;
  onSubmit: (body: CreateSectionBody | UpdateSectionBody) => void;
  isLoading?: boolean;
}

export function SectionForm({ initialData, onSubmit, isLoading = false }: SectionFormProps) {
  const [name, setName] = useState(initialData?.name ?? '');
  const [description, setDescription] = useState(initialData?.description ?? '');
  const [displayOrder, setDisplayOrder] = useState(initialData?.displayOrder ?? 0);
  const [isActive, setIsActive] = useState(initialData?.isActive ?? true);
  const [error, setError] = useState<string | null>(null);

  function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError(null);

    if (!name.trim()) {
      setError('Name is required');
      return;
    }

    onSubmit({
      name: name.trim(),
      description: description.trim(),
      displayOrder,
      isActive,
    });
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <Input
        label="Name"
        id="section-name"
        value={name}
        onChange={(e) => setName(e.target.value)}
        error={error ?? undefined}
        disabled={isLoading}
      />
      <Input
        label="Description"
        id="section-description"
        value={description}
        onChange={(e) => setDescription(e.target.value)}
        disabled={isLoading}
      />
      <Input
        label="Display Order"
        id="section-display-order"
        type="number"
        value={displayOrder}
        onChange={(e) => setDisplayOrder(Number(e.target.value))}
        disabled={isLoading}
      />
      <label className="flex items-center gap-2 text-sm text-neutral-700">
        <input
          type="checkbox"
          checked={isActive}
          onChange={(e) => setIsActive(e.target.checked)}
          disabled={isLoading}
        />
        Active
      </label>
      <div className="flex justify-end">
        <Button type="submit" disabled={isLoading}>{isLoading ? 'Saving…' : 'Save'}</Button>
      </div>
    </form>
  );
}
