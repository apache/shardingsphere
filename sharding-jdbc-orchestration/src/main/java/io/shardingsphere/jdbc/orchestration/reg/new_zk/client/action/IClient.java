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

package io.shardingsphere.jdbc.orchestration.reg.new_zk.client.action;

import io.shardingsphere.jdbc.orchestration.reg.new_zk.client.zookeeper.section.Listener;
import io.shardingsphere.jdbc.orchestration.reg.new_zk.client.zookeeper.section.StrategyType;
import io.shardingsphere.jdbc.orchestration.reg.new_zk.client.zookeeper.transaction.ZKTransaction;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/*
 * client api
 *
 * @author lidongbo
 */
public interface IClient extends IAction, IGroupAction {
    void start() throws IOException, InterruptedException;
    boolean blockUntilConnected(int wait, TimeUnit units) throws InterruptedException;
    void close();
    void registerWatch(String key, Listener listener);
    void unregisterWatch(String key);
    void useExecStrategy(StrategyType strategyType);
    
    ZKTransaction transaction();
    /*
    void createNamespace();
    void deleteNamespace();
    
    Watcher registerWatch(Listener listener);
    void setRootNode(String namespace);
    
    void setAuthorities(String scheme, byte[] auth);
    ZooKeeper getZooKeeper();
    */
}
