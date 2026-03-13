package com.cms.service;

import com.cms.dto.SiteAnalysisResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.net.InetAddress;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class SiteAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(SiteAnalysisService.class);
    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public SiteAnalysisService(RestTemplateBuilder builder, ObjectMapper objectMapper) {
        this.restTemplate = builder
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = objectMapper;
    }

    public SiteAnalysisResponse analyze(String url) {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Gemini API key is not configured. Add GEMINI_API_KEY to your environment.");
        }
        validateHost(url);
        String fingerprint = fetchFingerprint(url);
        String prompt = buildPrompt(url, fingerprint);
        String analysisJson = callGemini(prompt);
        return parseAnalysis(url, analysisJson);
    }

    // ── SSRF guard ────────────────────────────────────────────────────────────

    private void validateHost(String rawUrl) {
        try {
            String scheme = URI.create(rawUrl).getScheme();
            if (scheme == null || (!scheme.equals("http") && !scheme.equals("https"))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "URL must use http or https");
            }
            String host = URI.create(rawUrl).getHost();
            if (host == null || host.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "URL has no host");
            }
            InetAddress addr = InetAddress.getByName(host);
            if (addr.isLoopbackAddress() || addr.isLinkLocalAddress() || addr.isSiteLocalAddress()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "URL resolves to a private or internal address");
            }
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid URL: " + ex.getMessage());
        }
    }

    // ── HTML fingerprint extraction ───────────────────────────────────────────

    private String fetchFingerprint(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (compatible; PantheonBot/1.0)")
                    .timeout(10_000)
                    .followRedirects(true)
                    .get();

            StringBuilder fp = new StringBuilder();

            fp.append("TITLE: ").append(doc.title()).append("\n");

            // Meta tags
            for (Element m : doc.select("meta[name], meta[property]")) {
                String name = m.attr("name").isBlank() ? m.attr("property") : m.attr("name");
                String content = m.attr("content");
                if (!name.isBlank() && !content.isBlank()) {
                    fp.append("META[").append(name).append("]: ")
                      .append(truncate(content, 150)).append("\n");
                }
            }

            // Scripts — strong platform fingerprint
            Elements scripts = doc.select("script[src]");
            fp.append("SCRIPTS: ");
            scripts.stream().limit(15)
                   .forEach(s -> fp.append(s.attr("src")).append(" | "));
            fp.append("\n");

            // Stylesheets
            Elements links = doc.select("link[rel=stylesheet]");
            fp.append("STYLES: ");
            links.stream().limit(8)
                 .forEach(l -> fp.append(l.attr("href")).append(" | "));
            fp.append("\n");

            // Headings
            fp.append("HEADINGS: ");
            doc.select("h1, h2").stream().limit(5)
               .forEach(h -> fp.append(h.text()).append(" | "));
            fp.append("\n");

            // Footer (WordPress, Wix, Squarespace often tag themselves here)
            Element footer = doc.selectFirst("footer");
            if (footer != null) {
                fp.append("FOOTER: ").append(truncate(footer.text(), 300)).append("\n");
            }

            // Body text sample
            if (doc.body() != null) {
                fp.append("BODY_SAMPLE: ")
                  .append(truncate(doc.body().text(), 600))
                  .append("\n");
            }

            return fp.toString();

        } catch (Exception ex) {
            log.warn("Could not fetch URL {}: {}", url, ex.getMessage());
            return "FETCH_ERROR: " + ex.getMessage();
        }
    }

    private String truncate(String text, int max) {
        if (text == null || text.isBlank()) return "";
        return text.length() > max ? text.substring(0, max) + "…" : text;
    }

    // ── Gemini prompt ─────────────────────────────────────────────────────────

    private String buildPrompt(String url, String fingerprint) {
        return """
                You are an expert web platform detection and analysis agent for Pantheon CMS.
                Analyse this website and return ONLY a valid JSON object — no extra text, no markdown fences.

                URL: %s

                HTML fingerprint:
                ---
                %s
                ---

                Return this exact JSON structure (camelCase keys, every field filled):
                {
                  "platformType": "one of: wordpress, ghost, shopify, woocommerce, blogger, wix, squarespace, webflow, custom_blog, custom_ecommerce, portfolio, news, social_profile, unknown",
                  "platformName": "human-readable platform name, e.g. WordPress, Ghost, Shopify",
                  "confidence": "HIGH, MEDIUM, or LOW",
                  "summary": "1-2 sentences describing what this site is and does",
                  "capabilities": ["list of things Pantheon could manage, e.g. Posts, Pages, Products, Media, Comments"],
                  "detectedFeatures": ["list of features visible on the site, e.g. Blog, E-commerce, Portfolio, Contact Form"],
                  "connectorStatus": "one of: full_api (well-known REST API exists), partial_api (limited API coverage), read_only (scrape only), none",
                  "recommendation": "one sentence on how Pantheon should best connect to this site"
                }
                """.formatted(url, fingerprint);
    }

    // ── Gemini API call ───────────────────────────────────────────────────────

    private String callGemini(String prompt) {
        String uri = GEMINI_URL + "?key=" + geminiApiKey;

        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of(
                        "responseMimeType", "application/json",
                        "temperature", 0.1
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    uri, new HttpEntity<>(body, headers), String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode candidates = root.path("candidates");
            if (!candidates.isArray() || candidates.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "Gemini returned no candidates");
            }
            JsonNode parts = candidates.get(0).path("content").path("parts");
            if (!parts.isArray() || parts.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "Gemini returned empty content");
            }
            return parts.get(0).path("text").asText();

        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Gemini API call failed: {}", ex.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Gemini analysis failed: " + ex.getMessage());
        }
    }

    // ── Parse Gemini response ─────────────────────────────────────────────────

    private SiteAnalysisResponse parseAnalysis(String url, String json) {
        try {
            SiteAnalysisResponse response = objectMapper.readValue(json, SiteAnalysisResponse.class);
            response.setUrl(url);
            return response;
        } catch (Exception ex) {
            log.error("Failed to parse Gemini response: {}", ex.getMessage());
            SiteAnalysisResponse fallback = new SiteAnalysisResponse();
            fallback.setUrl(url);
            fallback.setPlatformType("unknown");
            fallback.setPlatformName("Unknown");
            fallback.setConfidence("LOW");
            fallback.setSummary("Analysis could not be parsed. The site may be blocking automated access.");
            fallback.setConnectorStatus("none");
            fallback.setRecommendation("Try again or check that the URL is publicly accessible.");
            return fallback;
        }
    }
}
