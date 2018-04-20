package com.saaavsaaa.client.untils;

import javax.swing.tree.TreeNode;
import java.util.*;

/**
 * Created by aaa on 18-4-18.
 */
public class PathUtil {
    public static final String PATH_SEPARATOR = "/";
    
    public static String getRealPath(final String root, String path){
        return adjustPath(root, path);
    }
    
    private static String adjustPath(final String root, String path){
        if (StringUtil.isNullOrWhite(path)){
            throw new IllegalArgumentException("path should have content!");
        }
        if (!path.startsWith(PATH_SEPARATOR)){
            path = PATH_SEPARATOR + path;
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
        List<String> paths = new ArrayList<>();
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
}
