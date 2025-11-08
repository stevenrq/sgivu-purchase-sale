INSERT INTO purchase_sales (id, client_id, user_id, vehicle_id, purchase_price, sale_price, contract_type,
                            contract_status, payment_limitations, payment_terms, payment_method, observations,
                            created_at, updated_at)
VALUES (nextval('purchase_sales_id_seq'), 1, 1, 1, 22000000, 26000000, 'SALE', 'ACTIVE',
        'Pago máximo en 2 transacciones', 'Entrega del vehículo en 48 horas', 'BANK_TRANSFER',
        'Cliente pide revisión adicional del motor', NOW(), NOW()),

       (nextval('purchase_sales_id_seq'), 2, 2, 2, 15000000, 0, 'PURCHASE', 'COMPLETED',
        'Límite de efectivo permitido', 'Pago contra inspección mecánica', 'CASH',
        'Vehículo en buenas condiciones generales', NOW(), NOW()),

       (nextval('purchase_sales_id_seq'), 3, 3, 3, 18000000, 21000000, 'SALE', 'PENDING',
        'Tope máximo de transferencia de 5M diarios', 'Pago total antes del traspaso', 'MIXED',
        NULL, NOW(), NOW()),

       (nextval('purchase_sales_id_seq'), 4, 4, 4, 9000000, 0, 'PURCHASE', 'ACTIVE',
        'Sin pagos en efectivo', 'Transferencia bancaria obligatoria', 'BANK_TRANSFER',
        'El vehículo presenta detalles estéticos', NOW(), NOW()),

       (nextval('purchase_sales_id_seq'), 5, 5, 5, 24000000, 28500000, 'SALE', 'COMPLETED',
        'Máximo 10M en efectivo', 'Pago en una sola cuota', 'CASH',
        'Incluye SOAT y revisión técnico mecánica', NOW(), NOW()),

       (nextval('purchase_sales_id_seq'), 6, 6, 6, 11000000, 0, 'PURCHASE', 'CANCELED',
        'Sin pago en efectivo permitido', 'Pago vía Nequi', 'DIGITAL_WALLET',
        'Cliente canceló el proceso', NOW(), NOW()),

       (nextval('purchase_sales_id_seq'), 7, 7, 7, 32000000, 36000000, 'SALE', 'ACTIVE',
        'Pago máximo 5M por billeteras digitales', 'Plazo de entrega de 24 horas', 'BANK_DEPOSIT',
        NULL, NOW(), NOW()),

       (nextval('purchase_sales_id_seq'), 8, 8, 8, 5000000, 0, 'PURCHASE', 'COMPLETED',
        'Solo transferencia', 'Entrega inmediata', 'BANK_TRANSFER',
        'Vehículo usado para repuestos', NOW(), NOW()),

       (nextval('purchase_sales_id_seq'), 9, 9, 9, 13000000, 15500000, 'SALE', 'ACTIVE',
        'Máximo 3M en efectivo', 'Traspaso a nombre del comprador', 'CASH',
        'Cliente pidió garantía de caja y motor', NOW(), NOW()),

       (nextval('purchase_sales_id_seq'), 10, 10, 10, 45000000, 47000000, 'SALE', 'PENDING',
        'Pago con cheque de gerencia', 'Entrega del vehículo tras verificación del cheque', 'CASHIERS_CHECK',
        NULL, NOW(), NOW()),

       (nextval('purchase_sales_id_seq'), 11, 1, 11, 17000000, 0, 'PURCHASE', 'ACTIVE',
        'Pago parcial permitido', 'Primera cuota del 50%', 'INSTALLMENT_PAYMENT',
        'Cliente acordó pago en 2 cuotas', NOW(), NOW());
