package com.chunkrsvp.util;

import com.chunkrsvp.cli.CliArguments;
import java.util.Properties;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ConfigurationManager {
    private final ConfigService configService;
    private final DefaultConfigProvider defaults;
    private final CliArguments cliArgs;
    private RsvpConfig config;
    private final Set<String> transientKeys = new HashSet<>();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> pendingSave;

    public ConfigurationManager(ConfigService configService, CliArguments cliArgs, DefaultConfigProvider defaults) {
        this.configService = configService;
        this.cliArgs = cliArgs;
        this.defaults = defaults;
        this.config = resolveConfig();
    }

    private RsvpConfig resolveConfig() {
        Properties props = configService.load();
        
        int wpm = resolveValue(cliArgs.getWpm(), props.getProperty("wpm"), defaults.getWpm(), "wpm");
        double stopPerc = resolveValue(cliArgs.getSm(), props.getProperty("perc.stop"), defaults.getStopPerc(), "perc.stop");
        double pausePerc = resolveValue(cliArgs.getPm(), props.getProperty("perc.pause"), defaults.getPausePerc(), "perc.pause");
        int stopDelayMs = resolveValue(cliArgs.getSd(), props.getProperty("delay.stop"), defaults.getStopDelayMs(), "delay.stop");
        int pauseDelayMs = resolveValue(cliArgs.getPd(), props.getProperty("delay.pause"), defaults.getPauseDelayMs(), "delay.pause");
        boolean noControls = cliArgs.isNoControls();
        
        return new RsvpConfig(wpm, stopPerc, pausePerc, stopDelayMs, pauseDelayMs, noControls);
    }

    private <T> T resolveValue(T cli, String file, T def, String key) {
        if (cli != null) {
            transientKeys.add(key);
            return cli;
        }
        if (file != null) {
            if (def instanceof Integer) return (T) Integer.valueOf(file);
            if (def instanceof Double) return (T) Double.valueOf(file);
            return (T) file;
        }
        return def;
    }

    public RsvpConfig getConfig() { return config; }

    public void updateConfig(RsvpConfig newConfig) {
        this.config = newConfig;
        schedulePersist();
    }

    private synchronized void schedulePersist() {
        if (pendingSave != null && !pendingSave.isDone()) {
            pendingSave.cancel(false);
        }
        pendingSave = executor.schedule(this::persist, 500, TimeUnit.MILLISECONDS);
    }

    public synchronized void persist() {
        if (!configService.exists()) return;

        Properties props = configService.load();
        boolean changed = false;
        
        changed |= updateIfPersistent(props, "wpm", String.valueOf(config.wpm()));
        changed |= updateIfPersistent(props, "perc.stop", String.valueOf(config.stopPerc()));
        changed |= updateIfPersistent(props, "perc.pause", String.valueOf(config.pausePerc()));
        changed |= updateIfPersistent(props, "delay.stop", String.valueOf(config.stopDelayMs()));
        changed |= updateIfPersistent(props, "delay.pause", String.valueOf(config.pauseDelayMs()));
        
        if (changed) {
            configService.save(props);
        }
    }

    private boolean updateIfPersistent(Properties props, String key, String value) {
        if (!transientKeys.contains(key) && props.containsKey(key)) {
            if (!value.equals(props.getProperty(key))) {
                props.setProperty(key, value);
                return true;
            }
        }
        return false;
    }

    public void shutdown() {
        synchronized (this) {
            if (pendingSave != null && !pendingSave.isDone()) {
                pendingSave.cancel(false);
                persist();
            }
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public boolean isHelp() { return cliArgs.isHelp(); }
    public boolean isInit() { return cliArgs.isInit(); }
}
