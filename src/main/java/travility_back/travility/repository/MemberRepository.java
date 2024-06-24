package travility_back.travility.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import travility_back.travility.entity.Member;
import travility_back.travility.entity.enums.Role;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByUsername(String username); //username 존재 여부
    Optional<Member> findByUsername(String username); //username 회원 가져오기
    List<Member> findByRole(Role role);
    long countByRole(Role role);
    @Query("select count(*) from Member m where m.createdDate >= :startDate and m.createdDate <= :endDate and m.role = :role")
    long countNewMembersToday(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("role") Role role); //오늘 날짜 신규 가입자 수
}
