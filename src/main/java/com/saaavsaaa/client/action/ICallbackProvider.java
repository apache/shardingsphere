package com.saaavsaaa.client.action;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;

/**
 * Created by aaa
 */
public interface ICallbackProvider {
    void exists(String path, AsyncCallback.StatCallback cb, Object ctx);
    void getChildren(String path, AsyncCallback.ChildrenCallback cb, Object ctx);
    void createCurrentOnly(final String path, byte data[], CreateMode createMode, AsyncCallback.StringCallback cb, Object ctx);
    void update(final String path, byte data[], AsyncCallback.StatCallback cb, Object ctx);
}
