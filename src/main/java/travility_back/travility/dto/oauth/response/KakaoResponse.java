package travility_back.travility.dto.oauth.response;

import java.util.Map;

public class KakaoResponse implements OAuth2Response {
    /**
     * {@code KakaoResponse} 카카오로부터 받은 사용자 정보 처리
     * <p>카카오 사용자 정보에서 kakao_account와 profile 정보를 가져옴.</p>
     * <blockquote><pre>
     *     profile : 카카오는 profile 필드 안에 nickname이 있어서 profile에서 정보 빼옴.
     * </pre></blockquote>
     */

    public KakaoResponse(Map<String, Object> attribute) {
        this.attribute = attribute;
        this.kakaoAccount = (Map<String, Object>) attribute.get("kakao_account");
        this.profile = (Map<String, Object>) kakaoAccount.get("profile"); // 카카오는 profile 필드안에 nickname이 있음
    }

    private final Map<String, Object> attribute;
    private final Map<String, Object> kakaoAccount;

    private final Map<String, Object> profile;

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getProviderId() {
        return attribute.get("id").toString();
    }

    @Override
    public String getEmail() {
        return kakaoAccount.get("email").toString();
    }

    // 카카오톡 사용자가 정한 이름 가져오기. 카카오는 실명 못가져옴
    @Override
    public String getName() {
        return profile.get("nickname").toString();
    }
}
