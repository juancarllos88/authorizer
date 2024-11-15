package com.authorizer.domain.model;

import com.authorizer.infrastructure.entity.AccountEntity;
import com.authorizer.infrastructure.entity.BalanceEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Account extends BaseModel<UUID, AccountEntity> {

    private String name;
    private LocalDateTime insertedAt;
    private LocalDateTime updatedAt;
    private List<Balance> balances;

    public Account(UUID id, String name, LocalDateTime insertedAt, LocalDateTime updatedAt, List<Balance> balances) {
        this.id = id;
        this.name = name;
        this.insertedAt = insertedAt;
        this.updatedAt = updatedAt;
        this.balances = balances;
    }

    @Override
    public AccountEntity toEntity() {
        List<BalanceEntity> balanceList = null;
        AccountEntity account = new AccountEntity(id, name, insertedAt, updatedAt, balanceList);
        balanceList = balances== null ? null : balances.stream().map(balance -> new BalanceEntity(balance.getId(),balance.getType(),balance.getAmount(), balance.getInsertedAt(),balance.getUpdatedAt(),account)).collect(Collectors.toList());
        account.setBalances(balanceList);
        return account;
    }
}
