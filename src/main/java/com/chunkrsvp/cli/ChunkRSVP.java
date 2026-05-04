package com.chunkrsvp;

import com.chunkrsvp.engine.RSVPEngine;
import com.chunkrsvp.model.Chunk;
import com.chunkrsvp.util.ConfigService;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChunkRSVP {
    public static void main(String[] args) {
        Integer wpm = null;
        Double sm = null, pm = null;
        Integer sd = null, pd = null;
        for (String arg : args) {
            if (arg.equals("-h") || arg.equals("--help")) { printHelp(); return; }
            else if (arg.equals("--init")) { initializeConfig(); return; }
            else if (arg.startsWith("-wpm=") || arg.startsWith("--words-per-minute=")) { int val = Integer.parseInt(arg.split("=")[1]); if (val > 0) wpm = val; }
            else if (arg.startsWith("-sm=") || arg.startsWith("--stop-multiplier=")) { sm = Double.parseDouble(arg.split("=")[1]); }
            else if (arg.startsWith("-pm=") || arg.startsWith("--pause-multiplier=")) { pm = Double.parseDouble(arg.split("=")[1]); }
            else if (arg.startsWith("-sd=") || arg.startsWith("--stop-delay=")) { sd = Integer.parseInt(arg.split("=")[1]); }
            else if (arg.startsWith("-pd=") || arg.startsWith("--pause-delay=")) { pd = Integer.parseInt(arg.split("=")[1]); }
        }
        List<Chunk> chunks;
        try {
            if (System.in.available() > 0) chunks = loadChunksFromStream(System.in);
            else chunks = loadChunksFromStream(App.class.getClassLoader().getResourceAsStream("mock_chunks.txt"));
        } catch (Exception e) { System.err.println("Error reading input: " + e.getMessage()); return; }
        if (chunks == null || chunks.isEmpty()) { System.err.println("No chunks found."); return; }
        
        ConfigService cs = new ConfigService(System.getProperty("user.home") + "/.config/chunk-rsvp/config.properties");
        RSVPEngine engine = new RSVPEngine(wpm != null ? wpm : 300, sm, pm, sd, pd, cs);
        try (java.io.FileInputStream tty = new java.io.FileInputStream("/dev/tty")) {
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

    private static List<Chunk> loadChunksFromStream(InputStream is) {
        if (is == null) return new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            return reader.lines().filter(line -> !line.isBlank()).map(Chunk::new).collect(Collectors.toList());
        } catch (Exception e) { return new ArrayList<>(); }
    }
}
