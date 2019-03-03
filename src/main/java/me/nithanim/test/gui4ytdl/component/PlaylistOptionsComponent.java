package me.nithanim.test.gui4ytdl.component;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.layout.Pane;
import me.nithanim.test.gui4ytdl.parameters.Parameter;
import me.nithanim.test.gui4ytdl.parameters.PlaylistDisableParameter;
import me.nithanim.test.gui4ytdl.parameters.PlaylistEndParameter;
import me.nithanim.test.gui4ytdl.parameters.PlaylistStartParameter;

public class PlaylistOptionsComponent implements Initializable, InvocationParameterSource {
    public static PlaylistOptionsComponent create(Pane target) {
        PlaylistOptionsComponent c = new PlaylistOptionsComponent();
        FXMLLoader fl = new FXMLLoader();
        ClassLoader cl = PlaylistOptionsComponent.class.getClassLoader();
        fl.setClassLoader(cl);
        fl.setController(c);
        try {
            Parent p = fl.load(cl.getResourceAsStream("fxml/PlaylistOptions.fxml"));
            target.getChildren().add(p);
            return c;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @FXML
    private CheckBox chbPlaylistUse;
    @FXML
    private CheckBox chbPlaylistStart;
    @FXML
    private CheckBox chbPlaylistEnd;
    @FXML
    private TextField tfPlaylistStart;
    @FXML
    private TextField tfPlaylistEnd;

    private final SimpleObjectProperty<List<Parameter>> parameters = new SimpleObjectProperty<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        chbPlaylistUse.selectedProperty().addListener(this::updateDisableStatus);
        chbPlaylistStart.selectedProperty().addListener(this::updateDisableStatus);
        chbPlaylistEnd.selectedProperty().addListener(this::updateDisableStatus);

        chbPlaylistUse.selectedProperty().addListener(this::updateParameters);
        chbPlaylistStart.selectedProperty().addListener(this::updateParameters);
        chbPlaylistEnd.selectedProperty().addListener(this::updateParameters);
        tfPlaylistStart.textProperty().addListener(this::updateParameters);
        tfPlaylistEnd.textProperty().addListener(this::updateParameters);

        UnaryOperator<Change> uo = c -> {
            if (!c.getControlNewText().matches("\\d*")) {
                return null;
            } else {
                return c;
            }
        };
        tfPlaylistStart.setTextFormatter(new TextFormatter<>(uo));
        tfPlaylistEnd.setTextFormatter(new TextFormatter<>(uo));

        updateDisableStatus();
        updateParameters();
    }

    private void updateParameters(Observable o) {
        updateParameters();
    }

    private void updateParameters() {
        List<Parameter> l = new ArrayList<>();
        if (chbPlaylistUse.isSelected()) {
            if (chbPlaylistStart.isSelected()) {
                l.add(new PlaylistStartParameter(parseTfInt(tfPlaylistStart)));
            }
            if (chbPlaylistEnd.isSelected()) {
                l.add(new PlaylistEndParameter(parseTfInt(tfPlaylistEnd)));
            }
        } else {
            l.add(new PlaylistDisableParameter());
        }
        parameters.setValue(l);

    }

    private int parseTfInt(TextField tf) {
        try {
            return Integer.parseInt(tf.getText());
        } catch (NumberFormatException ex) {
            return 1;
        }
    }

    private void updateDisableStatus(Observable o) {
        updateDisableStatus();
    }

    private void updateDisableStatus() {
        chbPlaylistStart.setDisable(!chbPlaylistUse.isSelected());
        chbPlaylistEnd.setDisable(!chbPlaylistUse.isSelected());

        tfPlaylistStart.setDisable(!chbPlaylistStart.isSelected());
        tfPlaylistEnd.setDisable(!chbPlaylistEnd.isSelected());
    }

    @Override
    public ReadOnlyObjectProperty<List<Parameter>> getParameters() {
        return parameters;
    }
}
