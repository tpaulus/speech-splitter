package edu.sdsu.cs;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.google.gson.Gson;
import com.mathworks.engine.EngineException;
import com.mathworks.engine.MatlabEngine;
import edu.sdsu.cs.Models.CaptionLine;
import edu.sdsu.cs.Models.Utterance;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

/**
 * @author Tom Paulus
 * Created on 4/13/18.
 */
@Log4j
public class LineProcessor {
    private static LineProcessor ourInstance = new LineProcessor();

    @Getter(AccessLevel.PACKAGE)
    private MatlabEngine eng = null;

    private LineProcessor() {
        try {
            eng = MatlabEngine.startMatlab();
        } catch (EngineException e) {
            log.fatal("Could not start MATLAB Engine", e);
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            log.warn("MATLAB connection interrupted", e);
        }
    }

    static LineProcessor getInstance() {
        return ourInstance;
    }

    static void writeUtterance(final String tableName, final Utterance utterance) {
        Dynamo.getInstance().writeItem(tableName, utterance.asItem());
    }

    List<String[]> processCaptionLine(final String sourceFile, final CaptionLine line) throws ExecutionException, InterruptedException {
        Double clipStart = Double.parseDouble(line.getStart());
        Double duration = Double.parseDouble(line.getDur());

        // source_file, start (sec), dur (sec), num_words, demo
        double[] edges = new double[0];
        List<String[]> edgePairs = null;
        try {
            edges = LineProcessor.getInstance().getEng().feval("findEdges",
                    sourceFile,
                    clipStart,
                    duration,
                    line.getText().split("[\\s|\\n]").length,
                    false);
            edgePairs = new ArrayList<>();
        } catch (ClassCastException e) {
           log.warn(String.format("Problem finding roots for line -\"%s\"", line.getText()));
           return null;
        }

        for (int i = 0; i < edges.length; i += 2) {
            // Edges come in pairs
            Double word_start = clipStart + edges[i];
            Double word_end = clipStart + edges[i + 1];
            edgePairs.add(new String[]{
                    DurationFormatUtils.formatDurationHMS((long) (word_start * 1000)),
                    DurationFormatUtils.formatDurationHMS((long) (word_end * 1000))
            });
        }

        return edgePairs;
    }

    private static class Dynamo {
        @Getter
        private static Dynamo instance = new Dynamo();
        private DynamoDB dynamoDB;

        private Dynamo() {
            AmazonDynamoDB client = AmazonDynamoDBClientBuilder
                    .standard()
                    .withCredentials(Config.getCredentials())
                    .withRegion(Config.getRegion())
                    .build();

            dynamoDB = new DynamoDB(client);
        }

        void writeItem(final String tableName, final Item item) {
            Table table = dynamoDB.getTable(tableName);
            table.putItem(item);
        }

        <T> T getFromTable(final String tableName,
                           final String keyName,
                           final String keyValue,
                           final Class T) {
            Table table = dynamoDB.getTable(tableName);
            Item item = table.getItem(keyName, keyValue);
            return new Gson().fromJson(item.toJSON(), (Type) T);
        }
    }
}
