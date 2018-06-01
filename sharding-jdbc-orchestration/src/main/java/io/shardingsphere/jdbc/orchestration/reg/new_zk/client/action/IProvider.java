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

import io.shardingsphere.jdbc.orchestration.reg.new_zk.client.election.LeaderElection;
import io.shardingsphere.jdbc.orchestration.reg.new_zk.client.zookeeper.transaction.ZKTransaction;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;

import java.util.List;
import java.util.Stack;

/*
 * provider api
 *
 * @author lidongbo
 */
public interface IProvider {
    String getDataString(final String key) throws KeeperException, InterruptedException;
    byte[] getData(final String key) throws KeeperException, InterruptedException;
    void getData(final String key, final AsyncCallback.DataCallback callback, final Object ctx) throws KeeperException, InterruptedException;
    boolean exists(final String key) throws KeeperException, InterruptedException;
    boolean exists(final String key, final Watcher watcher) throws KeeperException, InterruptedException;
    List<String> getChildren(final String key) throws KeeperException, InterruptedException;
    void create(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException;
    void update(final String key, final String value) throws KeeperException, InterruptedException;
    void delete(final String key) throws KeeperException, InterruptedException;
    void delete(final String key, final AsyncCallback.VoidCallback callback, final Object ctx) throws KeeperException, InterruptedException;
    
    String getRealPath(final String path);
    List<String> getNecessaryPaths(final String key);
    Stack<String> getDeletingPaths(final String key);
    void executeContention(final LeaderElection election) throws KeeperException, InterruptedException;
    
    void createInTransaction(final String key, final String value, final CreateMode createMode, final ZKTransaction transaction) throws KeeperException, InterruptedException;
    
    void resetConnection();
}
