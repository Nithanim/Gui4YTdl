package me.nithanim.gui4ytdl;

import com.sun.javafx.binding.ObjectConstant;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.stream.Collectors;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import lombok.SneakyThrows;
import me.nithanim.gui4ytdl.component.AdvancedOptionsComponent;
import me.nithanim.gui4ytdl.component.ProgressComponent;
import me.nithanim.gui4ytdl.parameters.FormatParameter;
import me.nithanim.gui4ytdl.parameters.UrlParameter;

public class MainController implements Initializable {
    @FXML
    private TextField tfLink;
    @FXML
    private Button btnLink;
    @FXML
    private TableView<Format> tableFormats;
    @FXML
    private ProgressIndicator tableFormatsProgress;
    @FXML
    private Button btnDownload;
    
    @FXML
    private TextField tfCommand;

    @FXML
    private Pane advOptionsPane;
    private AdvancedOptionsComponent advancedOptionsComponent;
    @FXML
    private Pane downloadProgress;
    private ProgressComponent progressComponent;

    private Download download;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        advancedOptionsComponent = AdvancedOptionsComponent.create(advOptionsPane);
        progressComponent = ProgressComponent.create(downloadProgress);

        ObservableList<TableColumn<Format, String>> cols = (ObservableList<TableColumn<Format, String>>) (Object) tableFormats.getColumns();
        addTableHeader(cols.get(0), Format::getCode);
        addTableHeader(cols.get(1), Format::getExtension);
        addTableHeader(cols.get(2), Format::getResolution);
        addTableHeaderB(cols.get(3), Format::isAudioOnly);
        addTableHeaderB(cols.get(4), Format::isVideoOnly);
        addTableHeader(cols.get(5), Format::getNote);


        /*tableFormats.getSelectionModel().setSelectionMode(
            SelectionMode.MULTIPLE
        );*/
        tableFormatsProgress.setVisible(false);
        btnLink.setOnAction((ActionEvent t) -> {
            downloadFormats();
        });
        btnDownload.setOnAction(new BtnDownloadEventHandler());
        
        InvalidationListener updater = o -> {
            List<String> b = getCommand();
            
            tfCommand.setText(b.stream().collect(Collectors.joining(" ")));
        };
        
        tableFormats.getSelectionModel().getSelectedItems().addListener(updater);
        tfLink.textProperty().addListener(updater);
        advancedOptionsComponent.getParameters().addListener(updater);
        updater.invalidated(null);
    }

    private void downloadFormats() {
        tableFormats.getItems().clear();
        tableFormats.setDisable(true);
        tableFormatsProgress.setVisible(true);
        tableFormatsProgress.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        btnLink.setDisable(true);

        Task<List<Format>> task = new Task<List<Format>>() {
            @Override
            protected List<Format> call() throws Exception {
                List<Format> formats = new YoutubeDlInvoker().getFormats(tfLink.getText());
                return formats;
            }
        };
        task.setOnSucceeded(wse -> {
            List<Format> formats = task.getValue();
            tableFormats.setItems(FXCollections.observableList(formats));
            for (int i = 0; i < formats.size(); i++) {
                if (formats.get(i).isBest()) {
                    tableFormats.getSelectionModel().clearAndSelect(i);
                    break;
                }
            }
            tableFormatsProgress.setVisible(false);
            tableFormats.setDisable(false);
            btnLink.setDisable(false);
        });
        task.setOnFailed(wse -> {
            tableFormatsProgress.setVisible(false);
            tableFormats.setDisable(false);
            btnLink.setDisable(false);
        });
        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }

    private void addTableHeaderB(TableColumn<Format, String> tc, Function<Format, Boolean> f) {
        addTableHeader(tc, f.andThen(String::valueOf));
    }

    private void addTableHeader(TableColumn<Format, String> tc, Function<Format, String> f) {
        tc.setCellValueFactory(p -> ObjectConstant.valueOf(f.apply(p.getValue())));
    }

    private void addTableHeader(String title, Function<Format, String> f) {
        TableColumn<Format, String> tc = new TableColumn(title);
        tc.setCellValueFactory(p -> ObjectConstant.valueOf(f.apply(p.getValue())));
        tableFormats.getColumns().add(tc);
    }
    
    private List<String> getCommand() {
        CmdStringBuilder b = new CmdStringBuilder();
        Format format = tableFormats.getSelectionModel().getSelectedItem();
        if(format != null) {
            b.add(new FormatParameter(format.getCode()));
        }
        b.addAll(advancedOptionsComponent.getParameters().getValue());
        b.add(new UrlParameter(tfLink.getText()));
        return b.build();
    }

    private class BtnDownloadEventHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent t) {
            if (download == null) {
                //download = new Interactor().download(tfLink.getText(), tableFormats.getSelectionModel().getSelectedItem());
                download = new YoutubeDlInvoker().download(getCommand());
                btnDownload.setText("Stop download");
                progressComponent.showDownload(download);

                download.setOnFailed(new EventHandler<WorkerStateEvent>() {
                    @Override
                    @SneakyThrows
                    public void handle(WorkerStateEvent event) {
                        download.get();
                        btnDownload.setText("Download");
                        btnDownload.setDisable(false);
                    }
                });

                download.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    @Override
                    @SneakyThrows
                    public void handle(WorkerStateEvent event) {
                        download.get();
                        btnDownload.setText("Download");
                        btnDownload.setDisable(false);
                    }
                });

                download.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                    @Override
                    @SneakyThrows
                    public void handle(WorkerStateEvent event) {
                        btnDownload.setText("Download");
                        btnDownload.setDisable(false);
                    }
                });
            } else {
                btnDownload.setText("Stopping download...");
                btnDownload.setDisable(true);
                download.cancel(true);
                download = null;
            }
        }
    }
}
