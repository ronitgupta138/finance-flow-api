package com.financeflow.mesh.entity;

public enum MeshSettlementStatus {
    SETTLED,
    DUPLICATE_DROPPED,
    EXPIRED,
    INVALID_SIGNATURE,
    INSUFFICIENT_FUNDS,
    FAILED
}
