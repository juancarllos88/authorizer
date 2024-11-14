package com.authorizer.infrastructure.repository;

import com.authorizer.infrastructure.entity.BalanceEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.UUID;

public interface BalanceRepository extends BaseRepository<BalanceEntity, UUID> {

    @Modifying
    @Query("UPDATE BalanceEntity i SET i.amount = ?2 WHERE i.id = ?1")
    void updateBalanceAmount(UUID id, BigDecimal amount);

}
