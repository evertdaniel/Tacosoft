import { useState } from 'react';
import { Input } from '@/components/ui/Input';
import { Button } from '@/components/ui/Button';
import type {
  ProductDto,
  CategoryDto,
  ProductionAreaDto,
  CreateProductBody,
  UpdateProductBody,
  ProductStatus,
} from '@/types/domain.types';

interface ProductFormProps {
  categories: CategoryDto[];
  productionAreas: ProductionAreaDto[];
  initialData?: ProductDto | null;
  onSubmit: (body: CreateProductBody | UpdateProductBody) => void;
  isLoading?: boolean;
}

export function ProductForm({
  categories,
  productionAreas,
  initialData,
  onSubmit,
  isLoading = false,
}: ProductFormProps) {
  const [name, setName] = useState(initialData?.name ?? '');
  const [description, setDescription] = useState(initialData?.description ?? '');
  const [price, setPrice] = useState(initialData?.price ?? 0);
  const [categoryId, setCategoryId] = useState(initialData?.categoryId ?? categories[0]?.id ?? '');
  const [stock, setStock] = useState(initialData?.stock ?? 0);
  const [status, setStatus] = useState<ProductStatus>(initialData?.status ?? 'AVAILABLE');
  const [preparationTime, setPreparationTime] = useState(initialData?.preparationTime ?? 15);
  const [productionAreaId, setProductionAreaId] = useState(
    initialData?.productionAreaId ?? productionAreas[0]?.id ?? ''
  );
  const [isActive, setIsActive] = useState(initialData?.isActive ?? true);
  const [error, setError] = useState<string | null>(null);

  function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError(null);

    if (!name.trim()) {
      setError('Name is required');
      return;
    }
    if (price < 0) {
      setError('Price must be positive');
      return;
    }

    onSubmit({
      name: name.trim(),
      description: description.trim(),
      price,
      ...(initialData ? {} : { categoryId }),
      stock,
      status,
      preparationTime,
      productionAreaId,
      isActive,
    });
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <Input
        label="Name"
        id="product-name"
        value={name}
        onChange={(e) => setName(e.target.value)}
        error={error ?? undefined}
        disabled={isLoading}
      />
      <Input
        label="Description"
        id="product-description"
        value={description}
        onChange={(e) => setDescription(e.target.value)}
        disabled={isLoading}
      />
      <Input
        label="Price"
        id="product-price"
        type="number"
        value={price}
        onChange={(e) => setPrice(Number(e.target.value))}
        disabled={isLoading}
      />
      {!initialData && (
        <div className="space-y-1">
          <label htmlFor="product-category" className="block text-sm font-medium text-neutral-700">
            Category
          </label>
          <select
            id="product-category"
            value={categoryId}
            onChange={(e) => setCategoryId(e.target.value)}
            disabled={isLoading}
            className="block w-full rounded-md border border-neutral-300 px-3 py-2 text-sm"
          >
            {categories.map((category) => (
              <option key={category.id} value={category.id}>
                {category.name}
              </option>
            ))}
          </select>
        </div>
      )}
      <Input
        label="Stock"
        id="product-stock"
        type="number"
        value={stock}
        onChange={(e) => setStock(Number(e.target.value))}
        disabled={isLoading}
      />
      <div className="space-y-1">
        <label htmlFor="product-status" className="block text-sm font-medium text-neutral-700">
          Status
        </label>
        <select
          id="product-status"
          value={status}
          onChange={(e) => setStatus(e.target.value as ProductStatus)}
          disabled={isLoading}
          className="block w-full rounded-md border border-neutral-300 px-3 py-2 text-sm"
        >
          <option value="AVAILABLE">Available</option>
          <option value="OUT_OF_STOCK">Out of Stock</option>
          <option value="OUT_OF_SEASON">Out of Season</option>
        </select>
      </div>
      <Input
        label="Preparation Time (min)"
        id="product-preparation-time"
        type="number"
        value={preparationTime}
        onChange={(e) => setPreparationTime(Number(e.target.value))}
        disabled={isLoading}
      />
      <div className="space-y-1">
        <label htmlFor="product-area" className="block text-sm font-medium text-neutral-700">
          Production Area
        </label>
        <select
          id="product-area"
          value={productionAreaId}
          onChange={(e) => setProductionAreaId(e.target.value)}
          disabled={isLoading}
          className="block w-full rounded-md border border-neutral-300 px-3 py-2 text-sm"
        >
          {productionAreas.map((area) => (
            <option key={area.id} value={area.id}>
              {area.name}
            </option>
          ))}
        </select>
      </div>
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
