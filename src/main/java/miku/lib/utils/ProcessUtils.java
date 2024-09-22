package miku.lib.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ProcessUtils {

    private static final boolean win = System.getProperty("os.name").startsWith("Windows");

    public synchronized static String runProcess(String cmd){
        System.out.println("Run process:\n" + cmd);
        Process p;
        try {
            if (win) {
                ProcessBuilder process = new ProcessBuilder("cmd /c " + cmd);
                process.redirectErrorStream(true);
                p = process.start();
            } else {
                p = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", cmd}, null, null);
            }
            InputStream is = p.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while(true) {
                try {
                    String tmp = reader.readLine();
                    System.out.println("[attach]:"+line);
                    if(tmp == null){
                        break;
                    }
                    line = tmp;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            p.destroy();
            return line;
        }
        catch (Throwable t){
            throw new RuntimeException(t);
        }

    }
}
