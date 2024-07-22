package travility_back.travility.dto.auth.oauth2.response;

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

