package eu.supersede.mdm.storage.experiments.datalog;

import com.google.common.collect.Sets;
import com.google.common.io.Files;
import corecover.MiniCon;
import upc.AlgorithmExecutor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class DatalogExperimentsRunner {

    public static Set<String> runMiniCon(Set<DatalogQuery> queries) throws IOException {
        Set<DatalogQuery> query = queries.stream().filter(t->t.toString().contains("q")).collect(Collectors.toSet());
        Set<DatalogQuery> views = queries.stream().filter(t->t.toString().contains("w")).collect(Collectors.toSet());

        File queryFile = File.createTempFile(UUID.randomUUID().toString(),".query");
        File viewsFile = File.createTempFile(UUID.randomUUID().toString(),".views");

        query.forEach(q -> {
            try {
                Files.append(q.toString(),queryFile, Charset.defaultCharset());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        views.forEach(q -> {
            try {
                Files.append(q.toString(),viewsFile, Charset.defaultCharset());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        Set<String> rewritings = Sets.newHashSet();
        AlgorithmExecutor.minicon(queryFile.getAbsolutePath(),viewsFile.getAbsolutePath()).forEach(r -> {
            rewritings.add(r.toString());
        });
        return rewritings;
    }

    public static Set<String> runCoreCover(Set<DatalogQuery> queries) throws IOException {
        Set<DatalogQuery> query = queries.stream().filter(t->t.toString().contains("q")).collect(Collectors.toSet());
        Set<DatalogQuery> views = queries.stream().filter(t->t.toString().contains("w")).collect(Collectors.toSet());

        File queryFile = File.createTempFile(UUID.randomUUID().toString(),".query");
        File viewsFile = File.createTempFile(UUID.randomUUID().toString(),".views");

        query.forEach(q -> {
            try {
                Files.append(q.toString(),queryFile, Charset.defaultCharset());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        views.forEach(q -> {
            try {
                Files.append(q.toString(),viewsFile, Charset.defaultCharset());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        Set<String> rewritings = Sets.newHashSet();
        AlgorithmExecutor.corecover(queryFile.getAbsolutePath(),viewsFile.getAbsolutePath()).forEach(r -> {
            rewritings.add(r.toString());
        });
        return rewritings;
    }

}
