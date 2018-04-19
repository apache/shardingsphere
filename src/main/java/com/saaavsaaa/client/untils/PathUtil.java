package com.saaavsaaa.client.untils;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * Created by aaa on 18-4-18.
 */
public class PathUtil {
    public static final String PATH_SEPARATOR = "/";
    
    public static String getRealPath(final String root, String path){
        path = adjustPath(root, path);
        return new StringBuilder().append(root).append(path).toString();
    }
    
    private static String adjustPath(final String root, String path){
        if (StringUtil.isNullOrWhite(path)){
            throw new IllegalArgumentException("path should have content!");
        }
        if (path.startsWith(root)){
            return path;
        }
        if (!path.startsWith(PATH_SEPARATOR)){
            path = PATH_SEPARATOR + path;
        }
        return path;
    }
    
    //child to root
    public static Stack<String> getPathReverseNodes(final String root, String path){
        path = adjustPath(root, path);
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
    
    public static List<String> getPathOrderNodes(final String root, String path){
        path = adjustPath(root, path);
        List<String> paths = new LinkedList<>();
        int index = 1;
        int position = path.indexOf('/', index);
    
        do{
            paths.add(path.substring(0, position));
            index = position + 1;
            position = path.indexOf('/', index);
        }
        while (position > -1);
        paths.add(path);
        return paths;
    }
}
