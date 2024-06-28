package travility_back.travility.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import travility_back.travility.entity.AccountBook;

import java.util.List;

public interface AccountBookRepository extends JpaRepository<AccountBook, Long> {

    @Query("select a from AccountBook a where a.member.id = :memberId")
    List<AccountBook> findByMemberId(@Param("memberId") Long memberId);
}
