package uk.gov.hmpo.dropwizard.smartLogging.bundle;


import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Holder for HTTP header prefix
 */
public class SmartLogging {

    @JsonProperty
    public String useHeader;

    public void setUseHeader(String useHeader) {
        this.useHeader = useHeader;
    }
}
