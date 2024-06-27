package travility_back.travility.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import travility_back.travility.entity.AccountBook;

public interface AccountBookRepository extends JpaRepository<AccountBook, Long> {
}