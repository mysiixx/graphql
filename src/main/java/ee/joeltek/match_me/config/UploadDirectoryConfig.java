package ee.joeltek.match_me.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class UploadDirectoryConfig implements WebMvcConfigurer {
    @Value("${app.upload-url-prefix}")
    public String uploadUrlPrefix;
    @Value("${app.upload-dir}")
    public String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(uploadUrlPrefix + "/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}
