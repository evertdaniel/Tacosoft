import { useState, FormEvent } from 'react';
import {
  useSuppliers,
  useCreateSupplier,
  useUpdateSupplier,
  useDeactivateSupplier,
  useActivateSupplier,
  useSearchSuppliers,
} from '@/hooks/useSuppliers';
import { Loading } from '@/components/feedback/Loading';
import { ErrorState } from '@/components/feedback/ErrorState';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { FormModal } from '@/components/ui/FormModal';
import { SupplierCard } from '../components/SupplierCard';
import { SupplierForm } from '../components/SupplierForm';
import type { SupplierDto, CreateSupplierBody, UpdateSupplierBody } from '@/types/domain.types';

export function SuppliersPage() {
  const [searchQuery, setSearchQuery] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedSupplier, setSelectedSupplier] = useState<SupplierDto | null>(null);

  const {
    data: suppliers,
    isLoading: suppliersLoading,
    isError: suppliersError,
    error: suppliersErrorObj,
    refetch: refetchSuppliers,
  } = useSuppliers();
  const {
    data: searchResults,
    isLoading: searchLoading,
    isError: searchError,
    error: searchErrorObj,
    refetch: refetchSearch,
  } = useSearchSuppliers(searchQuery);

  const createSupplier = useCreateSupplier();
  const updateSupplier = useUpdateSupplier();
  const deactivateSupplier = useDeactivateSupplier();
  const activateSupplier = useActivateSupplier();

  const isSearching = Boolean(searchQuery);
  const displayedSuppliers = isSearching ? searchResults : suppliers;
  const isLoading = isSearching ? searchLoading : suppliersLoading;
  const isError = isSearching ? searchError : suppliersError;
  const errorObj = isSearching ? searchErrorObj : suppliersErrorObj;
  const refetch = isSearching ? refetchSearch : refetchSuppliers;

  function handleOpenCreate() {
    setSelectedSupplier(null);
    setIsModalOpen(true);
  }

  function handleEdit(id: string) {
    const supplier = displayedSuppliers?.find((item) => item.id === id) ?? null;
    setSelectedSupplier(supplier);
    setIsModalOpen(true);
  }

  function handleCloseModal() {
    setIsModalOpen(false);
    setSelectedSupplier(null);
  }

  function handleSubmit(body: CreateSupplierBody | UpdateSupplierBody) {
    if (selectedSupplier) {
      updateSupplier.mutate(
        { id: selectedSupplier.id, body: body as UpdateSupplierBody },
        {
          onSuccess: () => {
            handleCloseModal();
          },
        }
      );
    } else {
      createSupplier.mutate(body as CreateSupplierBody, {
        onSuccess: () => {
          handleCloseModal();
        },
      });
    }
  }

  function handleToggle(id: string) {
    const supplier = displayedSuppliers?.find((item) => item.id === id);
    if (!supplier) return;

    if (supplier.isActive) {
      deactivateSupplier.mutate(id);
    } else {
      activateSupplier.mutate(id);
    }
  }

  function handleSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
  }

  if (isLoading) {
    return <Loading message="Loading suppliers…" />;
  }

  if (isError || !displayedSuppliers) {
    return (
      <ErrorState
        title="Suppliers unavailable"
        message={errorObj?.message ?? 'Failed to load suppliers. Please try again.'}
        onRetry={() => refetch()}
      />
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <h1 className="text-2xl font-bold text-neutral-900">Suppliers</h1>
        <Button onClick={handleOpenCreate} data-testid="add-supplier-button">Add supplier</Button>
      </div>

      <form onSubmit={handleSearch} className="max-w-md">
        <Input
          id="supplier-search"
          label="Search suppliers"
          type="search"
          value={searchQuery}
          onChange={(event) => setSearchQuery(event.target.value)}
          placeholder="Search by name"
        />
      </form>

      {displayedSuppliers.length === 0 ? (
        <p className="text-neutral-500">No suppliers found.</p>
      ) : (
        <div className="grid gap-4 md:grid-cols-2">
          {displayedSuppliers.map((supplier) => (
            <SupplierCard
              key={supplier.id}
              supplier={supplier}
              onEdit={handleEdit}
              onToggle={handleToggle}
            />
          ))}
        </div>
      )}

      <FormModal
        isOpen={isModalOpen}
        title={selectedSupplier ? 'Edit supplier' : 'New supplier'}
        onClose={handleCloseModal}
      >
        <SupplierForm
          initialData={selectedSupplier}
          onSubmit={handleSubmit}
          isLoading={createSupplier.isPending || updateSupplier.isPending}
        />
      </FormModal>
    </div>
  );
}
