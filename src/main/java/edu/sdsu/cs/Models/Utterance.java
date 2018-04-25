package edu.sdsu.cs.Models;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.google.gson.annotations.Expose;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/**
 * @author Tom Paulus
 * Created on 4/25/18.
 */
@Data
public class Utterance {
    @Expose
    public String id;
    @Expose
    public String start;
    @Expose
    public String end;
    @Expose
    public String word;
    @Expose
    public String source;
    @Expose
    @Setter(AccessLevel.NONE)
    public Next next;

    public Utterance(String start, String end, String word, String source) {
        this.id = UUID.randomUUID().toString();
        this.start = start;
        this.end = end;
        this.word = word;
        this.source = source;
    }

    public void setNext(Utterance utterance) {
        this.next = new Next(utterance.word, utterance.id);
    }

    public Item asItem() {
        Item item = new Item();
        item.withString("id", id);
        item.withString("start", start);
        item.withString("end", end);
        item.withString("word", word);
        item.withString("source", source);

        Map<String, String> nextMap = new TreeMap<>();
        nextMap.put("word", this.next.word);
        nextMap.put("id", this.next.id);
        item.withMap("next", nextMap);

        return item;
    }

    @Data
    @AllArgsConstructor
    public class Next {
        @Expose
        public String word;
        @Expose
        public String id;
    }
}
