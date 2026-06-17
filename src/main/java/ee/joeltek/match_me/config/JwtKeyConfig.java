package ee.joeltek.match_me.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class JwtKeyConfig {

    @Value("${jwt.public.key.path}")
    private Resource publicKeyResource;

    @Value("${jwt.private.key.path}")
    private Resource privateKeyResource;

    @Bean
    public RSAPublicKey rsaPublicKey() throws Exception {
        return loadPublicKey(publicKeyResource);
    }

    @Bean
    public RSAPrivateKey rsaPrivateKey() throws Exception {
        return loadPrivateKey(privateKeyResource);
    }

    private RSAPublicKey loadPublicKey(Resource resource) throws Exception {

        String publicKeyString = readResource(resource);
        publicKeyString = publicKeyString
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] decoded = Base64.getDecoder().decode(publicKeyString);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(decoded));
    }

    private RSAPrivateKey loadPrivateKey(Resource resource) throws Exception {

        String privateKeyString = readResource(resource);
        privateKeyString = privateKeyString
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] decoded = Base64.getDecoder().decode(privateKeyString);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return (RSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decoded));
    }

    private String readResource(Resource resource) throws Exception {
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
