package ee.joeltek.match_me;

import ee.joeltek.match_me.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StartupConfigurations implements CommandLineRunner {
    private final StorageService storageService;

    @Override
    public void run(String... args) throws Exception {

        //Initialize file storage
        storageService.init();
        System.out.println("File storage initialized.");
    }
}
