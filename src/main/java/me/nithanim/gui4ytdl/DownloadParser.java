package me.nithanim.gui4ytdl;

import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DownloadParser {
    private static final Pattern LINE_PATTERN = Pattern.compile("\\[([^\\]]+)\\]\\s(.*)");
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
    /**
     * Pattern that matches the last download progress which differs in the fact
     * that it has no speed and the ETA is the download time. The patter is
     * specifically crafted to have matching group ids.
     * <pre>
     * (100)% of ([\d.]+)(\w+) (y)?(y)?in ([\d:]+)
     * </pre>
     */
    private static final Pattern DOWNLOAD_LAST_PATTERN = Pattern.compile("(100)% of ([\\d.]+)(\\w+) (y)?(y)?in ([\\d:]+)");

    /**
     * Pattern for ETA.
     * <pre>
     * (?:(\d+):)?(\d+):(\d+)
     * </pre>
     */
    private static final Pattern ETA_PATTERN = Pattern.compile("(?:(\\d+):)?(\\d+):(\\d+)");

    private static final Logger log = LoggerFactory.getLogger(DownloadParser.class);

    private final Consumer<String> filenameCallback;
    private final Consumer<DownloadProgressInfo> downloadProgressInfoCallback;

    public DownloadParser(
        Consumer<String> filenameCallback,
        Consumer<DownloadProgressInfo> downloadProgressInfoCallback
    ) {
        this.filenameCallback = filenameCallback;
        this.downloadProgressInfoCallback = downloadProgressInfoCallback;
    }

    public void parseNewLine(String line) {
        Matcher m = LINE_PATTERN.matcher(line);
        if (m.matches()) {
            String category = m.group(1);
            String content = m.group(2);
            handle(category, content);
        }
    }

    private void handle(String category, String content) {
        switch (category) {
            case "download":
                handleDownload(content);
                break;
            default:
                log.warn("Unkown category");
        }
    }

    private void handleDownload(String content) {
        Matcher m = DOWNLOAD_PROGRESS_PATTERN.matcher(content);
        if (m.matches()) {
            DownloadProgressInfo dpi = parseDownloadProgress(m);
            downloadProgressInfoCallback.accept(dpi);
        } else {
            if (content.startsWith("Destination:")) {
                filenameCallback.accept(content.substring(13));
            } else {
                Matcher m2 = DOWNLOAD_LAST_PATTERN.matcher(content);
                if (m2.matches()) {
                    DownloadProgressInfo dpi = parseDownloadProgress(m2);
                    downloadProgressInfoCallback.accept(dpi);
                }
            }
        }
    }

    private DownloadProgressInfo parseDownloadProgress(Matcher m) throws NumberFormatException {
        double percent = Double.parseDouble(m.group(1));
        double size = Double.parseDouble(m.group(2));
        String sizeModifier = m.group(3);
        final String g4 = m.group(4);
        double speed = g4 == null ? Double.NaN : Double.parseDouble(g4);
        String speedModifier = m.group(5);
        String eta = m.group(6);

        int sizeInBytes = parseDownloadSize(size, sizeModifier);
        long speedInBytesPerSecond = parseDownloadSpeed(speed, speedModifier);
        long etaInSeconds = parseEta(eta);

        return new DownloadProgressInfo(
            percent,
            sizeInBytes,
            speedInBytesPerSecond,
            etaInSeconds
        );
    }

    private long parseDownloadSpeed(double speed, String speedModifier) {
        long speedInBytesPerSecond;
        if (Double.isNaN(speed)) {
            speedInBytesPerSecond = -1;
        } else if (speedModifier.equals("KiB/s")) {
            speedInBytesPerSecond = (int) (speed * 1024);
        } else if (speedModifier.equals("MiB/s")) {
            speedInBytesPerSecond = (int) (speed * 1024 * 1024);
        } else if (speedModifier.equals("GiB/s")) {
            speedInBytesPerSecond = (int) (speed * 1024 * 1024 * 1024);
        } else {
            speedInBytesPerSecond = -2;
        }
        return speedInBytesPerSecond;
    }

    private int parseDownloadSize(double size, String sizeModifier) {
        int sizeInBytes;
        if (sizeModifier.equals("KiB")) {
            sizeInBytes = (int) (size * 1024);
        } else if (sizeModifier.equals("MiB")) {
            sizeInBytes = (int) (size * 1024 * 1024);
        } else if (sizeModifier.equals("GiB")) {
            sizeInBytes = (int) (size * 1024 * 1024 * 1024);
        } else {
            sizeInBytes = -2;
        }
        return sizeInBytes;
    }

    /**
     * Parses the ETA string to remaining seconds.
     *
     * @param eta
     * @return the seconds remaining, -1 if unknown or -2 if unable to estimate
     * (because download is taking too long)
     */
    private long parseEta(String eta) {
        if (eta.equals("--:--:--")) {
            return -2;
        } else if (eta.equals("Unknown ETA")) {
            return -1;
        } else {
            Matcher m = ETA_PATTERN.matcher(eta);
            if (m.matches()) {
                int seconds = Integer.parseInt(m.group(3));
                int minutes = Integer.parseInt(m.group(2));
                int hours = m.group(1) == null ? 0 : Integer.parseInt(m.group(1));

                return hours * 60 * 60 + minutes * 60 + seconds;
            } else {
                log.error("Unable to parse ETA \"{}\"", eta);
                return -1;
            }
        }
    }

    @Value
    public static class DownloadProgressInfo {
        /**
         * Percent as value from 0 to 100.
         */
        double percent;
        /**
         * Complete file size given in bytes.
         */
        long size;
        /**
         * Current download speed given in bytes per second.
         */
        long speed;
        /**
         * Estimated remaining time given in seconds. Equals -1 if unknown or -2
         * if too long to estimate.
         */
        long eta;
    }
}
