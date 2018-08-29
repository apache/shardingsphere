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

import io.shardingsphere.orchestration.reg.newzk.client.election.LeaderElection;
import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.transaction.BaseTransaction;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;

import java.util.List;
import java.util.Stack;

/**
 * Provider API.
 *
 * @author lidongbo
 */
public interface IProvider {
    
    /**
     * Get string type data.
     *
     * @param key key
     * @return data String
     * @throws KeeperException zookeeper exception
     * @throws InterruptedException interrupted exception
     */
    String getDataString(String key) throws KeeperException, InterruptedException;
    
    /**
     * Get string type data.
     *
     * @param key key
     * @return data
     * @throws KeeperException zookeeper exception
     * @throws InterruptedException interrupted exception
     */
    byte[] getData(String key) throws KeeperException, InterruptedException;
    
    /**
     * Get string type data.
     *
     * @param key key
     * @param callback callback
     * @param ctx ctx
     * @throws KeeperException zookeeper exception
     * @throws InterruptedException interrupted exception
     */
    void getData(String key, AsyncCallback.DataCallback callback, Object ctx) throws KeeperException, InterruptedException;
    
    /**
     * Check exist.
     *
     * @param key key
     * @return exist
     * @throws KeeperException zookeeper exception
     * @throws InterruptedException interrupted exception
     */
    boolean exists(String key) throws KeeperException, InterruptedException;
    
    /**
     * Check exist.
     *
     * @param key key
     * @param watcher watcher
     * @return exist
     * @throws KeeperException zookeeper exception
     * @throws InterruptedException interrupted exception
     */
    boolean exists(String key, Watcher watcher) throws KeeperException, InterruptedException;
    
    /**
     * Get children's keys.
     *
     * @param key key
     * @return exist
     * @throws KeeperException zookeeper exception
     * @throws InterruptedException interrupted exception
     */
    List<String> getChildren(String key) throws KeeperException, InterruptedException;
    
    /**
     * Only create target node.
     *
     * @param key key
     * @param value value
     * @param createMode create mode
     * @throws KeeperException zookeeper exception
     * @throws InterruptedException interrupted exception
     */
    void create(String key, String value, CreateMode createMode) throws KeeperException, InterruptedException;
    
    /**
     * Update.
     *
     * @param key key
     * @param value value
     * @return is success
     * @throws KeeperException zookeeper exception
     * @throws InterruptedException interrupted exception
     */
    boolean update(String key, String value) throws KeeperException, InterruptedException;
    
    /**
     * Only delete target node..
     *
     * @param key key
     * @throws KeeperException zookeeper exception
     * @throws InterruptedException interrupted exception
     */
    void delete(String key) throws KeeperException, InterruptedException;
    
    /**
     * Only delete target node.
     *
     * @param key key
     * @param callback callback
     * @param ctx ctx
     * @throws KeeperException zookeeper exception
     * @throws InterruptedException interrupted exception
     */
    void delete(String key, AsyncCallback.VoidCallback callback, Object ctx) throws KeeperException, InterruptedException;
    
    /**
     * Get real path with root.
     *
     * @param path path
     * @return real path
     */
    String getRealPath(String path);
    
    /**
     * Get path nodes that needed create.
     *
     * @param key key
     * @return all path nodes
     */
    List<String> getNecessaryPaths(String key);
    
    /**
     * Get path nodes that needed delete.
     *
     * @param key key
     * @return all path nodes
     */
    Stack<String> getDeletingPaths(String key);
    
    /**
     * Contention exec.
     *
     * @param election election
     * @throws KeeperException zookeeper exception
     * @throws InterruptedException InterruptedException
     */
    void executeContention(LeaderElection election) throws KeeperException, InterruptedException;
    
    /**
     * Reset connection.
     */
    void resetConnection();
    
    /**
     * Create zookeeper transaction.
     *
     * @return zookeeper transaction
     */
    BaseTransaction transaction();
}
