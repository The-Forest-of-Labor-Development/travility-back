package travility_back.travility.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import travility_back.travility.entity.AccountBook;
import travility_back.travility.entity.Member;

import java.util.List;

public interface AccountBookRepository extends JpaRepository<AccountBook, Long> {

    @Query("select a from AccountBook a where a.member.id = :memberId")
    List<AccountBook> findByMemberId(@Param("memberId") Long memberId);

    @Query("select ab.countryName from AccountBook ab group by ab.countryName order by count(ab) desc")
    List<String> findTop5TravelDestination(Pageable pageable);

    List<AccountBook> findByMember(Member member);

    @Query("select a from AccountBook a where a.member.id = :memberId order by a.startDate desc")
    List<AccountBook> findByMemberOrderByStartDateDesc(@Param("memberId") Long memberId); //가계부 최신순

    @Query("select a from AccountBook a where a.member.id = :memberId order by a.startDate asc")
    List<AccountBook> findByMemberOrderByStartDateAsc(@Param("memberId") Long memberId); //가계부 오래된순

    @Query("select ab from AccountBook ab join ab.expenses e where ab.member.id = :memberId group by ab.id order by sum(e.amount) desc")
    List<AccountBook> findAccountBooksByMemberIdOrderByTotalExpenseDesc(@Param("memberId") Long memberId); //가계부 높은 지출 순

    @Query("select ab from AccountBook ab join ab.expenses e where ab.member.id = :memberId group by ab.id order by sum(e.amount) asc")
    List<AccountBook> findAccountBooksByMemberIdOrderByTotalExpenseAsc(@Param("memberId") Long memberId); //가계부 낮은 지출순

}
