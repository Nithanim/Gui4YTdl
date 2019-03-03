package me.nithanim.test.gui4ytdl.component;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;

public class TargetFolderComponent implements Initializable {
    public static TargetFolderComponent create(Pane target) {
        TargetFolderComponent c = new TargetFolderComponent();
        FXMLLoader fl = new FXMLLoader();
        ClassLoader cl = AdvancedOptionsComponent.class.getClassLoader();
        fl.setClassLoader(cl);
        fl.setController(c);
        try {
            Parent p = fl.load(cl.getResourceAsStream("fxml/TargetFolder.fxml"));
            target.getChildren().add(p);
            return c;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @FXML
    private TextField tfDest;
    @FXML
    private Button btnDest;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnDest.setOnAction((ActionEvent event) -> {
            DirectoryChooser dc = new DirectoryChooser();
            dc.showDialog(tfDest.getScene().getWindow());
        });
    }

    public Path getDestinationFolder() {
        String t = tfDest.getText();
        if (t == null) {
            return null;
        } else {
            return Paths.get(t);
        }
    }
}
