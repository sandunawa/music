package com.melodymart.service;

import com.melodymart.model.TickerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TickerService {

    private static final String TICKER_FILE = "ticker.json";

    @Autowired
    private FileStorageService fileStorageService;

    public TickerConfig getTickerConfig() {
        List<TickerConfig> list = fileStorageService.readList(TICKER_FILE, TickerConfig.class);
        if (list.isEmpty()) {
            TickerConfig defaultConfig = new TickerConfig();
            saveTickerConfig(defaultConfig);
            return defaultConfig;
        }
        return list.get(0);
    }

    public void saveTickerConfig(TickerConfig config) {
        fileStorageService.writeList(TICKER_FILE, List.of(config));
    }
}
