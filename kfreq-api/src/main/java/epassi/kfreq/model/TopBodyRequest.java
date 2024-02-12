package epassi.kfreq.model;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Top words source request parameters")
public class TopBodyRequest {
    @Schema(description = "URL of source S3 object",
        example = "https://s3.eu-north-1.amazonaws.com/dev.01/epassi/steinbeck.txt")
    private String resourceUrl;

    @Schema(description = "Most frequent words limit",
        example = "8")
    private int limit;

    @Schema(description = "Encoding of source text (optional)",
        example = "UTF-8",
        defaultValue = "UTF-8")
    private String encoding;

    @Schema(description = "Use stop words which are supposed not to be included into counting (optional)",
        example = "true",
        defaultValue = "true")
    private boolean useStopWords = true;

    public String getResourceUrl() {
        return resourceUrl;
    }

    public void setResourceUrl(String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public boolean isUseStopWords() {
        return useStopWords;
    }

    public void setUseStopWords(boolean useStopWords) {
        this.useStopWords = useStopWords;
    }
}

