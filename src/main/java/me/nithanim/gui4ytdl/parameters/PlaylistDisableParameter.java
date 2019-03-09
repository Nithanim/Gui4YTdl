package me.nithanim.gui4ytdl.parameters;

import java.util.ArrayList;
import java.util.List;

public class PlaylistDisableParameter implements Parameter {
    private final List<String> params;

    public PlaylistDisableParameter() {
        params = new ArrayList<>();
        params.add("--no-playlist");
    }

    @Override
    public List<String> toCmdParam() {
        return params;
    }
}
