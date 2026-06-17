package ee.joeltek.match_me.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest
public class JwtPathTest {

    @Value("${jwt.private.key.path}")
    private Resource privateKeyResource;

    @Value("${jwt.public.key.path}")
    private Resource publicKeyResource;

    @Test
    void isJwtPrivateKeyAccessible() {
        assertTrue(privateKeyResource.exists(),
                "JWT private key file does not exist: " + getResourceDescription(privateKeyResource));
    }

    @Test
    void isJwtPublicKeyAccessible() {
        assertTrue(publicKeyResource.exists(),
                "JWT public key file does not exist: " + getResourceDescription(publicKeyResource));
    }

    @Test
    void isJwtPrivateKeyReadable() {
        assertTrue(privateKeyResource.isReadable(),
                "Private key is unreadable: " + getResourceDescription(privateKeyResource));
    }

    @Test
    void isJwtPublicKeyReadable() {
        assertTrue(publicKeyResource.isReadable(),
                "Public key is unreadable: " + getResourceDescription(publicKeyResource));
    }

    @Test
    void isJwtPrivateKeyRegularFile() throws Exception {
        assertTrue(privateKeyResource.getFile().isFile(),
                getResourceDescription(privateKeyResource) + " is not a regular file.");
    }

    @Test
    void isJwtPublicKeyRegularFile() throws Exception {
        assertTrue(publicKeyResource.getFile().isFile(),
                getResourceDescription(publicKeyResource) + " is not a regular file.");
    }

    private String getResourceDescription(Resource resource) {
        try {
            return resource.getFile().getAbsolutePath();
        } catch (Exception e) {
            return resource.getDescription();
        }
    }
}