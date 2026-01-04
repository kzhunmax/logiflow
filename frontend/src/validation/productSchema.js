import * as yup from 'yup'

export const productSchema = yup.object({
  name: yup
    .string()
    .required('Product name must not be blank')
    .min(2, 'Name must be between 2 and 100 characters long.')
    .max(100, 'Name must be between 2 and 100 characters long.'),
  sku: yup
    .string()
    .required('SKU must not be blank')
    .min(2, 'SKU must be between 2 and 150 characters long.')
    .max(150, 'SKU must be between 2 and 150 characters long.'),
  price: yup
    .number()
    .typeError('Price must be a number')
    .required('Price must not be blank')
    .min(0.01, 'Price must be greater than 0.01')
})
