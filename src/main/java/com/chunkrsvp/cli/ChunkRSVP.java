package com.chunkrsvp.cli;

import com.chunkrsvp.engine.RSVPEngine;
import com.chunkrsvp.model.Chunk;
import com.chunkrsvp.util.ConfigService;
import com.chunkrsvp.util.ConfigurationManager;
import java.io.*;
import java.util.List;

public class ChunkRSVP {
    public static void main(String[] args) {
        CliArguments cli = CliParser.parse(args);
        ConfigurationManager configManager = new ConfigurationManager(
            new ConfigService(System.getProperty("user.home") + "/.config/chunk-rsvp/config.properties"),
            cli,
            new com.chunkrsvp.util.DefaultConfigProvider()
        );

        if (configManager.isHelp()) { printHelp(); return; }
        if (configManager.isInit()) { initializeConfig(); return; }

        List<Chunk> chunks = ChunkLoader.load(System.in);
        if (chunks.isEmpty()) { System.err.println("No chunks found."); return; }

        RSVPEngine engine = new RSVPEngine(configManager, new com.chunkrsvp.cli.ui.AnsiTerminalView());

        try (FileInputStream tty = new FileInputStream("/dev/tty")) {
            engine.run(chunks, tty);
        } catch (Exception e) {
            System.err.println("Error reading tty: " + e.getMessage());
        }
        System.out.println("Reading complete.");
    }

    private static void printHelp() {
        System.out.println("RSVP CLI - Rapid Serial Visual Presentation Reader\n" +
            "--------------------------------------------------\n" +
            "Usage: rsvp [OPTIONS]\n" +
            "Options:\n" +
            "  -h, --help                          Show this help message\n" +
            "  --init                              Generate default config file\n" +
            "  -wpm, --words-per-minute=WPM        Set base words per minute\n" +
            "  -sm, --stop-multiplier=PERCENT      Stop punctuation increase (percentage of chunk time)\n" +
            "  -pm, --pause-multiplier=PERCENT     Pause punctuation increase (percentage of chunk time)\n" +
            "  -sd, --stop-delay=MS                Stop punctuation delay in ms\n" +
            "  -pd, --pause-delay=MS               Pause punctuation delay in ms\n\n" +
            "Precedence: Millisecond delays (--stop-delay, --pause-delay) take priority over percentage multipliers.\n\n" +
            "Configuration File:\n" +
            "  Location: ~/.config/chunk-rsvp/config.properties\n" +
            "  Format:   wpm=300, delay.stop=30, delay.pause=10\n\n" +
            "Controls:\n" +
            "  [↑] Increase WPM | [↓] Decrease WPM | [←] Back 5 chunks");
    }

    private static void initializeConfig() {
        File dir = new File(System.getProperty("user.home") + "/.config/chunk-rsvp");
        File configFile = new File(dir, "config.properties");
        if (dir.exists() && configFile.exists()) { System.err.println("Config already exists."); return; }
        try { dir.mkdirs(); try (PrintWriter out = new PrintWriter(configFile)) { out.println("wpm=300\ndelay.stop=50\ndelay.pause=20"); } System.out.println("Initialized: " + configFile.getAbsolutePath()); } catch (Exception e) { System.err.println("Error: " + e.getMessage()); }
    }
}

