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

package io.shardingsphere.orchestration.reg.newzk.client.action;

import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.section.StrategyType;
import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.section.ZookeeperEventListener;
import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.transaction.BaseTransaction;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Client API.
 *
 * @author lidongbo
 */
public interface IClient extends IAction, IGroupAction {
    
    /**
     * Start.
     *
     * @throws IOException IO exception
     * @throws InterruptedException interrupted exception
     */
    void start() throws IOException, InterruptedException;
    
    /**
     * Start until out.
     *
     * @param waitingTime waiting time
     * @param timeUnit time unit
     * @return connected or not
     * @throws IOException IO exception
     * @throws InterruptedException interrupted exception
     */
    boolean start(int waitingTime, TimeUnit timeUnit) throws IOException, InterruptedException;
    
    /**
     * Block until connected.
     *
     * @param waitingTime waiting time
     * @param timeUnit time unit
     * @return connected or not
     * @throws InterruptedException interrupted exception
     */
    boolean blockUntilConnected(int waitingTime, TimeUnit timeUnit) throws InterruptedException;
    
    /**
     * Register watcher.
     *
     * @param key key
     * @param zookeeperEventListener zookeeper event listener
     */
    void registerWatch(String key, ZookeeperEventListener zookeeperEventListener);
    
    /**
     * Unregister watcher.
     *
     * @param key key
     */
    void unregisterWatch(String key);
    
    /**
     * Choice exec strategy.
     *
     * @param strategyType strategyType
     */
    void useExecStrategy(StrategyType strategyType);
    
    /**
     * Get execution strategy.
     *
     * @return execution strategy
     */
    IExecStrategy getExecStrategy();
    
    /**
     * Create zookeeper transaction.
     *
     * @return zookeeper transaction
     */
    BaseTransaction transaction();
    
    /**
     * Close.
     */
    void close();
}
