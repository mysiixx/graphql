package ee.joeltek.match_me.dev;

import ee.joeltek.match_me.location.LocationSource;
import ee.joeltek.match_me.location.UserLocationService;
import ee.joeltek.match_me.location.dto.UpdateUserLocationRequest;
import ee.joeltek.match_me.location.dto.UserLocationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@Profile("dev")
@Controller
public class DevController {

    private final UserLocationService userLocationService;
    private final SeederService seederService;

    @GetMapping(value = "/seeder", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String simpleDataAdministrationPanel() {
        return renderPage(null, null, null);
    }

    @PostMapping(value = "/seeder", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String saveUserLocation(
            @RequestParam String action,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Integer preferredRadiusMeters,
            @RequestParam(required = false) LocationSource source
    ) {
        switch (action) {
            case "saveLocation":
                try {
                    UpdateUserLocationRequest request = new UpdateUserLocationRequest(
                            latitude,
                            longitude,
                            preferredRadiusMeters,
                            source
                    );

                    UserLocationResponse response = userLocationService.updateUserLocation(userId, request);
                    return renderPage(response, null, null);
                } catch (ResponseStatusException ex) {
                    return renderPage(null, null, ex.getReason());
                } catch (RuntimeException ex) {
                    return renderPage(null, null, ex.getMessage());
                }
            case "clearData":
                try {
                    seederService.clearData();
                    return renderPage(null, "Data cleared successfully.", null);
                } catch (ResponseStatusException ex) {
                    return renderPage(null,null, ex.getReason());
                } catch (RuntimeException ex) {
                    return renderPage(null,null,  ex.getMessage());
                }
            case "seed20":
                try {
                    seederService.seed20Users();
                    return renderPage(null, "20 users created successfully.", null);
                } catch (ResponseStatusException ex) {
                    return renderPage(null,null, ex.getReason());
                } catch (RuntimeException ex) {
                    return renderPage(null,null,  ex.getMessage());
                }
            case "seed200":
                try {
                    seederService.seed200Users();
                    return renderPage(null, "200 users created successfully.", null);
                } catch (ResponseStatusException ex) {
                    return renderPage(null,null, ex.getReason());
                } catch (RuntimeException ex) {
                    return renderPage(null,null,  ex.getMessage());
                }
            default:
                return renderPage(null, null, null);
        }
    }

    private String renderPage(UserLocationResponse response, String successMessage, String errorMessage) {
        String resultSection = "";
        if (response != null) {
            resultSection = """
                    <section style="margin-top: 24px; padding: 16px; border: 1px solid #1f2937; border-radius: 8px;">
                      <h2 style="margin-top: 0;">Saved location</h2>
                      <p><strong>User ID:</strong> %d</p>
                      <p><strong>Longitude:</strong> %.6f</p>
                      <p><strong>Latitude:</strong> %.6f</p>
                      <p><strong>Preferred radius meters:</strong> %d</p>
                      <p><strong>Source:</strong> %s</p>
                      <p><strong>Updated at:</strong> %s</p>
                    </section>
                    """.formatted(
                    response.getUserId(),
                    response.getLongitude(),
                    response.getLatitude(),
                    response.getPreferredRadiusMeters(),
                    response.getSource(),
                    response.getUpdatedAt()
            );
        }
        String successSection = "";
        if (successMessage != null && !successMessage.isBlank()) {
            successSection = """
                    <section style="margin-top: 24px; padding: 16px; border: 1px solid #006622; background: #d9f2d9; color: #006622; border-radius: 8px;">
                      <strong>Ok:</strong> %s
                    </section>
                    """.formatted(successMessage);
        }

        String errorSection = "";
        if (errorMessage != null && !errorMessage.isBlank()) {
            errorSection = """
                    <section style="margin-top: 24px; padding: 16px; border: 1px solid #991b1b; background: #fef2f2; color: #991b1b; border-radius: 8px;">
                      <strong>Error:</strong> %s
                    </section>
                    """.formatted(errorMessage);
        }

        return """
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <title>Dev Seeder</title>
                </head>
                <body style="font-family: sans-serif; max-width: 720px; margin: 40px auto; line-height: 1.5;">
                    <section id="dev-ui">
                        <h1>Dev Location Seeder</h1>
                        <p>Save a location for an existing user.</p>

                        <form method="post" action="/seeder" style="display: grid; gap: 12px;">
                            <input type="hidden" name="action" value="saveLocation" />
                            <label>
                                User ID
                                <input type="number" name="userId" required />
                            </label>
                            <label>
                                Longitude
                                <input type="number" step="any" name="longitude" required />
                            </label>
                            <label>
                                Latitude
                                <input type="number" step="any" name="latitude" required />
                            </label>
                            <label>
                                Preferred radius meters
                                <input type="number" name="preferredRadiusMeters" min="10000" max="20000000" required />
                            </label>
                            <label>
                                Source
                                <select name="source" required>
                                    <option value="BROWSER">BROWSER</option>
                                    <option value="MANUAL">MANUAL</option>
                                </select>
                            </label>
                            <button type="submit" style="width: fit-content; padding: 8px 14px;">Save location</button>
                        </form>
                        <h3>Start Match-me options.</h3>
                        <section style="display: grid; grid-template-columns: auto auto auto;">
                            <form method="post" action="/seeder" style="display: grid; gap: 12px;">
                                <input type="hidden" name="action" value="clearData" />
                                <button type="submit" style="width: fit-content; padding: 8px 14px;">0 users</button>
                            </form>
                            <form method="post" action="/seeder" style="display: grid; gap: 12px;">
                                <input type="hidden" name="action" value="seed20" />
                                <button type="submit" style="width: fit-content; padding: 8px 14px;">20 users</button>
                            </form>
                            <form method="post" action="/seeder" style="display: grid; gap: 12px;">
                                <input type="hidden" name="action" value="seed200" />
                                <button type="submit" style="width: fit-content; padding: 8px 14px;">200 users</button>
                            </form>
                        </section>
                    </section>
                    <p id="working-message" style="display: none; margin-top: 16px; font-weight: bold;">...working</p>
                    %s
                    %s
                    %s
                    <script>
                        document.addEventListener("DOMContentLoaded", function () {
                            const forms = document.querySelectorAll("form");
                            const controls = document.querySelectorAll("#dev-ui button");
                            const workingMessage = document.getElementById("working-message");

                            forms.forEach(function (form) {
                                form.addEventListener("submit", function () {
                                    controls.forEach(function (control) {
                                        control.disabled = true;
                                    });
                                    workingMessage.style.display = "block";
                                });
                            });
                        });
                    </script>
                </body>
                </html>
                """.formatted(resultSection, successSection, errorSection);
    }
}
