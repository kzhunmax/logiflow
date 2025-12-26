export enum AdjustmentType {
  ADD = 'ADD',
  REMOVE = 'REMOVE'
}

export interface StockAdjustmentRequest {
  sku: string;
  adjustmentQuantity: number;
  type: AdjustmentType;
}
