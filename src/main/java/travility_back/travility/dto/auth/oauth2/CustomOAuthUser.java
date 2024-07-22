package travility_back.travility.dto.auth.oauth2;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import travility_back.travility.dto.auth.oauth2.response.GoogleOAuth2LoginDto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@RequiredArgsConstructor
public class CustomOAuthUser implements OAuth2User {

    /**
     * 사용자의 권한 및 이름 반환 메서드
     * <blockquote><pre>
     *     String provider : 사용자가 어떤 소셜로그인 공급자(naver, kakao)를 통해 인증되었는지 확인하기 위해 주입.
     *     이런거 하나 주입해줘야 다른 소셜로그인 처리를 하나의 클래스에서 할 수 있음.
     * </pre></blockquote>
     */
    private final NaverOAuth2LoginDto naverOAuth2LoginDto; // naver
    private final KakaoOAuth2LoginDto kakaoOAuth2LoginDto; // kakao
    private final GoogleOAuth2LoginDto googleOAuth2LoginDto; // google
    private final String provider; // 사용자가 뭘로 로그인 인증했는지 저장하는거.

    // 네이버 로그인 생성자
    public CustomOAuthUser(NaverOAuth2LoginDto naverOAuth2LoginDto) {
        this.naverOAuth2LoginDto = naverOAuth2LoginDto; // 네이버 로그인이니까 공급자는 네이버
        this.kakaoOAuth2LoginDto = null; // 카카오는 설정안함
        this.googleOAuth2LoginDto = null; // 구글은 설정안함
        this.provider = "naver";
    }

    // 카카오 로그인 생성자
    public CustomOAuthUser(KakaoOAuth2LoginDto kakaoOAuth2LoginDto) {
        this.naverOAuth2LoginDto = null;
        this.kakaoOAuth2LoginDto = kakaoOAuth2LoginDto;
        this.googleOAuth2LoginDto = null; // 구글은 설정안함
        this.provider = "kakao";
    }

    // 구글 로그인 생성자
    public CustomOAuthUser(GoogleOAuth2LoginDto googleOAuth2LoginDto) {
        this.naverOAuth2LoginDto = null;
        this.kakaoOAuth2LoginDto = null;
        this.googleOAuth2LoginDto = googleOAuth2LoginDto;
        this.provider = "google";
    }

    // 사용자 속성 반환 (사용안함)
    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    // 사용자에게 부여된 권한 목록
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                if ("naver".equals(provider)) {
                    return naverOAuth2LoginDto.getRole().name();
                }
                else if ("kakao".equals(provider)) {
                    return kakaoOAuth2LoginDto.getRole().name();
                }
                else if ("google".equals(provider)) {
                    return googleOAuth2LoginDto.getRole().name();
                }
                return null;
            }
        });

        return collection;
    }

    // 사용자 이름(실명) 반환 (카카오 : 사용자 지정 이름(nickname)
    @Override
    public String getName() {
        if ("naver".equals(provider)) {
            return naverOAuth2LoginDto.getName();
        }
        else if ("kakao".equals(provider)) {
            return kakaoOAuth2LoginDto.getName();
        }
        else if ("google".equals(provider)) {
            return googleOAuth2LoginDto.getName();
        }
        return null;
    }

    // 사용자명 반환
    public String getUsername() {
        if ("naver".equals(provider)) {
            return naverOAuth2LoginDto.getUsername();
        }
        else if ("kakao".equals(provider)) {
            return kakaoOAuth2LoginDto.getUsername();
        }
        else if ("google".equals(provider)) {
            return googleOAuth2LoginDto.getUsername();
        }
        return null;
    }
}
