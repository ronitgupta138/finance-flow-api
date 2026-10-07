package com.financeflow.mesh.repository;

import com.financeflow.mesh.entity.MeshSettlementLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MeshSettlementLogRepository extends JpaRepository<MeshSettlementLog, Long> {

    Optional<MeshSettlementLog> findByCiphertextHash(String ciphertextHash);

    Optional<MeshSettlementLog> findByPacketId(String packetId);

    boolean existsByCiphertextHash(String ciphertextHash);

    List<MeshSettlementLog> findTop20ByOrderByCreatedAtDesc();
}
