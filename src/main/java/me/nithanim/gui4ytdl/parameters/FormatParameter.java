package me.nithanim.gui4ytdl.parameters;

import java.util.ArrayList;
import java.util.List;

public class FormatParameter implements Parameter {
    private final List<String> params;

    public FormatParameter(String formatCode) {
        params = new ArrayList<>();
        params.add("-f");
        params.add(formatCode);
    }

    @Override
    public List<String> toCmdParam() {
        return params;
    }
}
