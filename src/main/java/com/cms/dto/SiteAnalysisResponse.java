package com.cms.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SiteAnalysisResponse {

    private String url;
    private String platformType;
    private String platformName;
    private String confidence;
    private String summary;
    private List<String> capabilities = new ArrayList<>();
    private List<String> detectedFeatures = new ArrayList<>();
    private String connectorStatus;
    private String recommendation;

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getPlatformType() { return platformType; }
    public void setPlatformType(String platformType) { this.platformType = platformType; }

    public String getPlatformName() { return platformName; }
    public void setPlatformName(String platformName) { this.platformName = platformName; }

    public String getConfidence() { return confidence; }
    public void setConfidence(String confidence) { this.confidence = confidence; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public List<String> getCapabilities() { return capabilities; }
    public void setCapabilities(List<String> capabilities) {
        this.capabilities = capabilities != null ? capabilities : new ArrayList<>();
    }

    public List<String> getDetectedFeatures() { return detectedFeatures; }
    public void setDetectedFeatures(List<String> detectedFeatures) {
        this.detectedFeatures = detectedFeatures != null ? detectedFeatures : new ArrayList<>();
    }

    public String getConnectorStatus() { return connectorStatus; }
    public void setConnectorStatus(String connectorStatus) { this.connectorStatus = connectorStatus; }

    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
}
