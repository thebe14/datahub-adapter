package idsa.connector;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "idsa")
public interface ConnectorConfig {

    String username();
    String password();

    @WithName("connector")
    String connectorBaseUrl();
}
