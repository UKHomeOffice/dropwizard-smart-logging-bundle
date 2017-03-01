package uk.gov.hmpo.dropwizard.smartLogging.bundle;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Holder for HTTP header prefix
 */
public class SmartLogging {

    @JsonProperty
    public String useHeader;

    @JsonProperty
    public RequestLoggingFilter requestLoggingFilter = new RequestLoggingFilter();

    @JsonProperty
    public Map<String, String> extraFields = new HashMap<>();

    public void setExtraFields(Map<String, String> extraFields) {
        this.extraFields = extraFields;
    }

    public void setUseHeader(String useHeader) {
        this.useHeader = useHeader;
    }

    static class RequestLoggingFilter {
        @JsonProperty
        public String[] excluded  = new String[] {"^/-1$"}; //just an unlikely url to ever match
    }
}
