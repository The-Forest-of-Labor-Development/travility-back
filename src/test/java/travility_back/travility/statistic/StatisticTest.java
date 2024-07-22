package travility_back.travility.statistic;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import travility_back.travility.dto.statistics.DateCategoryAmountDTO;
import travility_back.travility.entity.enums.Category;
import travility_back.travility.repository.ExpenseRepository;
import travility_back.travility.service.statistic.StatisticService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

public class StatisticTest {

    // 목객체화
    @Mock
    private ExpenseRepository expenseRepository;

    // 목객체가 주입받는 서비스 (@Mock을 여기에 주입)
    @InjectMocks
    private StatisticService statisticService;

    // 초기화
    @BeforeEach
    public void beforeEach() {
        MockitoAnnotations.openMocks(this);
    }

    // getStatisticsByDate
    @Test
    public void testGetstatisticsByDate() throws Exception{
        // given
        Long accountBookId = 1L;
        Long memberId = 1L;
        Object[] expenseRecord = {LocalDateTime.of(2024, 7, 17, 14, 29, 00), Category.FOOD, 10000}; // 2024년 7월 17일 14시 29분 10000원짜리 밥먹음
        List<Object[]> expenseRecordList = Collections.singletonList(expenseRecord);
        when(expenseRepository.findTotalAmountByDateAndCategory(accountBookId, memberId)).thenReturn(expenseRecordList); // mock 동작?

        // when
        List<DateCategoryAmountDTO> result = statisticService.getStatisticsByDate(accountBookId, memberId);

        // then
        assertThat(result).isNotNull(); // null이면 안됨
        assertThat(result.size()).isEqualTo(1); // 리스트 크기
        assertThat(result.get(0).getCategory()).isEqualTo(Category.FOOD);
        assertThat(result.get(0).getDate()).isEqualTo("2024-07-17T14:29:00");
        assertThat(result.get(0).getAmount()).isEqualTo(10000); // 밥값

    }

}
