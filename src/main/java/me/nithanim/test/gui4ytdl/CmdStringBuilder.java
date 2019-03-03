package me.nithanim.test.gui4ytdl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import me.nithanim.test.gui4ytdl.parameters.Parameter;

public class CmdStringBuilder {
    private final List<Parameter> params = new ArrayList<>();

    public void addAll(Collection<Parameter> ps) {
        params.addAll(ps);
    }

    public void add(Parameter p) {
        params.add(p);
    }

    public List<String> build() {
        List<String> l = new ArrayList<>();
        l.add(YoutubeDlInvoker.BASE_COMMAND);
        params.stream().map(Parameter::toCmdParam).collect(Collectors.toList()).forEach(l::addAll);
        return l;
    }
}
