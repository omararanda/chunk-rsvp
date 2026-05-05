package com.chunkrsvp.util;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

public class MockConfigService extends ConfigService {
    public final AtomicInteger saveCount = new AtomicInteger(0);
    private final boolean exists;

    public MockConfigService(boolean exists) {
        super("mock.properties");
        this.exists = exists;
    }

    @Override
    public boolean exists() {
        return exists;
    }

    @Override
    public Properties load() {
        Properties p = new Properties();
        p.setProperty("wpm", "300");
        return p;
    }

    @Override
    public void save(Properties props) {
        saveCount.incrementAndGet();
    }
}
