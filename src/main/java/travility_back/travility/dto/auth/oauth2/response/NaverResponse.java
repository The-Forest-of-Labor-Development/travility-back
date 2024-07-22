package travility_back.travility.dto.auth.oauth2.response;

import java.util.Map;

public class NaverResponse implements OAuth2Response {

    /**
     * {@code NaverResponse} 네이버로부터 받은 사용자 정보 처리
     */

    private final Map<String, Object> attribute;

    // 생성자
    public NaverResponse(Map<String, Object> attribute) {
        this.attribute = (Map<String, Object>) attribute.get("response");
    }

    @Override
    public String getProvider() {
        return "naver";
    }

    @Override
    public String getProviderId() {
        return attribute.get("id").toString();
    }

    @Override
    public String getEmail() {
        return attribute.get("email").toString();
    }

    @Override
    public String getName() {
        return attribute.get("name").toString();
    }
}
