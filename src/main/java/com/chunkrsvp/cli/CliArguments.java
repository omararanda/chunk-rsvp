package com.chunkrsvp.cli;

public class CliArguments {
    private final Integer wpm, sd, pd;
    private final Double sm, pm;
    private final boolean help, init;

    public CliArguments(Integer wpm, Double sm, Double pm, Integer sd, Integer pd, boolean help, boolean init) {
        this.wpm = wpm;
        this.sm = sm;
        this.pm = pm;
        this.sd = sd;
        this.pd = pd;
        this.help = help;
        this.init = init;
    }

    public Integer getWpm() { return wpm; }
    public Double getSm() { return sm; }
    public Double getPm() { return pm; }
    public Integer getSd() { return sd; }
    public Integer getPd() { return pd; }
    public boolean isHelp() { return help; }
    public boolean isInit() { return init; }
}
