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

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalTime;
import org.apache.commons.lang3.time.DurationFormatUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

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

    List<String[]> processCaptionLine(final String sourceFile, final CaptionLine line) throws ExecutionException, InterruptedException {
        double[] edges = LineProcessor.getInstance().getEng().feval("findEdges.m", sourceFile, line.getText().split(" ").length);
        List<String[]> edgePairs = new ArrayList<>();

        Duration clipStart = Duration.between(
                LocalTime.MIN,
                LocalTime.parse(line.getStart()));

        for (int i = 0; i < edges.length; i += 2) {
            Duration word_start = clipStart.plusMillis((long) edges[i]);
            Duration word_end = clipStart.plusMillis((long) edges[i+1]);

            edgePairs.add(new String[]{
                    DurationFormatUtils.formatDurationHMS(word_start.toMillis()),
                    DurationFormatUtils.formatDurationHMS(word_end.toMillis())
            });
        }

        return edgePairs;
    }

    static void writeUtterance(final String tableName, final Utterance utterance){
        Dynamo.getInstance().writeItem(tableName, utterance.asItem());
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
