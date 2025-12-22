import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProductCreate } from './product-create';

describe('ProductCreate', () => {
  let component: ProductCreate;
  let fixture: ComponentFixture<ProductCreate>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductCreate]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProductCreate);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
