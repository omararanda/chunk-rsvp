package com.chunkrsvp.cli;

public class CliArguments {
    private final Integer wpm, sd, pd;
    private final Double sm, pm;
    private final boolean help, init, noControls;
    private final String filePath;

    public CliArguments(Integer wpm, Double sm, Double pm, Integer sd, Integer pd, boolean help, boolean init, String filePath) {
        this(wpm, sm, pm, sd, pd, help, init, filePath, false);
    }

    public CliArguments(Integer wpm, Double sm, Double pm, Integer sd, Integer pd, boolean help, boolean init, String filePath, boolean noControls) {
        this.wpm = wpm;
        this.sm = sm;
        this.pm = pm;
        this.sd = sd;
        this.pd = pd;
        this.help = help;
        this.init = init;
        this.filePath = filePath;
        this.noControls = noControls;
    }

    public String getFilePath() { return filePath; }

    public Integer getWpm() { return wpm; }
    public Double getSm() { return sm; }
    public Double getPm() { return pm; }
    public Integer getSd() { return sd; }
    public Integer getPd() { return pd; }
    public boolean isHelp() { return help; }
    public boolean isInit() { return init; }
    public boolean isNoControls() { return noControls; }
}
