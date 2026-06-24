import { useState } from 'react';
import { Input } from '@/components/ui/Input';
import { Button } from '@/components/ui/Button';
import type { CategoryDto, SectionDto, CreateCategoryBody, UpdateCategoryBody } from '@/types/domain.types';

interface CategoryFormProps {
  sections: SectionDto[];
  initialData?: CategoryDto | null;
  onSubmit: (body: CreateCategoryBody | UpdateCategoryBody) => void;
  isLoading?: boolean;
}

export function CategoryForm({ sections, initialData, onSubmit, isLoading = false }: CategoryFormProps) {
  const [name, setName] = useState(initialData?.name ?? '');
  const [description, setDescription] = useState(initialData?.description ?? '');
  const [sectionId, setSectionId] = useState(initialData?.sectionId ?? sections[0]?.id ?? '');
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
      ...(initialData ? {} : { sectionId }),
      ...(initialData ? { isActive } : {}),
    });
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <Input
        label="Name"
        id="category-name"
        value={name}
        onChange={(e) => setName(e.target.value)}
        error={error ?? undefined}
        disabled={isLoading}
      />
      <Input
        label="Description"
        id="category-description"
        value={description}
        onChange={(e) => setDescription(e.target.value)}
        disabled={isLoading}
      />
      {!initialData && (
        <div className="space-y-1">
          <label htmlFor="category-section" className="block text-sm font-medium text-neutral-700">
            Section
          </label>
          <select
            id="category-section"
            value={sectionId}
            onChange={(e) => setSectionId(e.target.value)}
            disabled={isLoading}
            className="block w-full rounded-md border border-neutral-300 px-3 py-2 text-sm"
          >
            {sections.map((section) => (
              <option key={section.id} value={section.id}>
                {section.name}
              </option>
            ))}
          </select>
        </div>
      )}
      {initialData && (
        <label className="flex items-center gap-2 text-sm text-neutral-700">
          <input
            type="checkbox"
            checked={isActive}
            onChange={(e) => setIsActive(e.target.checked)}
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
