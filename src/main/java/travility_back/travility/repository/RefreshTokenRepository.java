package travility_back.travility.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import travility_back.travility.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    /**
     * Refresh Token 존재 여부
     */
    Boolean existsByRefresh(String refresh);

    /**
     * Refresh Token 삭제
     */
    @Transactional
    void deleteByRefresh(String refresh);
}
