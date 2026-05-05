package com.chunkrsvp.util;

import com.chunkrsvp.cli.CliArguments;
import java.util.Properties;
import java.util.HashSet;
import java.util.Set;

public class ConfigurationManager {
    private final ConfigService configService;
    private final DefaultConfigProvider defaults;
    private final CliArguments cliArgs;
    private RsvpConfig config;
    private final Set<String> transientKeys = new HashSet<>();

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
        
        return new RsvpConfig(wpm, stopPerc, pausePerc, stopDelayMs, pauseDelayMs);
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
        persist();
    }

    public void persist() {
        if (!configService.exists()) return;

        Properties props = configService.load();
        
        props.setProperty("wpm", String.valueOf(config.wpm()));
        props.setProperty("perc.stop", String.valueOf(config.stopPerc()));
        props.setProperty("perc.pause", String.valueOf(config.pausePerc()));
        props.setProperty("delay.stop", String.valueOf(config.stopDelayMs()));
        props.setProperty("delay.pause", String.valueOf(config.pauseDelayMs()));
        
        configService.save(props);
    }

    public boolean isHelp() { return cliArgs.isHelp(); }
    public boolean isInit() { return cliArgs.isInit(); }
}
