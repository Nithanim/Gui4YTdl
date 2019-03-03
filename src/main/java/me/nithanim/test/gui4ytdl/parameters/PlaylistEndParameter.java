package me.nithanim.test.gui4ytdl.parameters;

import java.util.ArrayList;
import java.util.List;

public class PlaylistEndParameter implements Parameter {
    private final List<String> params;

    public PlaylistEndParameter(int n) {
        params = new ArrayList<>();
        params.add("--playlist-end");
        params.add(String.valueOf(n));
    }

    @Override
    public List<String> toCmdParam() {
        return params;
    }
}
