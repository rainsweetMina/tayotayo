package kroryi.bus2.repository.jpa.apikey;

import kroryi.bus2.entity.apikey.ApiKeyCallbackUrl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApiKeyCallbackUrlRepository extends JpaRepository<ApiKeyCallbackUrl, Long> {
    List<ApiKeyCallbackUrl> findByApiKey_Apikey(String apiKey);
}