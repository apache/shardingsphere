package com.saaavsaaa.client.untils;

import java.util.List;
import java.util.Stack;

/**
 * Created by aaa on 18-4-18.
 */
public class PathUtil {
    public static final String PATH_SEPARATOR = "/";
    
    public static String getRealPath(final String root, String path){
        path = adjustPath(path);
        if (path.startsWith(root)){
            return path;
        }
        return new StringBuilder().append(root).append(path).toString();
    }
    
    private static String adjustPath(String path){
        if (StringUtil.isNullOrWhite(path)){
            throw new IllegalArgumentException("path should have content!");
        }
        if (!path.startsWith(PATH_SEPARATOR)){
            path = PATH_SEPARATOR + path;
        }
        return path;
    }
    
    public static Stack<String> getPathNodes(String path){
        path = adjustPath(path);
        Stack<String> pathStack = new Stack<>();
        int index = 1;
        int position = path.indexOf(PATH_SEPARATOR, index);
        do{
            pathStack.push(path.substring(0, position));
            index = position + 1;
            position = path.indexOf(PATH_SEPARATOR, index);
        }
        while (position > -1);
        pathStack.push(path);
        return pathStack;
    }
    
}
