package com.saaavsaaa.client.action;

import com.saaavsaaa.client.zookeeper.Provider;

/**
 * Created by aaa
 */
public interface IExecStrategy extends IClient {
    Provider getProvider();
}
