package travility_back.travility.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import travility_back.travility.entity.Expense;
import travility_back.travility.entity.enums.Category;
import travility_back.travility.entity.enums.PaymentMethod;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class ExpenseDTO {
    private Long id;
    private String title;
    @JsonFormat(shape = JsonFormat.Shape.STRING, timezone ="Asia/Seoul")
    private LocalDate expenseDate;
    private double amount;
    @JsonProperty("isShared")
    private boolean isShared;
    private String imgName;
    private String memo;
    private Long accountBookId;
    private PaymentMethod paymentMethod;
    private Category category;
    private String curUnit;

    public ExpenseDTO(Expense expense) {
        this.id = expense.getId();
        this.title = expense.getTitle();
        this.expenseDate = expense.getExpenseDate();
        this.amount = expense.getAmount();
        this.isShared = expense.isShared();
        this.imgName = expense.getImgName();
        this.memo = expense.getMemo();
        this.paymentMethod = expense.getPaymentMethod();
        this.category = expense.getCategory();
        this.curUnit = expense.getCurUnit();
        this.accountBookId = expense.getAccountBook().getId();
    }
}
