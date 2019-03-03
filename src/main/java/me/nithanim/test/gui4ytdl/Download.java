package me.nithanim.test.gui4ytdl;

import java.io.InputStreamReader;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import lombok.Getter;
import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Download extends Task<Void> {
    private static final Pattern DOWNLOAD_SPLIT_PATTERN = Pattern.compile("\r|\n");
    /**
     * <pre>
     * ([\d.]+)% of ([\d.]+)(\w+) at\s+([\d.]+)?(Unknown speed|[\w\/]+)? ETA (Unknown ETA|[\d:]+)
     * group 1: percent of downloaded size (double)
     * group 2: download size (double)
     * group 3: download size modifier e.g. "MiB"
     * group 4: download speed: null (if unknown) or double
     * group 5: download speed modifier e.g. "MiB/s" or "Unknown speed"
     * group 6: estimated remaining time: "Unknown ETA" or "(\d\d:)?\d\d:\d\d" or "--:--:--"
     * </pre>
     */
    private static final Pattern DOWNLOAD_PROGRESS_PATTERN = Pattern.compile("\\s*([\\d.]+)% of ([\\d.]+)(\\w+) at\\s+([\\d.]+)?(Unknown speed|[\\w\\/]+)? ETA (Unknown ETA|[\\d:]+)");

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

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            proclog.trace(line);
            if (line.startsWith("[download]")) {
                String payload = line.substring(11);
                if (payload.startsWith("Destination:")) {
                    Platform.runLater(() -> fileName.setValue(payload.substring(13)));
                } else {
                    Matcher m = DOWNLOAD_PROGRESS_PATTERN.matcher(payload);
                    if (m.matches()) {
                        double percent = Double.parseDouble(m.group(1));
                        double size = Double.parseDouble(m.group(2));
                        String sizeModifier = m.group(3);
                        final String g4 = m.group(4);
                        double speed = g4 == null ? Double.NaN : Double.parseDouble(g4);
                        String speedModifier = m.group(5);
                        String eta = m.group(5);

                        updateProgress(percent, 100);

                        /*int sizeInBytes;
                        if (sizeModifier.equals("KiB")) {
                            sizeInBytes = (int) (size * 1024);
                        } else if (sizeModifier.equals("MiB")) {
                            sizeInBytes = (int) (size * 1024 * 1024);
                        } else if (sizeModifier.equals("GiB")) {
                            sizeInBytes = (int) (size * 1024 * 1024 * 1024);
                        } else {
                            sizeInBytes = -2;
                        }

                        long speedInBytesPerSecond;
                        if (Double.isNaN(speed)) {
                            speedInBytesPerSecond = -1;
                        } else if (speedModifier.equals("KiB/s")) {
                            speedInBytesPerSecond = (int) (size * 1024);
                        } else if (speedModifier.equals("MiB/s")) {
                            speedInBytesPerSecond = (int) (size * 1024 * 1024);
                        } else if (speedModifier.equals("GiB/s")) {
                            speedInBytesPerSecond = (int) (size * 1024 * 1024 * 1024);
                        } else {
                            speedInBytesPerSecond = -2;
                        }*/
                        updateInfo(new Info(
                            size + " " + sizeModifier,
                            speed + " " + speedModifier,
                            eta
                        ));
                    }
                }
            }

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

    @Value
    private static class Info {
        String size;
        String speed;
        String eta;
    }

    @Getter
    private final StringProperty filesize = new SimpleStringProperty(this, "filesize", null);
    @Getter
    private final StringProperty speed = new SimpleStringProperty(this, "speed", null);
    @Getter
    private final StringProperty eta = new SimpleStringProperty(this, "eta", null);

    private final AtomicReference<Info> infoUpdate = new AtomicReference<>();

    private void updateInfo(Info v) {
        if (Platform.isFxApplicationThread()) {
            _updateInfo(v);
        } else {
            if (infoUpdate.getAndSet(v) == null) {
                Platform.runLater(() -> {
                    _updateInfo(infoUpdate.getAndSet(null));
                });
            }
        }
    }

    private void _updateInfo(Info info) {
        filesize.setValue(info.getSize());
        speed.setValue(info.getSpeed());
        eta.setValue(info.getEta());
    }

    /*@Getter
    private final LongProperty filesize = new SimpleLongProperty(this, "filename", -1);
    private final AtomicLong filesizeUpdate = new AtomicLong(-1);

    protected void updateFilesize(long v) {
        if (Platform.isFxApplicationThread()) {
            this.filesizeUpdate.set(v);
        } else {
            if (filesizeUpdate.getAndSet(v) == -1) {
                Platform.runLater(() -> {
                    filesize.setValue(filesizeUpdate.getAndSet(-1));
                });
            }
        }
    }*/
}
