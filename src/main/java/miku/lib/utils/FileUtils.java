package miku.lib.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtils {
    public static void createDirs(Path path){
        if(Files.isDirectory(path)){
            return;
        }
        try {
            Files.createDirectories(path);
        } catch (IOException ignored) {
        }
    }

    public static void createFile(Path path){
        if(path.getParent() != null){
            if (Files.exists(path.getParent()) && !Files.isDirectory(path.getParent())) {
                return;
            }
            createDirs(path.getParent());
        }
        if(Files.exists(path)){
            if(!Files.isDirectory(path)){
                return;
            } else {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        try {
            Files.createFile(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void write(Path path,byte[] data){
        createFile(path);
        try {
            Files.write(path,data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
