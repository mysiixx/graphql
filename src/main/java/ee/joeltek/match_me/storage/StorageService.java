package ee.joeltek.match_me.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    void init();

    void store(MultipartFile file, String filename);

    void delete(String filename);
}
