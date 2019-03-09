package me.nithanim.gui4ytdl.component;

import java.util.List;
import javafx.beans.property.ReadOnlyObjectProperty;
import me.nithanim.gui4ytdl.parameters.Parameter;

public interface InvocationParameterSource {
    ReadOnlyObjectProperty<List<Parameter>> getParameters();
}
