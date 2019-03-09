package me.nithanim.gui4ytdl.parameters;

import java.util.ArrayList;
import java.util.List;

public class UrlParameter implements Parameter {
    private final List<String> params;

    public UrlParameter(String url) {
        params = new ArrayList<>();
        params.add(url);
    }

    @Override
    public List<String> toCmdParam() {
        return params;
    }
}
