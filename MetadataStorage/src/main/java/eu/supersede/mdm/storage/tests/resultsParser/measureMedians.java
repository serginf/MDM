package eu.supersede.mdm.storage.tests.resultsParser;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

public class measureMedians {

    public static void main(String[] args) throws Exception {
        System.out.println("UPPER_BOUND_FEATURES_IN_G;MAX_EDGES_IN_QUERY;MAX_WRAPPERS;COVERED_FEATURES_QUERY;COVERED_FEATURES_WRAPPER;SIZE_OF_INTERMEDIATE_RESULTS;SIZE_OF_CQs;PROCESSING_TIME");

        String path = "/home/snadal/UPC/Projects/MDM/MetadataStorage/experiments_local/allexperiments.csv";

        Map<String, List<String>> allData = Maps.newHashMap();
        Files.readAllLines(new File(path).toPath()).forEach(line -> {
            String[] split = line.split(";");

            String dimensions = split[0]+";"+split[1]+";"+split[2]+";"+split[3]+";"+split[4];
            String values = split[5]+";"+split[6]+";"+split[7];
            allData.putIfAbsent(dimensions, Lists.newArrayList());

            List<String> oldList = allData.get(dimensions);
            oldList.add(values);

            allData.put(dimensions,oldList);
        });

        allData.forEach((dimension,values) -> {
            values.sort((s, t1) -> {
                int a = Integer.parseInt(s.split(";")[2]);
                int b = Integer.parseInt(t1.split(";")[2]);
                return Integer.compare(a,b);
            });

            System.out.print(dimension+";");
            if (values.size() == 1 || values.size() == 2) System.out.println(values.get(0));
            else System.out.println(values.get(1));
        });

    }

}
