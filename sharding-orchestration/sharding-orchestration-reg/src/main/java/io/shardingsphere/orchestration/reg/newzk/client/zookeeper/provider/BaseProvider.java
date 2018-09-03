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

package io.shardingsphere.orchestration.reg.newzk.client.zookeeper.provider;

import io.shardingsphere.orchestration.reg.newzk.client.action.IProvider;
import io.shardingsphere.orchestration.reg.newzk.client.election.LeaderElection;
import io.shardingsphere.orchestration.reg.newzk.client.utility.PathUtil;
import io.shardingsphere.orchestration.reg.newzk.client.utility.ZookeeperConstants;
import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.base.Holder;
import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.transaction.BaseTransaction;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.ACL;

import java.io.IOException;
import java.util.List;
import java.util.Stack;

/**
 * Base provider.
 *
 * @author lidongbo
 */
@RequiredArgsConstructor
@Slf4j
public class BaseProvider implements IProvider {
    
    @Getter
    private final String rootNode;
    
    @Getter
    private final Holder holder;
    
    @Getter(value = AccessLevel.PROTECTED)
    private final boolean watched;
    
    @Getter(value = AccessLevel.PROTECTED)
    private final List<ACL> authorities;
    
    @Override
    public final String getDataString(final String key) throws KeeperException, InterruptedException {
        return new String(getData(key));
    }
    
    @Override
    public final byte[] getData(final String key) throws KeeperException, InterruptedException {
        return holder.getZooKeeper().getData(key, watched, null);
    }
    
    @Override
    public final void getData(final String key, final AsyncCallback.DataCallback callback, final Object ctx) {
        holder.getZooKeeper().getData(key, watched, callback, ctx);
    }
    
    @Override
    public final boolean exists(final String key) throws KeeperException, InterruptedException {
        return null != holder.getZooKeeper().exists(key, watched);
    }
    
    @Override
    public final boolean exists(final String key, final Watcher watcher) throws KeeperException, InterruptedException {
        return null != holder.getZooKeeper().exists(key, watcher);
    }
    
    @Override
    public final List<String> getChildren(final String key) throws KeeperException, InterruptedException {
        return holder.getZooKeeper().getChildren(key, watched);
    }
    
    @Override
    public final void create(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        if (exists(key)) {
            return;
        }
        holder.getZooKeeper().create(key, value.getBytes(ZookeeperConstants.UTF_8), authorities, createMode);
    }

    @Override
    public final boolean update(final String key, final String value) throws KeeperException, InterruptedException {
        if (exists(key)) {
            holder.getZooKeeper().setData(key, value.getBytes(ZookeeperConstants.UTF_8), ZookeeperConstants.VERSION);
            return true;
        }
        return false;
    }
    
    @Override
    public final void delete(final String key) throws KeeperException, InterruptedException {
        holder.getZooKeeper().delete(key, ZookeeperConstants.VERSION);
    }
    
    @Override
    public final void delete(final String key, final AsyncCallback.VoidCallback callback, final Object ctx) {
        holder.getZooKeeper().delete(key, ZookeeperConstants.VERSION, callback, ctx);
    }

    @Override
    public final String getRealPath(final String path) {
        return PathUtil.getRealPath(rootNode, path);
    }
    
    @Override
    public final List<String> getNecessaryPaths(final String key) {
        List<String> nodes = PathUtil.getPathOrderNodes(rootNode, key);
        nodes.remove(rootNode);
        return nodes;
    }
    
    @Override
    public final Stack<String> getDeletingPaths(final String key) {
        return PathUtil.getPathReverseNodes(rootNode, key);
    }
    
    @Override
    public final void executeContention(final LeaderElection election) throws KeeperException, InterruptedException {
        this.executeContention(rootNode, election);
    }
    
    private void executeContention(final String nodeBeCompete, final LeaderElection election) throws KeeperException, InterruptedException {
        election.executeContention(nodeBeCompete, this);
    }
    
    @Override
    public final void resetConnection() {
        try {
            holder.reset();
        } catch (final InterruptedException | IOException ex) {
            log.error("resetConnection Exception: {}", ex.getMessage(), ex);
        }
    }
    
    @Override
    public BaseTransaction transaction() {
        return new BaseTransaction();
    }
}
