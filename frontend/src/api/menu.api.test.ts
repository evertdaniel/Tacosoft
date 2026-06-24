import { describe, it, expect } from 'vitest';
import {
  getSections,
  getCategories,
  getProducts,
  getProductOptions,
  getProductionAreas,
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

  it('throws when the sections request fails', async () => {
    server.use(
      http.get('http://localhost:8080/sections', () => {
        return new HttpResponse(JSON.stringify({ message: 'Sections error' }), { status: 500 });
      })
    );

    await expect(getSections()).rejects.toThrow('Sections error');
  });

  it('throws when the categories request fails', async () => {
    server.use(
      http.get('http://localhost:8080/categories', () => {
        return new HttpResponse(JSON.stringify({ message: 'Categories error' }), { status: 500 });
      })
    );

    await expect(getCategories()).rejects.toThrow('Categories error');
  });

  it('throws when the products request fails', async () => {
    server.use(
      http.get('http://localhost:8080/products', () => {
        return new HttpResponse(JSON.stringify({ message: 'Products error' }), { status: 500 });
      })
    );

    await expect(getProducts()).rejects.toThrow('Products error');
  });

  it('throws when the product options request fails', async () => {
    server.use(
      http.get('http://localhost:8080/product-options', () => {
        return new HttpResponse(JSON.stringify({ message: 'Options error' }), { status: 500 });
      })
    );

    await expect(getProductOptions()).rejects.toThrow('Options error');
  });

  it('throws when the production areas request fails', async () => {
    server.use(
      http.get('http://localhost:8080/production-areas', () => {
        return new HttpResponse(JSON.stringify({ message: 'Areas error' }), { status: 500 });
      })
    );

    await expect(getProductionAreas()).rejects.toThrow('Areas error');
  });
});
