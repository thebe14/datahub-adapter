package egi.eu;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "datahub")
public interface ActionConfig {

    @WithName("onezone-host")
    String zoneBaseUrl();

    @WithName("onezone-token")
    String zoneToken();

    @WithName("oneprovider-token")
    String providerToken();

    @WithName("file-token-validity-days")
    int fileTokenValidityDays();
}
