import { useState } from 'react';
import { Input } from '@/components/ui/Input';
import { Button } from '@/components/ui/Button';
import type {
  ProductionAreaDto,
  CreateProductionAreaBody,
  UpdateProductionAreaBody,
} from '@/types/domain.types';

interface ProductionAreaFormProps {
  initialData?: ProductionAreaDto | null;
  onSubmit: (body: CreateProductionAreaBody | UpdateProductionAreaBody) => void;
  isLoading?: boolean;
}

export function ProductionAreaForm({ initialData, onSubmit, isLoading = false }: ProductionAreaFormProps) {
  const [name, setName] = useState(initialData?.name ?? '');
  const [description, setDescription] = useState(initialData?.description ?? '');
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
    });
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <Input
        label="Name"
        id="area-name"
        value={name}
        onChange={(e) => setName(e.target.value)}
        error={error ?? undefined}
        disabled={isLoading}
      />
      <Input
        label="Description"
        id="area-description"
        value={description}
        onChange={(e) => setDescription(e.target.value)}
        disabled={isLoading}
      />
      <div className="flex justify-end">
        <Button type="submit" disabled={isLoading}>{isLoading ? 'Saving…' : 'Save'}</Button>
      </div>
    </form>
  );
}
