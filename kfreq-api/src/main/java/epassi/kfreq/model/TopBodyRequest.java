package epassi.kfreq.model;


import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.lang.NonNull;

import java.util.Optional;

@Schema(description = "Top words source request parameters")
public class TopBodyRequest {
    @Schema(description = "URL of source S3 object",
        example = "https://s3.eu-north-1.amazonaws.com/dev.01/epassi/steinbeck.txt")
    private String resourceUrl;

    @Schema(description = "Most frequent words limit (> 0)",
        example = "8")
    private Integer limit;

    @Schema(description = "Encoding of source text (optional)",
        example = "UTF-8",
        defaultValue = "UTF-8")
    private String encoding;

    @Schema(description = "Use stop words which are supposed not to be included into counting (optional)",
        example = "true",
        defaultValue = "true")
    private Boolean useStopWords;

    public TopBodyRequest(String resourceUrl, Integer limit, String encoding, Boolean useStopWords) {
        this.resourceUrl = resourceUrl;
        this.limit = limit;
        this.encoding = encoding;
        this.useStopWords = useStopWords;
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

    public void setResourceUrl(String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public Boolean isUseStopWords() {
        return useStopWords;
    }

    public void setUseStopWords(Boolean useStopWords) {
        this.useStopWords = useStopWords;
    }
}

