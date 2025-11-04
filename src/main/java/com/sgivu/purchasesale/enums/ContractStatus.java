package com.sgivu.purchasesale.enums;

/**
 * Enum que define los diferentes estados que puede tener un contrato de compra-venta en el sistema
 * SGIVU.
 *
 * <p>Los estados reflejan el ciclo de vida típico de un contrato, desde su activación hasta su
 * finalización o cancelación.
 */
public enum ContractStatus {
  /** Contrato pendiente de confirmacion. */
  PENDING,

  /** Contrato activo y en ejecución. */
  ACTIVE,

  /** Contrato que ha sido completado exitosamente. */
  COMPLETED,

  /** Contrato que ha sido cancelado antes de su finalización. */
  CANCELED
}
