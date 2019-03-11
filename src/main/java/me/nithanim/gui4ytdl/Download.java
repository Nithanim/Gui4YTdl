package me.nithanim.gui4ytdl;

import java.io.InputStreamReader;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Download extends Task<Void> {
    private static final Pattern DOWNLOAD_SPLIT_PATTERN = Pattern.compile("\r|\n");

    private static final Logger log = LoggerFactory.getLogger(Download.class);
    private static final Logger proclog = LoggerFactory.getLogger(Download.class.getName() + ":process");

    @Getter
    private final StringProperty fileName = new SimpleStringProperty();

    private final List<String> command;

    public Download(List<String> command) {
        this.command = command;
    }

    @Override
    protected Void call() throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
        Process p = pb.start();
        Scanner scanner = new Scanner(new InputStreamReader(p.getInputStream(), "UTF-8"));
        scanner.useDelimiter(DOWNLOAD_SPLIT_PATTERN);

        DownloadParser parser = new DownloadParser(
            s -> Platform.runLater(() -> fileName.setValue(s)),
            i -> updateInfo(i)
        );

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            proclog.trace(line);
            parser.parseNewLine(line);

            if (Thread.interrupted()) {
                p.destroy();
                p.waitFor();
                return null;
            }
        }
        updateProgress(100, 100);
        p.waitFor();
        return null;
    }

    @Getter
    private final ObjectProperty<DownloadParser.DownloadProgressInfo> downloadProgressInfo = new SimpleObjectProperty<>(this, "downloadProgressInfo", null);

    private final AtomicReference<DownloadParser.DownloadProgressInfo> downloadProgressInfoUpdate = new AtomicReference<>();

    private void updateInfo(DownloadParser.DownloadProgressInfo dpi) {
        if (Platform.isFxApplicationThread()) {
            downloadProgressInfo.setValue(dpi);
        } else {
            if (downloadProgressInfoUpdate.getAndSet(dpi) == null) {
                Platform.runLater(() -> {
                    DownloadParser.DownloadProgressInfo v = downloadProgressInfoUpdate.getAndSet(null);
                    updateProgress(v.getPercent(), 100);
                    downloadProgressInfo.setValue(dpi);
                });
            }
        }
    }
}
