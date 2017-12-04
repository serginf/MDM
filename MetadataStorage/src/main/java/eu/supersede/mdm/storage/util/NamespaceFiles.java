package eu.supersede.mdm.storage.util;

import com.google.common.collect.Sets;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Created by snadal on 2/06/16.
 */
public class NamespaceFiles {

    private Set<String> namespaces;
    private Set<String> ignoredNamespaces;

    public NamespaceFiles() {
        this.namespaces = Sets.newHashSet();
        this.ignoredNamespaces = Sets.newHashSet();

        ClassLoader classLoader = getClass().getClassLoader();
        try (Stream<String> stream = Files.lines(new File(ConfigManager.getProperty("resources_path")+"namespaces.txt"/*classLoader.getResource("namespaces.txt").getFile()*/).toPath())) {
            stream.forEach(line -> namespaces.add(line));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (Stream<String> stream = Files.lines(new File(ConfigManager.getProperty("resources_path")+"ignoredNamespaces.txt"/*classLoader.getResource("ignoredNamespaces.txt").getFile()*/).toPath())) {
            stream.forEach(line -> ignoredNamespaces.add(line));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Set<String> getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(Set<String> namespaces) {
        this.namespaces = namespaces;
    }

    public Set<String> getIgnoredNamespaces() {
        return ignoredNamespaces;
    }

    public void setIgnoredNamespaces(Set<String> ignoredNamespaces) {
        this.ignoredNamespaces = ignoredNamespaces;
    }
}
