package me.nithanim.test.gui4ytdl.component;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Pane;
import me.nithanim.test.gui4ytdl.parameters.IgnoreErrorsParameter;
import me.nithanim.test.gui4ytdl.parameters.Parameter;
import me.nithanim.test.gui4ytdl.parameters.RateLimitParameter;

public class AdvancedOptionsComponent implements Initializable, InvocationParameterSource {
    public static AdvancedOptionsComponent create(Pane target) {
        AdvancedOptionsComponent c = new AdvancedOptionsComponent();
        FXMLLoader fl = new FXMLLoader();
        ClassLoader cl = AdvancedOptionsComponent.class.getClassLoader();
        fl.setClassLoader(cl);
        fl.setController(c);
        try {
            Parent p = fl.load(cl.getResourceAsStream("fxml/AdvancedOptions.fxml"));
            target.getChildren().add(p);
            return c;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @FXML
    private CheckBox chbSpeed;
    @FXML
    private ComboBox<String> cobSpeed;
    @FXML
    private CheckBox chbIgnoreErrors;
    
    @FXML
    private Pane playlistOptionsPane;
    private PlaylistOptionsComponent playlistOptions;

    private final SimpleObjectProperty<List<Parameter>> parameters = new SimpleObjectProperty<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        playlistOptions = PlaylistOptionsComponent.create(playlistOptionsPane);
        chbSpeed.setOnAction((ActionEvent t) -> {
            cobSpeed.setDisable(!chbSpeed.isSelected());
        });
        cobSpeed.setEditable(true);
        cobSpeed.setItems(FXCollections.observableList(Arrays.asList("16 KiB/s", "32 KiB/s", "64 KiB/s", "128 KiB/s", "256 KiB/s", "512 KiB/s", "768 KiB/s", "1024 KiB/s", "1.5 MiB/s", "2 MiB/s", "3 MiB/s", "5 MiB/s", "7 MiB/s", "10 MiB/s", "25 MiB/s")));
        cobSpeed.getSelectionModel().clearAndSelect(0);
        /*cobSpeed.getEditor().focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                //cobSpeed.setValue(cobSpeed.getEditor().getText());
                System.out.println("A: " + cobSpeed.getEditor().getText());
            }
        });*/
        cobSpeed.valueProperty().addListener((ObservableValue<? extends String> ov, String p, String n) -> {
            try {
                int v = Integer.parseInt(n);
                if (v >= 1024) {
                    cobSpeed.setValue((((int) (v / 1024f * 10f)) / 10f) + " MiB/s");
                } else {
                    cobSpeed.setValue(v + " KB/s");
                }
            } catch (NumberFormatException ex) {
                Matcher m = Pattern.compile("(\\d+) ((?:MiB|KiB)\\/s)").matcher(n);
                if (!m.find()) {
                    //reset for now
                    cobSpeed.setValue(p);
                }
            }
        });

        InvalidationListener inv = o -> {
            ArrayList<Parameter> l = new ArrayList<>();
            if (chbSpeed.isSelected()) {
                l.add(new RateLimitParameter(cobSpeed.getValue()));
            }
            l.addAll(playlistOptions.getParameters().getValue());
            if(chbIgnoreErrors.isSelected()) {
                l.add(new IgnoreErrorsParameter());
            }
            parameters.setValue(l);
        };

        chbSpeed.selectedProperty().addListener(inv);
        cobSpeed.valueProperty().addListener(inv);
        playlistOptions.getParameters().addListener(inv);
        
        inv.invalidated(null);
    }

    @Override
    public ReadOnlyObjectProperty<List<Parameter>> getParameters() {
        return parameters;
    }
}
