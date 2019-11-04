package eu.supersede.mdm.storage.tests.resultsParser;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

public class measureMedians {

    public static void main(String[] args) throws Exception {
        System.out.println("|F|;|E_Q|;|W|;|E_W|;Frac_Q;Frac_W;SIZE_OF_INTERMEDIATE_RESULTS;|UCQ|;R (ms)");

        String path = "/home/snadal/Desktop/20191001.txt";

        Map<String, List<String>> allData = Maps.newHashMap();
        Files.readAllLines(new File(path).toPath()).forEach(line -> {
            String[] split = line.split(";");

            String dimensions = split[0]+";"+split[1]+";"+split[2]+";"+split[3]+";"+split[4]+";"+split[5];
            String values = split[6]+";"+split[7]+";"+split[8];
            allData.putIfAbsent(dimensions, Lists.newArrayList());

            List<String> oldList = allData.get(dimensions);
            oldList.add(values);

            allData.put(dimensions,oldList);
        });

        allData.forEach((dimension,values) -> {
            int X = 0, Y = 0, Z = 0;
            int N = 0;
            for(String v : values) {
                X += Integer.parseInt(v.split(";")[0]);
                Y += Integer.parseInt(v.split(";")[1]);
                Z += Integer.parseInt(v.split(";")[2]);
                N++;
            };
            System.out.print(dimension+";");
            System.out.println((double)(X)/(double)(N)+";"+(double)(Y)/(double)(N)+";"+(double)(Z)/(double)(N));
            /*
            values.sort((s, t1) -> {
                int a = Integer.parseInt(s.split(";")[2]);
                int b = Integer.parseInt(t1.split(";")[2]);
                return Integer.compare(a,b);
            });

            System.out.print(dimension+";");
            if (values.size() == 1 || values.size() == 2) System.out.println(values.get(0));
            else System.out.println(values.get(1));*/
        });

    }

}
