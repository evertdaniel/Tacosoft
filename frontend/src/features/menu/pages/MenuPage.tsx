import { useState } from 'react';
import { SectionsList } from '../components/SectionsList';
import { CategoriesList } from '../components/CategoriesList';
import { ProductsList } from '../components/ProductsList';
import { ProductOptionsList } from '../components/ProductOptionsList';
import { ProductionAreasList } from '../components/ProductionAreasList';
import { SectionForm } from '../components/SectionForm';
import { CategoryForm } from '../components/CategoryForm';
import { ProductForm } from '../components/ProductForm';
import { ProductOptionForm } from '../components/ProductOptionForm';
import { ProductionAreaForm } from '../components/ProductionAreaForm';
import { FormModal } from '@/components/ui/FormModal';
import { useSections } from '@/hooks/useSections';
import { useCategories } from '@/hooks/useCategories';
import { useProducts } from '@/hooks/useProducts';
import { useProductionAreas } from '@/hooks/useProductionAreas';
import {
  useCreateSection,
  useUpdateSection,
  useDeleteSection,
} from '@/hooks/useSectionMutations';
import {
  useCreateCategory,
  useUpdateCategory,
  useDeleteCategory,
} from '@/hooks/useCategoryMutations';
import {
  useCreateProduct,
  useUpdateProduct,
  useDeleteProduct,
} from '@/hooks/useProductMutations';
import {
  useCreateProductOption,
  useUpdateProductOption,
  useDeleteProductOption,
} from '@/hooks/useProductOptionMutations';
import {
  useCreateProductionArea,
  useUpdateProductionArea,
  useDeleteProductionArea,
} from '@/hooks/useProductionAreaMutations';
import type {
  SectionDto,
  CategoryDto,
  ProductDto,
  ProductOptionDto,
  ProductionAreaDto,
} from '@/types/domain.types';

type MenuTab = 'sections' | 'categories' | 'products' | 'options' | 'productionAreas';
type ModalType = 'section' | 'category' | 'product' | 'option' | 'area';
type ModalMode = 'create' | 'edit';

interface ModalState {
  isOpen: boolean;
  type: ModalType | null;
  mode: ModalMode;
  item: SectionDto | CategoryDto | ProductDto | ProductOptionDto | ProductionAreaDto | null;
}

const tabs: { id: MenuTab; label: string }[] = [
  { id: 'sections', label: 'Sections' },
  { id: 'categories', label: 'Categories' },
  { id: 'products', label: 'Products' },
  { id: 'options', label: 'Options' },
  { id: 'productionAreas', label: 'Production Areas' },
];

const modalTitles: Record<ModalType, string> = {
  section: 'Section',
  category: 'Category',
  product: 'Product',
  option: 'Product Option',
  area: 'Production Area',
};

export function MenuPage() {
  const [activeTab, setActiveTab] = useState<MenuTab>('sections');
  const [modal, setModal] = useState<ModalState>({
    isOpen: false,
    type: null,
    mode: 'create',
    item: null,
  });
  const [error, setError] = useState<string | null>(null);

  const { data: sections } = useSections();
  const { data: categories } = useCategories();
  const { data: products } = useProducts();
  const { data: productionAreas } = useProductionAreas();

  const sectionMutations = {
    create: useCreateSection(),
    update: useUpdateSection(),
    remove: useDeleteSection(),
  };
  const categoryMutations = {
    create: useCreateCategory(),
    update: useUpdateCategory(),
    remove: useDeleteCategory(),
  };
  const productMutations = {
    create: useCreateProduct(),
    update: useUpdateProduct(),
    remove: useDeleteProduct(),
  };
  const optionMutations = {
    create: useCreateProductOption(),
    update: useUpdateProductOption(),
    remove: useDeleteProductOption(),
  };
  const areaMutations = {
    create: useCreateProductionArea(),
    update: useUpdateProductionArea(),
    remove: useDeleteProductionArea(),
  };

  function openCreate(type: ModalType) {
    setError(null);
    setModal({ isOpen: true, type, mode: 'create', item: null });
  }

  function openEdit(
    type: ModalType,
    item: SectionDto | CategoryDto | ProductDto | ProductOptionDto | ProductionAreaDto
  ) {
    setError(null);
    setModal({ isOpen: true, type, mode: 'edit', item });
  }

  function closeModal() {
    setModal((prev) => ({ ...prev, isOpen: false }));
    setError(null);
  }

  function handleMutationError(err: unknown) {
    if (err instanceof Error) {
      setError(err.message);
    } else {
      setError('An unexpected error occurred');
    }
  }

  function handleSectionSubmit(body: unknown) {
    setError(null);
    if (modal.mode === 'create') {
      sectionMutations.create.mutate(body as Parameters<typeof sectionMutations.create.mutate>[0], {
        onSuccess: closeModal,
        onError: handleMutationError,
      });
    } else if (modal.item) {
      sectionMutations.update.mutate(
        { id: modal.item.id, body: body as Parameters<typeof sectionMutations.update.mutate>[0]['body'] },
        { onSuccess: closeModal, onError: handleMutationError }
      );
    }
  }

  function handleCategorySubmit(body: unknown) {
    setError(null);
    if (modal.mode === 'create') {
      categoryMutations.create.mutate(body as Parameters<typeof categoryMutations.create.mutate>[0], {
        onSuccess: closeModal,
        onError: handleMutationError,
      });
    } else if (modal.item) {
      categoryMutations.update.mutate(
        { id: modal.item.id, body: body as Parameters<typeof categoryMutations.update.mutate>[0]['body'] },
        { onSuccess: closeModal, onError: handleMutationError }
      );
    }
  }

  function handleProductSubmit(body: unknown) {
    setError(null);
    if (modal.mode === 'create') {
      productMutations.create.mutate(body as Parameters<typeof productMutations.create.mutate>[0], {
        onSuccess: closeModal,
        onError: handleMutationError,
      });
    } else if (modal.item) {
      productMutations.update.mutate(
        { id: modal.item.id, body: body as Parameters<typeof productMutations.update.mutate>[0]['body'] },
        { onSuccess: closeModal, onError: handleMutationError }
      );
    }
  }

  function handleOptionSubmit(body: unknown) {
    setError(null);
    if (modal.mode === 'create') {
      optionMutations.create.mutate(body as Parameters<typeof optionMutations.create.mutate>[0], {
        onSuccess: closeModal,
        onError: handleMutationError,
      });
    } else if (modal.item) {
      optionMutations.update.mutate(
        { id: modal.item.id, body: body as Parameters<typeof optionMutations.update.mutate>[0]['body'] },
        { onSuccess: closeModal, onError: handleMutationError }
      );
    }
  }

  function handleAreaSubmit(body: unknown) {
    setError(null);
    if (modal.mode === 'create') {
      areaMutations.create.mutate(body as Parameters<typeof areaMutations.create.mutate>[0], {
        onSuccess: closeModal,
        onError: handleMutationError,
      });
    } else if (modal.item) {
      areaMutations.update.mutate(
        { id: modal.item.id, body: body as Parameters<typeof areaMutations.update.mutate>[0]['body'] },
        { onSuccess: closeModal, onError: handleMutationError }
      );
    }
  }

  function handleDelete(type: ModalType, id: string) {
    setError(null);
    switch (type) {
      case 'section':
        sectionMutations.remove.mutate(id, { onError: handleMutationError });
        break;
      case 'category':
        categoryMutations.remove.mutate(id, { onError: handleMutationError });
        break;
      case 'product':
        productMutations.remove.mutate(id, { onError: handleMutationError });
        break;
      case 'option':
        optionMutations.remove.mutate(id, { onError: handleMutationError });
        break;
      case 'area':
        areaMutations.remove.mutate(id, { onError: handleMutationError });
        break;
    }
  }

  function renderForm() {
    const isLoading =
      sectionMutations.create.isPending ||
      sectionMutations.update.isPending ||
      categoryMutations.create.isPending ||
      categoryMutations.update.isPending ||
      productMutations.create.isPending ||
      productMutations.update.isPending ||
      optionMutations.create.isPending ||
      optionMutations.update.isPending ||
      areaMutations.create.isPending ||
      areaMutations.update.isPending;

    switch (modal.type) {
      case 'section':
        return (
          <SectionForm
            initialData={modal.item as SectionDto | null}
            onSubmit={handleSectionSubmit}
            isLoading={isLoading}
          />
        );
      case 'category':
        return (
          <CategoryForm
            sections={sections ?? []}
            initialData={modal.item as CategoryDto | null}
            onSubmit={handleCategorySubmit}
            isLoading={isLoading}
          />
        );
      case 'product':
        return (
          <ProductForm
            categories={categories ?? []}
            productionAreas={productionAreas ?? []}
            initialData={modal.item as ProductDto | null}
            onSubmit={handleProductSubmit}
            isLoading={isLoading}
          />
        );
      case 'option':
        return (
          <ProductOptionForm
            products={products ?? []}
            initialData={modal.item as ProductOptionDto | null}
            onSubmit={handleOptionSubmit}
            isLoading={isLoading}
          />
        );
      case 'area':
        return (
          <ProductionAreaForm
            initialData={modal.item as ProductionAreaDto | null}
            onSubmit={handleAreaSubmit}
            isLoading={isLoading}
          />
        );
      default:
        return null;
    }
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-neutral-900">Menu</h1>

      {error && (
        <div className="rounded-md bg-red-50 p-4 text-sm text-red-700" role="alert">{error}</div>
      )}

      <div className="border-b border-neutral-200" role="tablist" aria-label="Menu sections">
        <nav className="-mb-px flex space-x-6" aria-label="Menu tabs">
          {tabs.map((tab) => (
            <button
              key={tab.id}
              role="tab"
              aria-selected={activeTab === tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`whitespace-nowrap border-b-2 px-1 py-3 text-sm font-medium transition-colors ${
                activeTab === tab.id
                  ? 'border-blue-500 text-blue-600'
                  : 'border-transparent text-neutral-500 hover:border-neutral-300 hover:text-neutral-700'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </nav>
      </div>

      {activeTab === 'sections' && (
        <SectionsList
          onAdd={() => openCreate('section')}
          onEdit={(item) => openEdit('section', item)}
          onDelete={(id) => handleDelete('section', id)}
        />
      )}
      {activeTab === 'categories' && (
        <CategoriesList
          onAdd={() => openCreate('category')}
          onEdit={(item) => openEdit('category', item)}
          onDelete={(id) => handleDelete('category', id)}
        />
      )}
      {activeTab === 'products' && (
        <ProductsList
          onAdd={() => openCreate('product')}
          onEdit={(item) => openEdit('product', item)}
          onDelete={(id) => handleDelete('product', id)}
        />
      )}
      {activeTab === 'options' && (
        <ProductOptionsList
          onAdd={() => openCreate('option')}
          onEdit={(item) => openEdit('option', item)}
          onDelete={(id) => handleDelete('option', id)}
        />
      )}
      {activeTab === 'productionAreas' && (
        <ProductionAreasList
          onAdd={() => openCreate('area')}
          onEdit={(item) => openEdit('area', item)}
          onDelete={(id) => handleDelete('area', id)}
        />
      )}

      <FormModal
        isOpen={modal.isOpen}
        title={`${modal.mode === 'create' ? 'Create' : 'Edit'} ${modal.type ? modalTitles[modal.type] : ''}`}
        onClose={closeModal}
      >
        {renderForm()}
      </FormModal>
    </div>
  );
}
