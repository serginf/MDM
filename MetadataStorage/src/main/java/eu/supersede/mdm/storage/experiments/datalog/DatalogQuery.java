package eu.supersede.mdm.storage.experiments.datalog;

import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

public class DatalogQuery {
    HashMap<String, Set<String>> head;
    HashMap<String, Set<String>> body;

    public DatalogQuery() {
        this.head = Maps.newHashMap();
        this.body = Maps.newHashMap();
    }

    public HashMap<String, Set<String>> getHead() {
        return head;
    }

    public void setHead(HashMap<String, Set<String>> head) {
        this.head = head;
    }

    public HashMap<String, Set<String>> getBody() {
        return body;
    }

    public void setBody(HashMap<String, Set<String>> body) {
        this.body = body;
    }

    private static String toRelationName(String r) {
        return r.replace("Concept","C").toLowerCase().replace("_","");
    }

    private static String toAttributeName(String a) {
        return a.replace("Concept","C").replace("Feature","F").
                replace("_","").replace("id","ID");
    }

    @Override
    public String toString() {
        String headName = (String)head.keySet().toArray()[0];
        String query =
                headName+"("+head.get(headName).stream().map(a->toAttributeName(a)).sorted()
                        .collect(Collectors.joining(","))+")"
                        +" :- "+body.keySet().stream().map(c -> toRelationName(c)+
                        "("+body.get(c).stream().map(a->toAttributeName(a)).sorted().collect(Collectors.joining(","))+")").
                        collect(Collectors.joining(","));

        return query;
    }

}
