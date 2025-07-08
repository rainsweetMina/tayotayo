package kroryi.bus2.components;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "redirect")
@Getter
@Setter
public class RedirectProperties {
    private String baseUrl;
    private String adminPath;
    private String userPath;

    public String getAdminUrl() {
        return baseUrl + adminPath;
    }

    public String getUserUrl() {
        return baseUrl + userPath;
    }

    @PostConstruct
    public void init() {
        System.out.println("--------------------------------------------- baseUrl = " + baseUrl);
        System.out.println("--------------------------------------------- adminUrl = " + getAdminUrl());
        System.out.println("--------------------------------------------- userUrl = " + getUserUrl());
    }
}