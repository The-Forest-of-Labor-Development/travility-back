package travility_back.travility.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import travility_back.travility.dto.BudgetDTO;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "budget_id")
    private Long id;

    @Column(name = "is_shared", columnDefinition = "BIT(1)")
    @JsonProperty("isShared")
    private boolean isShared; //공유 경비 or 개인 경비

    private String curUnit; //통화 코드

    @Column(precision = 10, scale = 2)
    private BigDecimal exchangeRate; //환율

    private double amount; //금액

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_book_id")
    private AccountBook accountBook;

    public Budget(BudgetDTO budgetDTO, AccountBook accountBook) {
        this.isShared = budgetDTO.isShared();
        this.curUnit = budgetDTO.getCurUnit();
        this.exchangeRate = budgetDTO.getExchangeRate();
        this.amount = budgetDTO.getAmount();
        this.accountBook = accountBook;
    }

}