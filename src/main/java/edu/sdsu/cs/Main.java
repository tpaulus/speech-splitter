package edu.sdsu.cs;

import com.google.gson.Gson;
import edu.sdsu.cs.Models.CaptionLine;
import edu.sdsu.cs.Models.Utterance;
import lombok.extern.log4j.Log4j;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author Tom Paulus
 * Created on 4/13/18.
 */
@Log4j
public class Main {
    /**
     * @param args The Source File (MP4), the Corresponding Caption File (JSON), and the Speaker Name (i.e. "Obama")
     */
    public static void main(String[] args) {
        String captionFilePath = null;
        String mediaFilePath = null;
        String speaker = null;

        for (String arg : args) {
            if (arg.endsWith(".json")) captionFilePath = arg;
            else if (arg.endsWith(".mp4")) mediaFilePath = arg;
            else speaker = arg.toLowerCase();
        }

        if (captionFilePath == null || mediaFilePath == null || speaker == null) {
            throw new RuntimeException("Not all inputs have been supplied");
        }

        log.info("Starting Video Processing");

        URL sourceFile = StorageService.getInstance().uploadFile(new File(mediaFilePath));
        log.info("Video Uploaded to S3");

        List<Utterance> utterances = new ArrayList<>();
        Utterance previousUtterance = null;

        try {
            CaptionLine[] lines = new Gson().fromJson(readFile(captionFilePath, Charset.defaultCharset()), CaptionLine[].class);
            for (CaptionLine line : lines) {
                try {
                    List<String[]> edges = LineProcessor.getInstance().processCaptionLine(mediaFilePath, line);
                    if (edges == null || edges.size() == 0) continue;
                    String[] words = line.getText().split("[\\s|\\n]");

                    for (int i = 0; i < Math.min(edges.size(), words.length); i++) {
                        String[] tsPair = edges.get(i);
                        Utterance u = new Utterance(tsPair[0], tsPair[1], words[i], sourceFile.toString());
                        if (previousUtterance != null) previousUtterance.setNext(u);

                        utterances.add(u);
                        previousUtterance = u;
                    }
                    log.info("Processed Line - " + line.getText());
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    log.warn("MATLAB connection interrupted", e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (Utterance utterance : utterances) {
            log.debug(utterance);
            LineProcessor.writeUtterance(speaker, utterance);
            log.info(String.format("Pushed Word \"%s\" to DDB", utterance.getWord()));
        }

        log.info(String.format("Finished Processing Video - Uploaded %d words", utterances.size()));
    }

    private static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
}
