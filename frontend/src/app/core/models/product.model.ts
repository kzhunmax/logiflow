export interface Product {
  id?: string;
  name: string;
  sku: string;
  price: number;
  attributes: Record<string, any>
}
