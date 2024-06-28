package travility_back.travility.dto;

import lombok.Getter;
import lombok.Setter;
import travility_back.travility.entity.enums.Category;
import travility_back.travility.entity.enums.PaymentMethod;

import java.util.Date;

@Getter
@Setter
public class ExpenseDTO {
    private Long id;
    private Date expenseDate;
    private double amount;
    private boolean isShared;
    private String imgName;
    private String memo;
    private Long accountBookId;
    private PaymentMethod paymentMethod;
    private Category category;
}