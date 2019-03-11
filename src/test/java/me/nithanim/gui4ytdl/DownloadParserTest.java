package me.nithanim.gui4ytdl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class DownloadParserTest {
    @Test
    public void basicTest() throws IOException {
        try (InputStream in = DownloadParserTest.class.getResourceAsStream("/test.cmd.txt")) {
            Scanner s = new Scanner(in);
            Consumer<String> filenameCallback = Mockito.mock(Consumer.class);
            Consumer<DownloadParser.DownloadProgressInfo> downloadProgressInfoCallback = Mockito.mock(Consumer.class);

            DownloadParser parser = new DownloadParser(filenameCallback, downloadProgressInfoCallback);
            while(s.hasNextLine()) {
                parser.parseNewLine(s.nextLine());
            }

            Mockito.verify(filenameCallback, Mockito.times(1)).accept("ThisIsAFileName.mp4");
            Mockito.verify(downloadProgressInfoCallback, Mockito.times(24)).accept(Mockito.any());
        }
    }
}
