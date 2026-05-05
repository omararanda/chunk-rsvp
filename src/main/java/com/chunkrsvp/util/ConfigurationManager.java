package com.chunkrsvp.util;

import com.chunkrsvp.cli.CliArguments;
import java.util.Properties;

public class ConfigurationManager {
    private final ConfigService configService;
    private RsvpConfig config;
    private final CliArguments cliArgs;

    public ConfigurationManager(ConfigService configService, CliArguments cliArgs) {
        this.configService = configService;
        this.cliArgs = cliArgs;
        this.config = resolveConfig();
    }

    private RsvpConfig resolveConfig() {
        Properties props = configService.load();
        
        int wpm = cliArgs.getWpm() != null ? cliArgs.getWpm() : Integer.parseInt(props.getProperty("wpm", "300"));
        double stopPerc = cliArgs.getSm() != null ? cliArgs.getSm() : Double.parseDouble(props.getProperty("perc.stop", "0.0"));
        double pausePerc = cliArgs.getPm() != null ? cliArgs.getPm() : Double.parseDouble(props.getProperty("perc.pause", "0.0"));
        int stopDelayMs = cliArgs.getSd() != null ? cliArgs.getSd() : Integer.parseInt(props.getProperty("delay.stop", "30"));
        int pauseDelayMs = cliArgs.getPd() != null ? cliArgs.getPd() : Integer.parseInt(props.getProperty("delay.pause", "10"));
        
        return new RsvpConfig(wpm, stopPerc, pausePerc, stopDelayMs, pauseDelayMs);
    }

    public RsvpConfig getConfig() {
        return config;
    }

    public void updateWpm(int wpm) {
        config = new RsvpConfig(wpm, config.stopPerc(), config.pausePerc(), config.stopDelayMs(), config.pauseDelayMs());
        persist();
    }

    private void persist() {
        Properties props = new Properties();
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
