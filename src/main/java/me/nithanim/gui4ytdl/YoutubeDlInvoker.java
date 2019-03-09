package me.nithanim.gui4ytdl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.SneakyThrows;

public class YoutubeDlInvoker {
    static final String BASE_COMMAND;
    static final String[] REQUIRED_ARGS = {"--ignore-config", "--no-color"};

    static {
        if (Files.exists(Paths.get("./youtube-dl"))) {
            BASE_COMMAND = Paths.get("./youtube-dl").toString();
        } else {
            BASE_COMMAND = "youtube-dl";
        }
    }

    /**
     * ^([\w-._]+)\s+([\w]+)\s+(audio only|[\w]+)\s+(.+)$ 1: code 2: ext 3: res
     * 4: note
     */
    private static final Pattern FORMAT_PATTERN = Pattern.compile("^([\\w-._]+)\\s+([\\w]+)\\s+(audio only|[\\w]+)\\s+(.+)$");

    @SneakyThrows
    public String getVersion() {
        ProcessBuilder pb = new ProcessBuilder(BASE_COMMAND, "--version");
        pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
        Process p = pb.start();
        String v = readString(p.getInputStream()).trim();
        p.waitFor();
        return v;
    }

    @SneakyThrows
    public ArrayList<Format> getFormats(String url) {
        ProcessBuilder pb = new ProcessBuilder(BASE_COMMAND, "--list-formats", url);
        pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
        Process p = pb.start();
        Scanner s = new Scanner(p.getInputStream());

        ArrayList<Format> formats = new ArrayList<>();
        while (s.hasNextLine()) {
            String line = s.nextLine();
            Matcher m = FORMAT_PATTERN.matcher(line);
            if (m.find()) {
                String code = m.group(1);
                String extension = m.group(2);
                String resolution = m.group(3);
                String note = m.group(4);

                if (code.equals("format") && extension.equals("code")) {
                    //filter table header
                    continue;
                }

                boolean audioOnly = resolution.equals("audio only");
                boolean videoOnly = note.contains("video only");
                boolean best = note.endsWith("(best)");

                formats.add(new Format(code, extension, resolution, note, audioOnly, videoOnly, best));
            }
        }
        p.waitFor();
        return formats;
    }

    @SneakyThrows
    public String getExtractors() {
        ProcessBuilder pb = new ProcessBuilder(BASE_COMMAND, "--list-extractors", "--extractor-descriptions");
        pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
        Process p = pb.start();
        String s = readString(p.getInputStream()).trim();
        p.waitFor();
        return s;
    }

    @SneakyThrows
    public Download download(List<String> command) {
        Download download = new Download(command);
        Thread t = new Thread(download);
        t.setDaemon(true);
        t.start();
        return download;
    }

    private static String readString(InputStream in) throws IOException {
        return new String(readAllBytes(in));
    }

    private static byte[] readAllBytes(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[5 * 1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            baos.write(buffer, 0, read);
        }
        return baos.toByteArray();
    }
}
