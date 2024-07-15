package travility_back.travility.dto.oauth.response;

public interface OAuth2Response {

    // provider (naver, google, kakao, ...)
    String getProvider();

    // ID
    String getProviderId();

    // email
    String getEmail();

    // name
    String getName();
}

