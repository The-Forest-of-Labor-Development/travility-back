package travility_back.travility.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import travility_back.travility.entity.enums.PaymentMethod;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodAmountDTO {

    private PaymentMethod paymentMethod;
    private double amount;
}
