package travility_back.travility.config;

import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        registry.addMapping("/**") // 특정한 모든경로에서 매핑 진행
                .exposedHeaders("Set-Cookie") // 노출할 헤더값 : 쿠키헤더
                .allowedOrigins("http://localhost:3000"); // 오리진 허용 : 프론트단 uri
    }
}
