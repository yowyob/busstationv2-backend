package cm.yowyob.bus_station_backend.infrastructure.config.email;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.email")
@Data
public class EmailConfig {
    // Exactement comme ton ancien EmailTemplateConfig
    private String fromEmail;
    private String fromName;
    private String supportEmail;
    private String baseUrl;
    private String logoUrl;
    private String primaryColor;
    private String secondaryColor;
    private String successColor;
    private String errorColor;
    private String warningColor;
}
