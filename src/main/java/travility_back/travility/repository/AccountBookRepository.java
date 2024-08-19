package travility_back.travility.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import travility_back.travility.entity.AccountBook;

import java.util.List;

public interface AccountBookRepository extends JpaRepository<AccountBook, Long> {

    /**
     * 가계부 목록 조회
     */
    @Query("select a from AccountBook a where a.member.id = :memberId")
    List<AccountBook> findByMemberId(@Param("memberId") Long memberId);

    /**
     * 가계부 최신순 조회
     */
    @Query("select a from AccountBook a where a.member.id = :memberId order by a.startDate desc")
    List<AccountBook> findByMemberOrderByStartDateDesc(@Param("memberId") Long memberId);

    /**
     * 가계부 오래된순 조회
     */
    @Query("select a from AccountBook a where a.member.id = :memberId order by a.startDate asc")
    List<AccountBook> findByMemberOrderByStartDateAsc(@Param("memberId") Long memberId);
}
