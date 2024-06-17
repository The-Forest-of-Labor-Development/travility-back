package travility_back.travility.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import travility_back.travility.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByUsername(String username);
}
