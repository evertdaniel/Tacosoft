import { useState } from 'react';
import { Input } from '@/components/ui/Input';
import { Button } from '@/components/ui/Button';
import type {
  ProductOptionDto,
  ProductDto,
  CreateProductOptionBody,
  UpdateProductOptionBody,
} from '@/types/domain.types';

interface ProductOptionFormProps {
  products: ProductDto[];
  initialData?: ProductOptionDto | null;
  onSubmit: (body: CreateProductOptionBody | UpdateProductOptionBody) => void;
  isLoading?: boolean;
}

export function ProductOptionForm({
  products,
  initialData,
  onSubmit,
  isLoading = false,
}: ProductOptionFormProps) {
  const [name, setName] = useState(initialData?.name ?? '');
  const [description, setDescription] = useState(initialData?.description ?? '');
  const [priceAdjustment, setPriceAdjustment] = useState(initialData?.priceAdjustment ?? 0);
  const [productId, setProductId] = useState(initialData?.productId ?? products[0]?.id ?? '');
  const [isDefault, setIsDefault] = useState(initialData?.isDefault ?? false);
  const [isAvailable, setIsAvailable] = useState(initialData?.isAvailable ?? true);
  const [error, setError] = useState<string | null>(null);

  function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError(null);

    if (!name.trim()) {
      setError('Name is required');
      return;
    }
    if (priceAdjustment < 0) {
      setError('Price adjustment must be positive or zero');
      return;
    }

    onSubmit({
      name: name.trim(),
      description: description.trim(),
      priceAdjustment,
      ...(initialData ? {} : { productId }),
      isDefault,
      isAvailable,
    });
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <Input
        label="Name"
        id="option-name"
        value={name}
        onChange={(e) => setName(e.target.value)}
        error={error ?? undefined}
        disabled={isLoading}
      />
      <Input
        label="Description"
        id="option-description"
        value={description}
        onChange={(e) => setDescription(e.target.value)}
        disabled={isLoading}
      />
      <Input
        label="Price Adjustment"
        id="option-price-adjustment"
        type="number"
        value={priceAdjustment}
        onChange={(e) => setPriceAdjustment(Number(e.target.value))}
        disabled={isLoading}
      />
      {!initialData && (
        <div className="space-y-1">
          <label htmlFor="option-product" className="block text-sm font-medium text-neutral-700">
            Product
          </label>
          <select
            id="option-product"
            value={productId}
            onChange={(e) => setProductId(e.target.value)}
            disabled={isLoading}
            className="block w-full rounded-md border border-neutral-300 px-3 py-2 text-sm"
          >
            {products.map((product) => (
              <option key={product.id} value={product.id}>
                {product.name}
              </option>
            ))}
          </select>
        </div>
      )}
      <label className="flex items-center gap-2 text-sm text-neutral-700">
        <input
          type="checkbox"
          checked={isDefault}
          onChange={(e) => setIsDefault(e.target.checked)}
          disabled={isLoading}
        />
        Default
      </label>
      <label className="flex items-center gap-2 text-sm text-neutral-700">
        <input
          type="checkbox"
          checked={isAvailable}
          onChange={(e) => setIsAvailable(e.target.checked)}
          disabled={isLoading}
        />
        Available
      </label>
      <div className="flex justify-end">
        <Button type="submit" disabled={isLoading}>{isLoading ? 'Saving…' : 'Save'}</Button>
      </div>
    </form>
  );
}
