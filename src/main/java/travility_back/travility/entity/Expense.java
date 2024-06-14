package travility_back.travility.entity;

import jakarta.persistence.*;
import travility_back.travility.entity.enums.Category;
import travility_back.travility.entity.enums.PamentMethod;

import java.util.Date;

@Entity
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expense_id")
    private Long id;

    private String title;

    private Date expenseDate; // 지출일자

    private double amount; // 지출금액

    private boolean isShared; // 공유경비 개인경비 구분. true - 공유 / false - 공유안함

    private String imgName;

    private String memo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_book_id")
    private AccountBook accountBook; // 가계부

    @Enumerated(EnumType.STRING)
    private PamentMethod pamentMethod; // 결제방법 [CARD, CASH]

    @Enumerated(EnumType.STRING)
    private Category category; // 지출종류 [TRANSPORTATION, ACCOMMODATION, FOOD, TOURISM, SHOPPING, OTHERS]


}
