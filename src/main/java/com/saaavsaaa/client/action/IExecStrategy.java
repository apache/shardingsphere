package com.saaavsaaa.client.action;

import com.saaavsaaa.client.zookeeper.base.BaseProvider;

/**
 * Created by aaa
 */
public interface IExecStrategy extends IAction, IGroupAction {
    BaseProvider getProvider();
}
