package com.melodymart.service;

import com.melodymart.model.BannerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BannerService {

    private static final String BANNER_FILE = "banner.json";

    @Autowired
    private FileStorageService fileStorageService;

    public BannerConfig getBannerConfig() {
        List<BannerConfig> list = fileStorageService.readList(BANNER_FILE, BannerConfig.class);
        if (list.isEmpty()) {
            BannerConfig defaultConfig = new BannerConfig();
            saveBannerConfig(defaultConfig);
            return defaultConfig;
        }
        return list.get(0);
    }

    public void saveBannerConfig(BannerConfig config) {
        fileStorageService.writeList(BANNER_FILE, List.of(config));
    }
}
