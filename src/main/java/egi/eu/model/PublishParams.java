package egi.eu.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;


/**
 * Optional parameters for publish action
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PublishParams {

    @Schema(title="The publisher of the data")
    public String publisher;

    @Schema(title="The owner of the data")
    public String sovereign;

    @Schema(title="The language of the data")
    public String language;

    @Schema(title="Link to the license for the data")
    public String license;

    @Schema(title="Keywords for the data")
    public List<String> keywords;

    @Schema(title="Access policy for the data")
    public PublishPolicy policy;
}
