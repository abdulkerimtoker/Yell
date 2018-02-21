package yell.client.util;

import java.io.File;

/**
 * Created by Abdulkerim on 31.05.2016.
 */
public class FileExtension {

    public static String getExtension(File file) {
        String name = file.getName();
        String extension = "";

        for (int i = name.length() - 1; i > 0; i--) {
            char c = name.charAt(i);
            if (c == '.') {
                return extension;
            }
            extension = c + extension;
        }

        return "";
    }
}