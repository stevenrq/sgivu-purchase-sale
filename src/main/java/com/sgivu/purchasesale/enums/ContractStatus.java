package com.sgivu.purchasesale.enums;

/**
 * Enum que define los diferentes estados que puede tener un contrato de compra-venta en el sistema
 * SGIVU.
 *
 * <p>Los estados reflejan el ciclo de vida típico de un contrato, desde su activación hasta su
 * finalización o cancelación.
 */
public enum ContractStatus {
  /** Contrato pendiente de confirmacion. Significa que el contrato está a la espera de ser confirmado por ambas partes. */
  PENDING,

  /** Contrato activo y en ejecución. Significa que el contrato ha sido confirmado y está en vigor. */
  ACTIVE,

  /** Contrato que ha sido completado exitosamente. Significa que todas las obligaciones del contrato han sido cumplidas. */
  COMPLETED,

  /** Contrato que ha sido cancelado antes de su finalización. Significa que el contrato ha sido anulado y no tiene validez. */
  CANCELED
}
