package com.saaavsaaa.client.utility;

import com.saaavsaaa.client.utility.constant.Constants;

import javax.swing.tree.TreeNode;
import java.util.*;

/**
 * Created by aaa
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
    
    public static List<String> breadthToB(TreeNode root) {
        List<String> lists = new ArrayList<>();
        if(root==null)
            return lists;
        Queue<TreeNode> queue=new LinkedList<>();
        queue.offer(root);
        while(!queue.isEmpty()){
            /*TreeNode tree=queue.poll();
            if(tree.left!=null)
                queue.offer(tree.left);
            if(tree.right!=null)
                queue.offer(tree.right);
            lists.add(tree.val);*/
        }
        return lists;
    }
    
    public static List<String> depthToB(TreeNode root) {
        List<String> lists = new ArrayList<>();
        if(root==null)
            return lists;
        Stack<TreeNode> stack=new Stack<TreeNode>();
        stack.push(root);
        while(!stack.isEmpty()){
            TreeNode tree=stack.pop();
            //先往栈中压入右节点，再压左节点，这样出栈就是先左节点后右节点了。
            /*if(tree.right!=null)
                stack.push(tree.right);
            if(tree.left!=null)
                stack.push(tree.left);
            lists.add(tree.val);*/
        }
        return lists;
    }
    
    //isSequential
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
