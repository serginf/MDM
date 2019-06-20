package eu.supersede.mdm.storage.util;

import com.google.common.io.Files;
//import javafx.util.Pair; Use of this library gives compilation error because javafx is no longer a part of Java JDK
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import org.apache.commons.io.filefilter.RegexFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Created by snadal on 24/11/16.
 */
public class TempFiles {
    private static final File DIR = new File(ConfigManager.getProperty("output_path"));

    private static final String SUFFIX = ".ttl";

    public static String storeInTempFile(String content) {
        String tempFileName = UUID.randomUUID().toString();
        String filePath = "";
        try {
            File tempFile = File.createTempFile(tempFileName, ".tmp");
            filePath = tempFile.getAbsolutePath();
            Files.write(content.getBytes(), tempFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filePath;
    }

    public static String getTempFile() {
        String tempFileName = UUID.randomUUID().toString();
        String filePath = "";
        try {
            File tempFile = File.createTempFile(tempFileName, ".tmp");
            filePath = tempFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filePath;
    }

    /**
     * Get the latest file by comparing the file number as numbers (not alphabetical)
     *
     * @param baseFileNamePrefix
     * @return
     */
    public static Optional<Pair<Long, File>> getLatestFile(String baseFileNamePrefix) {
        FileFilter fileFilter = new RegexFileFilter(baseFileNamePrefix + "[0-9]*" + SUFFIX);
        File[] files = DIR.listFiles(fileFilter);

        Stream<Pair<Long, File>> fileNumbers = Arrays.stream(files).map(file ->
                new MutablePair<Long, File>(Long.parseLong(file.getName().substring(baseFileNamePrefix.length(), file.getName().indexOf(SUFFIX))), file));

        return fileNumbers.max((o1, o2) -> (int) (o1.getKey() - o2.getKey()));
    }

    /**
     * Get the next file in the sequence by getting the latest file and incrementing the number.
     *
     * @param baseFileNamePrefix
     * @return
     */
    public static String getIncrementalTempFile(String baseFileNamePrefix) {

        Pair<Long, File> longStringPair = getLatestFile(baseFileNamePrefix).orElse(new MutablePair<>(0l, null));

        File file = new File(DIR, baseFileNamePrefix + (longStringPair.getKey() + 1) + SUFFIX);
        return file.getAbsolutePath();
    }

}
