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

package io.shardingsphere.jdbc.orchestration.reg.newzk.client.action;

import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.section.StrategyType;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/*
 * client api
 *
 * @author lidongbo
 */
public interface IClient extends io.shardingsphere.jdbc.orchestration.reg.newzk.client.action.IAction, IGroupAction {
    
    /**
     * start.
     *
     * @throws IOException IO Exception
     * @throws InterruptedException InterruptedException
     */
    void start() throws IOException, InterruptedException;
    
    /**
     * block until connected.
     *
     * @param wait wait
     * @param units units
     * @return connected
     * @throws InterruptedException InterruptedException
     */
    boolean blockUntilConnected(int wait, TimeUnit units) throws InterruptedException;
    
    /**
     * close.
     */
    void close();
    
    /**
     * register watcher.
     *
     * @param key key
     * @param listener listener
     */
    void registerWatch(String key, io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.section.Listener listener);
    
    /**
     * unregister watcher.
     *
     * @param key key
     */
    void unregisterWatch(String key);
    
    /**
     * choice exec strategy.
     *
     * @param strategyType strategyType
     */
    void useExecStrategy(StrategyType strategyType);
    
    /**
     * create transaction.
     *
     * @return ZKTransaction
     */
    io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.transaction.ZKTransaction transaction();
    /*
    void createNamespace();
    void deleteNamespace();
    
    Watcher registerWatch(Listener listener);
    void setRootNode(String namespace);
    
    void setAuthorities(String scheme, byte[] auth);
    ZooKeeper getZooKeeper();
    */
}
