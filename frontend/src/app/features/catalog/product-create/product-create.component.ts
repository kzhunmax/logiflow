import {Component, inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {Router} from '@angular/router';
import {ProductService} from '../../../core/services/product.service';
import {ReactiveFormsModule, FormBuilder, FormGroup, FormArray, Validators} from '@angular/forms';
import {ButtonModule} from 'primeng/button';
import {InputTextModule} from 'primeng/inputtext';

@Component({
  selector: 'app-product-create',
  imports: [CommonModule, ReactiveFormsModule, ButtonModule, InputTextModule],
  templateUrl: './product-create.component.html',
  styleUrl: './product-create.component.scss',
  standalone: true
})
export class ProductCreateComponent {
  private fb = inject(FormBuilder);
  private productService = inject(ProductService);
  private router = inject(Router);

  productForm: FormGroup = this.fb.group({
    name: ['', Validators.required],
    sku: ['', Validators.required],
    price: [null, [Validators.required, Validators.min(0)]],
    attributes: this.fb.array([])
  });

  get attributes(): FormArray {
    return this.productForm.get('attributes') as FormArray;
  }

  addAttribute(): void {
    const attributeGroup = this.fb.group({
      key: ['', Validators.required],
      value: ['', Validators.required]
    });
    this.attributes.push(attributeGroup);
  }

  removeAttribute(index: number) {
    this.attributes.removeAt(index);
  }

  onSubmit(): void {
    if(this.productForm.invalid) return;

    const rawData = this.productForm.value;

    const transformedAttributes = rawData.attributes.reduce((acc: any, curr: any) => {
      acc[curr.key] = curr.value;
      return acc;
    }, {});

    const payload = {
      ...rawData,
      attributes: transformedAttributes
    };

    this.productService.createProduct(payload).subscribe({
      next: () => {
        alert('Product created successfully!');
        this.router.navigate(['/catalog']);
      },
      error: (err) => {
        console.error(err);
        alert('Failed to create product.');
    }
    })
  }
}
