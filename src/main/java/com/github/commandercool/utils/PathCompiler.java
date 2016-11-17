package com.github.commandercool.utils;

/**
 * Created by Alex on 17.11.2016.
 */
public class PathCompiler {

    private static final String FILE_SEPARATOR = System.getProperty("file.separator");

    public static String getAbsolutePath(String filePath, String projectPath) {
        return projectPath + FILE_SEPARATOR + filePath;
    }

}
