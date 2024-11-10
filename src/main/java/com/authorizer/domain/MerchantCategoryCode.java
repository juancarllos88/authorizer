package com.authorizer.domain;

import com.authorizer.enums.BalanceTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "merchants_category_code")
public class MerchantCategoryCode {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Type(type = "uuid-char")
    private UUID id;

    private String code;

    @NotNull
    @Column(name = "balance_type")
    @Enumerated(EnumType.STRING)
    private BalanceTypeEnum balanceType;
}
