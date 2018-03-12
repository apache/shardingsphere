package io.shardingjdbc.dbtest.common;

import io.shardingjdbc.dbtest.exception.DbTestException;

import java.io.File;

public class PathUtils {

    private static final String BasePath =  PathUtils.class.getClassLoader().getResource("").getPath();

    /**
     * 获取资源路径
     * @param path
     * @return
     */
    public static String getPath(String path,String parent){
        if(path == null){
            throw new DbTestException("路径不能为空");
        }

       if(path.startsWith("classpath:")){
           path  = path.substring("classpath:".length());
           path = BasePath + path;
           return path;
        }
        if(parent != null){
            return parent+path;
        }
        return path;
    }

    /**
     * 获取资源路径
     * @param path
     * @return
     */
    public static String getPath(String path){
        return getPath(path,null);
    }



}
