package edu.sdsu.cs;

import com.google.gson.Gson;
import edu.sdsu.cs.Models.CaptionLine;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author Tom Paulus
 * Created on 4/13/18.
 */
public class Main {
    /**
     *
     * @param args The Source File (MP4), the Corresponding Caption File (JSON), and the Speaker Name (i.e. "Obama")
     */
    public static void main(String[] args) {
        String captionFilePath = null;
        String mediaFilePath = null;
        String speaker = null;

        for (String arg : args) {
            if(arg.endsWith(".json")) captionFilePath = arg;
            else if(arg.endsWith(".mp4")) mediaFilePath = arg;
            else speaker = arg.toLowerCase();
        }

        if (captionFilePath == null || mediaFilePath == null || speaker == null) {
            throw new RuntimeException("Not all inputs have been supplied");
        }

        // TODO Upload File to S3

        try {
            CaptionLine[] lines = new Gson().fromJson(readFile(captionFilePath, Charset.defaultCharset()), CaptionLine[].class);
            for (CaptionLine line : lines) {
                LineProcessor.getInstance().processCaptionLine(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
}
