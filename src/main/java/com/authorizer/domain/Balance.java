package com.authorizer.domain;


import com.authorizer.enums.BalanceTypeEnum;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "balances")
public class Balance implements Serializable {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Type(type = "uuid-char")
    private UUID id;

    @NotNull
    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private BalanceTypeEnum type;

    @NotNull
    @Column(name = "amount")
    private BigDecimal amount;

    @NotNull
    @Column(name = "inserted_at")
    private LocalDateTime insertedAt;

    @NotNull
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @JoinColumn(name = "account_id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Account account;

}
