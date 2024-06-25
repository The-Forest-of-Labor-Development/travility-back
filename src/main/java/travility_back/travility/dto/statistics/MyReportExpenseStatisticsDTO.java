package travility_back.travility.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyReportExpenseStatisticsDTO {

    private String[] categories;
    private double[] amounts;
    private PaymentMethodAmountDTO[] paymentMethods;
    private double totalAmount;

}
