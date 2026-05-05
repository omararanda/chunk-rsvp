package com.chunkrsvp.cli;

public class CliParser {
    public static CliArguments parse(String[] args) {
        Integer wpm = null;
        Double sm = null, pm = null;
        Integer sd = null, pd = null;
        boolean help = false, init = false;

        for (String arg : args) {
            if (arg.equals("-h") || arg.equals("--help")) { help = true; }
            else if (arg.equals("--init")) { init = true; }
            else if (arg.startsWith("-wpm=") || arg.startsWith("--words-per-minute=")) { int val = Integer.parseInt(arg.split("=")[1]); if (val > 0) wpm = val; }
            else if (arg.startsWith("-sm=") || arg.startsWith("--stop-multiplier=")) { sm = Double.parseDouble(arg.split("=")[1]); }
            else if (arg.startsWith("-pm=") || arg.startsWith("--pause-multiplier=")) { pm = Double.parseDouble(arg.split("=")[1]); }
            else if (arg.startsWith("-sd=") || arg.startsWith("--stop-delay=")) { sd = Integer.parseInt(arg.split("=")[1]); }
            else if (arg.startsWith("-pd=") || arg.startsWith("--pause-delay=")) { pd = Integer.parseInt(arg.split("=")[1]); }
        }
        return new CliArguments(wpm, sm, pm, sd, pd, help, init);
    }
}
