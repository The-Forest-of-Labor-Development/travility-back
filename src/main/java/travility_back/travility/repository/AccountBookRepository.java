package travility_back.travility.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import travility_back.travility.entity.AccountBook;
import travility_back.travility.entity.Member;

import java.util.List;

public interface AccountBookRepository extends JpaRepository<AccountBook, Long> {
    @Query("select ab.countryName from AccountBook ab group by ab.countryName order by count(ab) desc")
    List<String> findTop5TravelDestination(Pageable pageable);
    List<AccountBook> findByMember(Member member);
}
