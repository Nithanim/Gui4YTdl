package me.nithanim.gui4ytdl.component;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;
import me.nithanim.gui4ytdl.Download;

public class ProgressComponent implements Initializable {
    public static ProgressComponent create(Pane target) {
        ProgressComponent c = new ProgressComponent();
        FXMLLoader fl = new FXMLLoader();
        ClassLoader cl = c.getClass().getClassLoader();
        fl.setClassLoader(cl);
        fl.setController(c);
        try {
            Parent p = fl.load(cl.getResourceAsStream("fxml/Progress.fxml"));
            target.getChildren().add(p);
            return c;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    @FXML
    private Label lblDownloadProgress;
    @FXML
    private ProgressBar barDownloadProgress;
    @FXML
    private Label lblFile;
    @FXML
    private Label lblSize;
    @FXML
    private Label lblSpeed;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        barDownloadProgress.setProgress(0);
        lblDownloadProgress.setText("0%");
    }

    public void showDownload(Download download) {
        lblFile.textProperty().bind(download.getFileName());
        lblSize.textProperty().bind(download.getFilesize());
        lblSpeed.textProperty().bind(download.getSpeed());
        download.progressProperty().addListener((Observable observable) -> {
            double v = ((ReadOnlyDoubleProperty) observable).get();
            barDownloadProgress.setProgress(v);
            lblDownloadProgress.setText(String.format("%.1f%%", v * 100));
        });
    }
}
