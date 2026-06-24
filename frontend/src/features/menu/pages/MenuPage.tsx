import { useState } from 'react';
import { SectionsList } from '../components/SectionsList';
import { CategoriesList } from '../components/CategoriesList';
import { ProductsList } from '../components/ProductsList';
import { ProductOptionsList } from '../components/ProductOptionsList';
import { ProductionAreasList } from '../components/ProductionAreasList';

type MenuTab = 'sections' | 'categories' | 'products' | 'options' | 'productionAreas';

const tabs: { id: MenuTab; label: string }[] = [
  { id: 'sections', label: 'Sections' },
  { id: 'categories', label: 'Categories' },
  { id: 'products', label: 'Products' },
  { id: 'options', label: 'Options' },
  { id: 'productionAreas', label: 'Production Areas' },
];

export function MenuPage() {
  const [activeTab, setActiveTab] = useState<MenuTab>('sections');

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-neutral-900">Menu</h1>

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

      {activeTab === 'sections' && <SectionsList />}
      {activeTab === 'categories' && <CategoriesList />}
      {activeTab === 'products' && <ProductsList />}
      {activeTab === 'options' && <ProductOptionsList />}
      {activeTab === 'productionAreas' && <ProductionAreasList />}
    </div>
  );
}
