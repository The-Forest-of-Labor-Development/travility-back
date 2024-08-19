package travility_back.travility.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import travility_back.travility.entity.Member;
import travility_back.travility.entity.enums.Role;

import java.time.LocalDateTime;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    /**
     * username 존재 여부
     */
    boolean existsByUsername(String username);

    /**
     * 회원 조회
     */
    Optional<Member> findByUsername(String username);

    /**
     * 총 회원 수 조회
     */
    long countByRole(Role role);

    /**
     * 신규 가입자 수 조회
     */
    @Query("select count(*) from Member m where m.createdDate >= :startDate and m.createdDate <= :endDate and m.role = :role")
    long countNewMembersToday(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("role") Role role); //오늘 날짜 신규 가입자 수

    /**
     * 회원 리스트 페이징 정렬 조회
     */
    Page<Member> findAllByRole(Role role, Pageable pageable);
}
