package com.cms.service;

import com.cms.model.Setting;
import com.cms.repository.SettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class SettingService {

    /** Keys that are accepted. Any key outside this set is silently ignored. */
    private static final Set<String> ALLOWED_KEYS = Set.of(
        "site_name", "footer_text", "logo_url",
        "connected_site_url", "detected_platform_type", "detected_platform_name",
        "detected_connector_status", "detected_summary", "detected_recommendation", "detected_updated_at",
        "detected_link_profiles",
        "social_twitter", "social_facebook", "social_instagram",
        "social_linkedin", "social_github"
    );

    private final SettingRepository settingRepository;

    public SettingService(SettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    public Map<String, String> getAll() {
        List<Setting> all = settingRepository.findAll();
        Map<String, String> result = new LinkedHashMap<>();
        for (Setting s : all) {
            result.put(s.getKey(), s.getValue());
        }
        return result;
    }

    @Transactional
    public Map<String, String> saveAll(Map<String, String> incoming) {
        for (Map.Entry<String, String> entry : incoming.entrySet()) {
            String key = entry.getKey();
            if (!ALLOWED_KEYS.contains(key)) continue;

            String value = entry.getValue() == null ? "" : entry.getValue().strip();
            settingRepository.save(new Setting(key, value));
        }
        return getAll();
    }
}
