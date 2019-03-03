package me.nithanim.test.gui4ytdl.parameters;

import java.util.ArrayList;
import java.util.List;

public class PlaylistStartParameter implements Parameter {
    private final List<String> params;

    public PlaylistStartParameter(int n) {
        params = new ArrayList<>();
        params.add("--playlist-start");
        params.add(String.valueOf(n));
    }

    @Override
    public List<String> toCmdParam() {
        return params;
    }
}
