import { describe, it, expect } from 'vitest';
import {
  getSections,
  getCategories,
  getProducts,
  getProductOptions,
  getProductionAreas,
  createSection,
  updateSection,
  deleteSection,
  createCategory,
  updateCategory,
  deleteCategory,
  createProduct,
  updateProduct,
  deleteProduct,
  createProductOption,
  updateProductOption,
  deleteProductOption,
  createProductionArea,
  updateProductionArea,
  deleteProductionArea,
} from './menu.api';
import {
  sectionsFixture,
  categoriesFixture,
  productsFixture,
  productOptionsFixture,
  productionAreasFixture,
} from '@/test/fixtures';
import { server } from '@/test/server';
import { http, HttpResponse } from 'msw';

const createBody = { name: 'New Section', description: 'Desc', displayOrder: 3, isActive: true };
const updateBody = { name: 'Updated Section', displayOrder: 1 };

describe('menu API', () => {
  it('getSections returns the list of sections', async () => {
    const sections = await getSections();

    expect(sections).toEqual(sectionsFixture);
  });

  it('getCategories returns the list of categories', async () => {
    const categories = await getCategories();

    expect(categories).toEqual(categoriesFixture);
  });

  it('getProducts returns the list of products', async () => {
    const products = await getProducts();

    expect(products).toEqual(productsFixture);
  });

  it('getProductOptions returns the list of product options', async () => {
    const options = await getProductOptions();

    expect(options).toEqual(productOptionsFixture);
  });

  it('getProductionAreas returns the list of production areas', async () => {
    const areas = await getProductionAreas();

    expect(areas).toEqual(productionAreasFixture);
  });

  it('createSection posts the body and returns the created section', async () => {
    const section = await createSection(createBody);

    expect(section.name).toBe(createBody.name);
    expect(section.displayOrder).toBe(createBody.displayOrder);
  });

  it('updateSection puts the body and returns the updated section', async () => {
    const section = await updateSection('section-1', updateBody);

    expect(section.id).toBe('section-1');
    expect(section.name).toBe(updateBody.name);
  });

  it('deleteSection sends a delete request', async () => {
    await expect(deleteSection('section-1')).resolves.toBeUndefined();
  });

  it('createCategory posts the body and returns the created category', async () => {
    const body = { name: 'New Category', sectionId: 'section-1' };
    const category = await createCategory(body);

    expect(category.name).toBe(body.name);
    expect(category.sectionId).toBe(body.sectionId);
  });

  it('updateCategory puts the body and returns the updated category', async () => {
    const body = { name: 'Updated Category' };
    const category = await updateCategory('category-1', body);

    expect(category.id).toBe('category-1');
    expect(category.name).toBe(body.name);
  });

  it('deleteCategory sends a delete request', async () => {
    await expect(deleteCategory('category-1')).resolves.toBeUndefined();
  });

  it('createProduct posts the body and returns the created product', async () => {
    const body = { name: 'New Product', price: 12000, categoryId: 'category-1' };
    const product = await createProduct(body);

    expect(product.name).toBe(body.name);
    expect(product.price).toBe(body.price);
  });

  it('updateProduct puts the body and returns the updated product', async () => {
    const body = { name: 'Updated Product', price: 9999 };
    const product = await updateProduct('product-1', body);

    expect(product.id).toBe('product-1');
    expect(product.name).toBe(body.name);
  });

  it('deleteProduct sends a delete request', async () => {
    await expect(deleteProduct('product-1')).resolves.toBeUndefined();
  });

  it('createProductOption posts the body and returns the created option', async () => {
    const body = { name: 'New Option', priceAdjustment: 500, productId: 'product-1' };
    const option = await createProductOption(body);

    expect(option.name).toBe(body.name);
    expect(option.productId).toBe(body.productId);
  });

  it('updateProductOption puts the body and returns the updated option', async () => {
    const body = { name: 'Updated Option' };
    const option = await updateProductOption('option-1', body);

    expect(option.id).toBe('option-1');
    expect(option.name).toBe(body.name);
  });

  it('deleteProductOption sends a delete request', async () => {
    await expect(deleteProductOption('option-1')).resolves.toBeUndefined();
  });

  it('createProductionArea posts the body and returns the created area', async () => {
    const body = { name: 'New Area' };
    const area = await createProductionArea(body);

    expect(area.name).toBe(body.name);
  });

  it('updateProductionArea puts the body and returns the updated area', async () => {
    const body = { name: 'Updated Area' };
    const area = await updateProductionArea('area-1', body);

    expect(area.id).toBe('area-1');
    expect(area.name).toBe(body.name);
  });

  it('deleteProductionArea sends a delete request', async () => {
    await expect(deleteProductionArea('area-1')).resolves.toBeUndefined();
  });

  it('throws when the sections request fails', async () => {
    server.use(
      http.get('http://localhost:8080/sections', () => {
        return new HttpResponse(JSON.stringify({ message: 'Sections error' }), { status: 500 });
      })
    );

    await expect(getSections()).rejects.toThrow('Sections error');
  });

  it('throws when createSection fails', async () => {
    server.use(
      http.post('http://localhost:8080/sections', () => {
        return new HttpResponse(JSON.stringify({ message: 'Create section error' }), { status: 500 });
      })
    );

    await expect(createSection(createBody)).rejects.toThrow('Create section error');
  });

  it('throws when updateSection fails', async () => {
    server.use(
      http.put('http://localhost:8080/sections/:id', () => {
        return new HttpResponse(JSON.stringify({ message: 'Update section error' }), { status: 500 });
      })
    );

    await expect(updateSection('section-1', updateBody)).rejects.toThrow('Update section error');
  });

  it('throws when deleteSection fails', async () => {
    server.use(
      http.delete('http://localhost:8080/sections/:id', () => {
        return new HttpResponse(JSON.stringify({ message: 'Delete section error' }), { status: 500 });
      })
    );

    await expect(deleteSection('section-1')).rejects.toThrow('Delete section error');
  });
});
