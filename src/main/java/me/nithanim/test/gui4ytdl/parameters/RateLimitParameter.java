package me.nithanim.test.gui4ytdl.parameters;

import java.util.ArrayList;
import java.util.List;

public class RateLimitParameter implements Parameter {
    private final List<String> params;

    public RateLimitParameter(String str) {
        List<String> l = new ArrayList<>();
        l.add("-r");
        l.add(convert(str));
        params = l;
    }

    public RateLimitParameter(int speed, String modifier) {
        List<String> l = new ArrayList<>();l.add("-r");
        l.add(convert(speed, modifier));
        params = l;
    }

    private String convert(String str) {
        String[] split = str.split(" ", 2);
        return convert(Double.parseDouble(split[0]), split[1]);
    }

    private String convert(double speed, String modifier) {
        String mod;
        if (modifier.equals("KiB/s")) {
            mod = "K";
        } else if (modifier.equals("MiB/s")) {
            mod = "M";
        } else if (modifier.equals("GiB/s")) {
            mod = "G";
        } else {
            mod = "";
        }
        return speed + mod;
    }

    @Override
    public List<String> toCmdParam() {
        return params;
    }
}
