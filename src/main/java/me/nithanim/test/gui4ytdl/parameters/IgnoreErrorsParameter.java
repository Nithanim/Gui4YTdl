package me.nithanim.test.gui4ytdl.parameters;

import java.util.ArrayList;
import java.util.List;

public class IgnoreErrorsParameter implements Parameter {
    private final List<String> params;

    public IgnoreErrorsParameter() {
        params = new ArrayList<>();
        params.add("-i");
    }

    @Override
    public List<String> toCmdParam() {
        return params;
    }
}
