package me.nithanim.gui4ytdl.parameters;

import java.util.ArrayList;
import java.util.List;

public class UsernameParameter implements Parameter {
    private final List<String> params;

    public UsernameParameter(String username) {
        params = new ArrayList<>();
        params.add("--username");
        params.add(username);
    }

    @Override
    public List<String> toCmdParam() {
        return params;
    }
}
