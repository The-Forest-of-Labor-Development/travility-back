package travility_back.travility.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import travility_back.travility.dto.ExpenseDTO;
import travility_back.travility.entity.enums.Category;
import travility_back.travility.entity.enums.PaymentMethod;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expense_id")
    private Long id;

    @Column(nullable = false)
    private String title; // 항목명

    @Column(nullable = false)
    private LocalDateTime expenseDate; // 지출일자

    @Column(nullable = false)
    private double amount; // 지출금액

    @Column(nullable = false)
    private boolean isShared; // 공유경비 개인경비 구분. true - 공유 / false - 공유안함

    private String imgName;

    private String memo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_book_id")
    private AccountBook accountBook; // 가계부

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod; // 결제방법 [CARD, CASH]

    @Enumerated(EnumType.STRING)
    private Category category; // 지출종류 [TRANSPORTATION, ACCOMMODATION, FOOD, TOURISM, SHOPPING, OTHERS]

    private String curUnit; // 화폐 단위

    public Expense(ExpenseDTO expenseDTO, AccountBook accountBook) {
        this.title = expenseDTO.getTitle();
        this.expenseDate = expenseDTO.getExpenseDate();
        this.amount = expenseDTO.getAmount();
        this.isShared = expenseDTO.isShared();
        this.memo = expenseDTO.getMemo();
        this.accountBook = accountBook;
        this.paymentMethod = expenseDTO.getPaymentMethod();
        this.category = expenseDTO.getCategory();
        this.curUnit = expenseDTO.getCurUnit();
    }
}
