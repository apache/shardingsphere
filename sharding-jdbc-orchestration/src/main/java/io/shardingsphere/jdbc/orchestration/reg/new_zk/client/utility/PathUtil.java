/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.jdbc.orchestration.reg.new_zk.client.utility;

import java.util.*;

/*
 * path util
 *
 * @author lidongbo
 */
public class PathUtil {
    
    public static String getRealPath(final String root, final String path){
        return adjustPath(root, path);
    }
    
    private static String adjustPath(String root, String path){
        if (StringUtil.isNullOrBlank(path)){
            throw new IllegalArgumentException("path should have content!");
        }
        if (!root.startsWith(Constants.PATH_SEPARATOR)){
            root = Constants.PATH_SEPARATOR + root;
        }
        if (!path.startsWith(Constants.PATH_SEPARATOR)){
            path = Constants.PATH_SEPARATOR + path;
        }
        if (!path.startsWith(root)){
            return root + path;
        }
        return path;
    }
    
    //child to root
    public static Stack<String> getPathReverseNodes(final String root, String path){
        path = adjustPath(root, path);
        Stack<String> pathStack = new Stack<>();
        int index = 1;
        int position = path.indexOf(Constants.PATH_SEPARATOR, index);
        do{
            pathStack.push(path.substring(0, position));
            index = position + 1;
            position = path.indexOf(Constants.PATH_SEPARATOR, index);
        }
        while (position > -1);
        pathStack.push(path);
        return pathStack;
    }
    
    public static List<String> getPathOrderNodes(final String root, String path){
        path = adjustPath(root, path);
        List<String> paths = new ArrayList<>();
        int index = 1;
        int position = path.indexOf(Constants.PATH_SEPARATOR, index);
    
        do{
            paths.add(path.substring(0, position));
            index = position + 1;
            position = path.indexOf(Constants.PATH_SEPARATOR, index);
        }
        while (position > -1);
        paths.add(path);
        return paths;
    }
    
    public static List<String> getShortPathNodes(String path){
        path = checkPath(path);
        List<String> paths = new ArrayList<>();
        char[] chars = path.toCharArray();
        StringBuilder builder = new StringBuilder(Constants.PATH_SEPARATOR);
        for (int i = 1; i < chars.length; i++) {
            if (chars[i] == Constants.PATH_SEPARATOR.charAt(0)){
                paths.add(builder.toString());
                builder = new StringBuilder(Constants.PATH_SEPARATOR);
                continue;
            }
            builder.append(chars[i]);
            if (i == chars.length - 1){
                paths.add(builder.toString());
            }
        }
        return paths;
    }
    
    /*
    * ignore invalid char and // /./  /../
    */
    public static String checkPath(String path) throws IllegalArgumentException {
        if(path == null || path.length() == 0) {
            throw new IllegalArgumentException("path should not be null");
        }
        if(path.charAt(0) != 47 || path.charAt(path.length() - 1) == 47){
            path = Constants.PATH_SEPARATOR + path;
        }
    
        if(path.charAt(path.length() - 1) == 47){
            path = Constants.PATH_SEPARATOR + path;
        }

        char previous = 47;
        char[] chars = path.toCharArray();
        StringBuilder builder = new StringBuilder();
        builder.append(previous);
        
        for(int i = 1; i < chars.length; ++i) {
            char c = chars[i];
            if (c == 0 || (c == 47 && previous == 47)) {
                continue;
            }
            if (c == 46) {
                // ignore /./  /../
                boolean preWarn = previous == 47 || (previous == 46 && chars[i - 2] == 47);
                if (previous == 47 && (i + 1 == chars.length || chars[i + 1] == 47)) {
                    i++;
                    continue;
                }
                if ((previous == 46 && chars[i - 2] == 47) && (i + 1 == chars.length || chars[i + 1] == 47)) {
                    i+=2;
                    continue;
                }
            }
            if (c > 0 && c < 31 || c > 127 && c < 159 || c > '\ud800' && c < '\uf8ff' || c > '\ufff0' && c < '\uffff') {
                continue;
            }
    
            builder.append(c);
            previous = c;
        }
        return builder.toString();
    }
}
