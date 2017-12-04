package eu.supersede.mdm.storage.util;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by snadal on 24/11/16.
 */
public class TempFiles {

    public static String storeInTempFile(String content) {
        String tempFileName = UUID.randomUUID().toString();
        String filePath = "";
        try {
            File tempFile = File.createTempFile(tempFileName,".tmp");
            filePath = tempFile.getAbsolutePath();
            Files.write(content.getBytes(),tempFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filePath;
    }

    public static String getTempFile() {
        String tempFileName = UUID.randomUUID().toString();
        String filePath = "";
        try {
            File tempFile = File.createTempFile(tempFileName,".tmp");
            filePath = tempFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filePath;
    }

}
