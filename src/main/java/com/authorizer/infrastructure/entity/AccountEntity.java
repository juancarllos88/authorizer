package com.authorizer.infrastructure.entity;

import com.authorizer.domain.model.Account;
import com.authorizer.domain.model.Balance;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "accounts")
public class AccountEntity extends BaseEntity<UUID, Account> {


    @NotNull
    @Column(name = "name")
    private String name;

    @NotNull
    @Column(name = "inserted_at")
    private LocalDateTime insertedAt;

    @NotNull
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(fetch =FetchType.LAZY, mappedBy = "account")
    private List<BalanceEntity> balances;

    public AccountEntity(UUID id, String name, LocalDateTime insertedAt, LocalDateTime updatedAt, List<BalanceEntity> balances) {
        this.id = id;
        this.name = name;
        this.insertedAt = insertedAt;
        this.updatedAt = updatedAt;
        this.balances = balances;
    }

    @Override
    public Account toModel() {
        List<Balance> balanceList = null;
        Account account = new Account(id, name, insertedAt, updatedAt, balanceList);
        balanceList =  balances== null ? null : balances.stream().map(balance -> new Balance(balance.getId(),balance.getType(),balance.getAmount(), balance.getInsertedAt(),balance.getUpdatedAt(),account)).collect(Collectors.toList());
        account.setBalances(balanceList);
        return account;
    }
}
