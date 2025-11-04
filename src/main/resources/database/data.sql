INSERT INTO purchase_sales (id, client_id, user_id, vehicle_id, purchase_price, sale_price, contract_type,
                            contract_status, payment_limitations, payment_terms, payment_method, observations,
                            created_at, updated_at)
VALUES (nextval('purchase_sales_id_seq'), 10, 3, 5, 22000000, 26000000, 'SALE', 'ACTIVE',
        'Pago máximo en 2 transacciones', 'Entrega del vehículo en 48 horas', 'BANK_TRANSFER',
        'Cliente pide revisión adicional del motor', NOW(), NOW()),

       (nextval('purchase_sales_id_seq'), 12, 5, 6, 15000000, 0, 'PURCHASE', 'COMPLETED',
        'Límite de efectivo permitido', 'Pago contra inspección mecánica', 'CASH',
        'Vehículo en buenas condiciones generales', NOW(), NOW()),

       (nextval('purchase_sales_id_seq'), 8, 2, 7, 18000000, 21000000, 'SALE', 'PENDING',
        'Tope máximo de transferencia de 5M diarios', 'Pago total antes del traspaso', 'MIXED',
        NULL, NOW(), NOW()),

       (nextval('purchase_sales_id_seq'), 15, 4, 8, 9000000, 0, 'PURCHASE', 'ACTIVE',
        'Sin pagos en efectivo', 'Transferencia bancaria obligatoria', 'BANK_TRANSFER',
        'El vehículo presenta detalles estéticos', NOW(), NOW()),

       (nextval('purchase_sales_id_seq'), 20, 6, 9, 24000000, 28500000, 'SALE', 'COMPLETED',
        'Máximo 10M en efectivo', 'Pago en una sola cuota', 'CASH',
        'Incluye SOAT y revisión técnico mecánica', NOW(), NOW()),

       (nextval('purchase_sales_id_seq'), 13, 7, 10, 11000000, 0, 'PURCHASE', 'CANCELED',
        'Sin pago en efectivo permitido', 'Pago vía Nequi', 'DIGITAL_WALLET',
        'Cliente canceló el proceso', NOW(), NOW()),

       (nextval('purchase_sales_id_seq'), 18, 3, 11, 32000000, 36000000, 'SALE', 'ACTIVE',
        'Pago máximo 5M por billeteras digitales', 'Plazo de entrega de 24 horas', 'BANK_DEPOSIT',
        NULL, NOW(), NOW()),

       (nextval('purchase_sales_id_seq'), 11, 5, 12, 5000000, 0, 'PURCHASE', 'COMPLETED',
        'Solo transferencia', 'Entrega inmediata', 'BANK_TRANSFER',
        'Vehículo usado para repuestos', NOW(), NOW()),

       (nextval('purchase_sales_id_seq'), 7, 6, 13, 13000000, 15500000, 'SALE', 'ACTIVE',
        'Máximo 3M en efectivo', 'Traspaso a nombre del comprador', 'CASH',
        'Cliente pidió garantía de caja y motor', NOW(), NOW()),

       (nextval('purchase_sales_id_seq'), 9, 2, 14, 45000000, 47000000, 'SALE', 'PENDING',
        'Pago con cheque de gerencia', 'Entrega del vehículo tras verificación del cheque', 'CASHIERS_CHECK',
        NULL, NOW(), NOW()),

       (nextval('purchase_sales_id_seq'), 5, 4, 15, 17000000, 0, 'PURCHASE', 'ACTIVE',
        'Pago parcial permitido', 'Primera cuota del 50%', 'INSTALLMENT_PAYMENT',
        'Cliente acordó pago en 2 cuotas', NOW(), NOW()),

       (nextval('purchase_sales_id_seq'), 6, 8, 16, 6000000, 0, 'PURCHASE', 'COMPLETED',
        'Pago en consignación', 'Entrega inmediata', 'BANK_DEPOSIT',
        NULL, NOW(), NOW()),

       (nextval('purchase_sales_id_seq'), 16, 3, 17, 19500000, 22500000, 'SALE', 'ACTIVE',
        'Máximo 8M en efectivo', 'Pago único vía transferencia', 'MIXED',
        'Incluye accesorios adicionales', NOW(), NOW()),

       (nextval('purchase_sales_id_seq'), 19, 6, 18, 25000000, 26000000, 'SALE', 'COMPLETED',
        'Pago solo por transferencia', 'Entrega inmediata', 'BANK_TRANSFER',
        'Cliente satisfecho con el estado del vehículo', NOW(), NOW()),

       (nextval('purchase_sales_id_seq'), 14, 5, 19, 8000000, 0, 'PURCHASE', 'PENDING',
        'Pago en Nequi o Daviplata', 'Entrega después de diagnóstico', 'DIGITAL_WALLET',
        NULL, NOW(), NOW()),

       (nextval('purchase_sales_id_seq'), 5, 9, 20, 38000000, 42000000, 'SALE', 'CANCELED',
        'Pago solo con cheque de gerencia', 'Entrega en 72 horas', 'CASHIERS_CHECK',
        'Cliente desistió del proceso', NOW(), NOW()),

       (nextval('purchase_sales_id_seq'), 2, 1, 1, 21000000, 24000000, 'SALE', 'ACTIVE',
        'Límite 4M en efectivo', 'Pago total antes de la entrega', 'CASH',
        NULL, NOW(), NOW()),

       (nextval('purchase_sales_id_seq'), 6, 7, 2, 27000000, 30000000, 'SALE', 'COMPLETED',
        'Pago en 2 cuotas', 'Cuotas mensuales durante 2 meses', 'INSTALLMENT_PAYMENT',
        'Contrato completado sin retrasos', NOW(), NOW()),

       (nextval('purchase_sales_id_seq'), 3, 8, 3, 14000000, 0, 'PURCHASE', 'ACTIVE',
        'Consignación bancaria obligatoria', 'Entrega inmediata', 'BANK_DEPOSIT',
        NULL, NOW(), NOW()),

       (nextval('purchase_sales_id_seq'), 4, 4, 4, 30000000, 35000000, 'SALE', 'ACTIVE',
        'Máximo 2M en billeteras digitales', 'Pago único', 'DIGITAL_WALLET',
        'Incluye cambio de aceite y limpieza general', NOW(), NOW());
