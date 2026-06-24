import { useState } from 'react';
import { Input } from '@/components/ui/Input';
import { Button } from '@/components/ui/Button';
import type {
  TableDto,
  ProductDto,
  ProductOptionDto,
  CreateOrderBody,
  CreateOrderDetailBody,
  OrderType,
} from '@/types/domain.types';

interface CreateOrderFormProps {
  tables: TableDto[];
  products: ProductDto[];
  productOptions: ProductOptionDto[];
  onSubmit: (body: CreateOrderBody) => void;
  isLoading?: boolean;
}

interface ProductLine {
  id: string;
  productId: string;
  quantity: number;
  productOptionId: string;
  notes: string;
}

function createProductLine(products: ProductDto[]): ProductLine {
  return {
    id: crypto.randomUUID(),
    productId: products[0]?.id ?? '',
    quantity: 1,
    productOptionId: '',
    notes: '',
  };
}

export function CreateOrderForm({
  tables,
  products,
  productOptions,
  onSubmit,
  isLoading = false,
}: CreateOrderFormProps) {
  const [type, setType] = useState<OrderType>('IN_PLACE');
  const [people, setPeople] = useState(1);
  const [tableId, setTableId] = useState(tables[0]?.id ?? '');
  const [lines, setLines] = useState<ProductLine[]>([]);
  const [error, setError] = useState<string | null>(null);

  function handleAddLine() {
    setLines((prev) => [...prev, createProductLine(products)]);
    setError(null);
  }

  function handleRemoveLine(id: string) {
    setLines((prev) => prev.filter((line) => line.id !== id));
  }

  function updateLine(id: string, patch: Partial<ProductLine>) {
    setLines((prev) =>
      prev.map((line) => {
        if (line.id !== id) return line;
        const updated = { ...line, ...patch };
        if (patch.productId) {
          updated.productOptionId = '';
        }
        return updated;
      })
    );
  }

  function getOptionsForProduct(productId: string): ProductOptionDto[] {
    return productOptions.filter((option) => option.productId === productId && option.isAvailable);
  }

  function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);

    if (lines.length === 0) {
      setError('At least one product is required');
      return;
    }

    const details: CreateOrderDetailBody[] = lines
      .filter((line) => line.productId)
      .map((line) => ({
        productId: line.productId,
        quantity: line.quantity,
        ...(line.productOptionId ? { productOptionId: line.productOptionId } : {}),
        ...(line.notes ? { notes: line.notes } : {}),
      }));

    if (details.length === 0) {
      setError('At least one product is required');
      return;
    }

    const body: CreateOrderBody = {
      type,
      people,
      details,
      ...(type === 'IN_PLACE' ? { tableId } : {}),
    };

    onSubmit(body);
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="space-y-1">
        <label htmlFor="order-type" className="block text-sm font-medium text-neutral-700">
          Order Type
        </label>
        <select
          id="order-type"
          value={type}
          onChange={(event) => setType(event.target.value as OrderType)}
          disabled={isLoading}
          className="block w-full rounded-md border border-neutral-300 px-3 py-2 text-sm"
        >
          <option value="IN_PLACE">In place</option>
          <option value="TAKE_AWAY">Take away</option>
        </select>
      </div>

      {type === 'IN_PLACE' && (
        <div className="space-y-1">
          <label htmlFor="order-table" className="block text-sm font-medium text-neutral-700">
            Table
          </label>
          <select
            id="order-table"
            value={tableId}
            onChange={(event) => setTableId(event.target.value)}
            disabled={isLoading}
            className="block w-full rounded-md border border-neutral-300 px-3 py-2 text-sm"
          >
            {tables.map((table) => (
              <option key={table.id} value={table.id}>Table {table.num}</option>
            ))}
          </select>
        </div>
      )}

      <Input
        label="People"
        id="order-people"
        type="number"
        min={1}
        value={people}
        onChange={(event) => setPeople(Number(event.target.value))}
        disabled={isLoading}
      />

      <div className="space-y-2">
        <div className="flex items-center justify-between">
          <span className="text-sm font-medium text-neutral-700">Products</span>
          <Button type="button" variant="secondary" onClick={handleAddLine} disabled={isLoading}>
            Add Product
          </Button>
        </div>

        {lines.map((line, index) => (
          <div key={line.id} className="rounded-md border border-neutral-200 p-3 space-y-3">
            <div className="flex items-center justify-between">
              <span className="text-sm font-medium text-neutral-700">Item {index + 1}</span>
              <Button
                type="button"
                variant="danger"
                onClick={() => handleRemoveLine(line.id)}
                disabled={isLoading}
              >
                Remove
              </Button>
            </div>
            <div className="space-y-1">
              <label htmlFor={`product-${line.id}`} className="block text-sm font-medium text-neutral-700">
                Product
              </label>
              <select
                id={`product-${line.id}`}
                value={line.productId}
                onChange={(event) => updateLine(line.id, { productId: event.target.value })}
                disabled={isLoading}
                className="block w-full rounded-md border border-neutral-300 px-3 py-2 text-sm"
              >
                {products.map((product) => (
                  <option key={product.id} value={product.id}>{product.name}</option>
                ))}
              </select>
            </div>
            <Input
              label="Quantity"
              id={`quantity-${line.id}`}
              type="number"
              min={1}
              value={line.quantity}
              onChange={(event) => updateLine(line.id, { quantity: Number(event.target.value) })}
              disabled={isLoading}
            />
            {getOptionsForProduct(line.productId).length > 0 && (
              <div className="space-y-1">
                <label htmlFor={`option-${line.id}`} className="block text-sm font-medium text-neutral-700">
                  Option
                </label>
                <select
                  id={`option-${line.id}`}
                  value={line.productOptionId}
                  onChange={(event) => updateLine(line.id, { productOptionId: event.target.value })}
                  disabled={isLoading}
                  className="block w-full rounded-md border border-neutral-300 px-3 py-2 text-sm"
                >
                  <option value="">No option</option>
                  {getOptionsForProduct(line.productId).map((option) => (
                    <option key={option.id} value={option.id}>{option.name}</option>
                  ))}
                </select>
              </div>
            )}
            <Input
              label="Notes"
              id={`notes-${line.id}`}
              value={line.notes}
              onChange={(event) => updateLine(line.id, { notes: event.target.value })}
              disabled={isLoading}
            />
          </div>
        ))}
      </div>

      {error && <p className="text-sm text-red-600">{error}</p>}

      <div className="flex justify-end">
        <Button type="submit" disabled={isLoading}>{isLoading ? 'Creating…' : 'Create Order'}</Button>
      </div>
    </form>
  );
}
