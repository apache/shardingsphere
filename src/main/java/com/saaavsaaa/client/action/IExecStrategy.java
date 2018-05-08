package com.saaavsaaa.client.action;

import com.saaavsaaa.client.zookeeper.Provider;

/**
 * Created by aaa on 18-5-2.
 */
public interface IExecStrategy extends IClient {
    Provider getProvider();
}
