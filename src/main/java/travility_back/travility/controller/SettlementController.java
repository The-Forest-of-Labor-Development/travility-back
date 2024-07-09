package travility_back.travility.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import travility_back.travility.dto.AccountBookDTO;
import travility_back.travility.service.SettlementService;

import java.util.Map;

@RestController
@RequestMapping("/api/settlement")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    //정산용 가계부 조회
    @GetMapping("/{id}/accountbook")
    public AccountBookDTO getAccountBook(@PathVariable("id") Long id) {
        return  settlementService.getAccountBook(id);
    }

    //공동 경비 합계 & 통화 코드별 가중 평균 환율
    @GetMapping("/{id}/totals")
    public Map<String, Object> getTotalSharedExpensesAndExchangeRates(@PathVariable("id") Long id) {
        return settlementService.getTotalSharedExpensesAndExchangeRates(id);
    }

    //1인당 정산 금액
    @GetMapping("/{id}/per-person")
    public Double getPerPersonAmount(@PathVariable("id") Long id) {
        return settlementService.getPerPersonAmount(id);
    }
}
