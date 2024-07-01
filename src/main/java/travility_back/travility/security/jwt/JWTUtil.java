package travility_back.travility.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JWTUtil { //JWT 토큰 생성, 검증 메소드 클래스
    private SecretKey secretKey;

    public JWTUtil(@Value("${spring.jwt.secret}")String secret){ //비밀키 생성
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    //JWT에서 username 클레임 추출
    public String getUsername(String token){
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token) //token 파싱
                .getPayload() //페이로드 추출
                .get("username", String.class); //username 추출하여 문자열로 반환
    }

    //JWT에서 password 클레임 추출
    public String getRole(String token){
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class); //password 추출하여 문자열로 반환
    }

    //JWT 만료 검증
    public Boolean isExpired(String token){
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration() //만료 시간 추출
                    .before(new Date()); //현재 시간보다 이전인지 확인
        }catch(ExpiredJwtException e) {
            return true;
        }
    }

    public String createJwt(String username, String nickname, String role, Long expiredMs){
        Date date = new Date();
        Date tokenExpiryDate = new Date(date.getTime() + expiredMs);

        System.out.println(tokenExpiryDate);
        return Jwts.builder() //JWT 생성하기 위한 빌더 객체 반환. 아래는 설정들.
                .claim("username", username) //클레임 설정. username
                .claim("nickname", nickname) //클레임 설정. username
                .claim("role", role) //클레임 설정. role
                .issuedAt(date) //토큰 발급 시간
                .expiration(tokenExpiryDate) //토큰 만료 시간
                .signWith(secretKey) //비밀키로 서명 추가
                .compact(); //문자열로 빌드
    }
}