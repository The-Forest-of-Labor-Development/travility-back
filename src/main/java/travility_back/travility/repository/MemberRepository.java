package travility_back.travility.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import travility_back.travility.entity.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByUsername(String username);
    Optional<Member> findByUsername(String username);

}
