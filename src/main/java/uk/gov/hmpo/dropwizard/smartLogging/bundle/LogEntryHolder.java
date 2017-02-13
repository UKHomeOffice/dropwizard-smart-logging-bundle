package uk.gov.hmpo.dropwizard.smartLogging.bundle;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cgrimble on 20/01/17.
 */
public class LogEntryHolder {
    private static Map<String, String> extraFields = new HashMap<>();
    private static String useHeader = "";

    public static String getUseHeader() {
        return useHeader;
    }

    public static void setUseHeader(String useHeader) {
        LogEntryHolder.useHeader = useHeader;
    }

    public static Map<String, String> getExtraFields() {
        return extraFields;
    }

    public static void setExtraFields(Map<String, String> extraFields) {
        LogEntryHolder.extraFields = extraFields;
    }
}
