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

package io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.provider;

import io.shardingsphere.jdbc.orchestration.reg.newzk.client.action.IProvider;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.election.LeaderElection;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.utility.Constants;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.utility.PathUtil;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.base.Holder;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.transaction.BaseTransaction;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.ACL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Stack;

/*
 * @author lidongbo
 */
public class BaseProvider implements IProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseProvider.class);
    
    @Getter
    private final Holder holder;
    
    @Getter
    private final String rootNode;
    
    @Getter(value = AccessLevel.PROTECTED)
    private final boolean watched;
    
    @Getter(value = AccessLevel.PROTECTED)
    private final List<ACL> authorities;
    
    public BaseProvider(final String rootNode, final Holder holder, final boolean watched, final List<ACL> authorities) {
        this.rootNode = rootNode;
        this.holder = holder;
        this.watched = watched;
        this.authorities = authorities;
    }
    
    @Override
    public String getDataString(final String key) throws KeeperException, InterruptedException {
        return new String(getData(key));
    }
    
    @Override
    public byte[] getData(final String key) throws KeeperException, InterruptedException {
        return holder.getZooKeeper().getData(key, watched, null);
    }
    
    @Override
    public void getData(final String key, final AsyncCallback.DataCallback callback, final Object ctx) throws KeeperException, InterruptedException {
        holder.getZooKeeper().getData(key, watched, callback, ctx);
    }
    
    @Override
    public boolean exists(final String key) throws KeeperException, InterruptedException {
        return null != holder.getZooKeeper().exists(key, watched);
    }
    
    @Override
    public boolean exists(final String key, final Watcher watcher) throws KeeperException, InterruptedException {
        return null != holder.getZooKeeper().exists(key, watcher);
    }
    
    @Override
    public List<String> getChildren(final String key) throws KeeperException, InterruptedException {
        return holder.getZooKeeper().getChildren(key, watched);
    }
    
    @Override
    public void create(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        holder.getZooKeeper().create(key, value.getBytes(Constants.UTF_8), authorities, createMode);
        LOGGER.debug("BaseProvider createCurrentOnly:{}", key);
//        create(key, value, createMode, new AtomicInteger());
    }

    @Override
    public void update(final String key, final String value) throws KeeperException, InterruptedException {
        holder.getZooKeeper().setData(key, value.getBytes(Constants.UTF_8), Constants.VERSION);
    }
    
    @Override
    public void delete(final String key) throws KeeperException, InterruptedException {
        holder.getZooKeeper().delete(key, Constants.VERSION);
        LOGGER.debug("BaseProvider deleteOnlyCurrent:{}", key);
    }
    
    @Override
    public void delete(final String key, final AsyncCallback.VoidCallback callback, final Object ctx) throws KeeperException, InterruptedException {
        holder.getZooKeeper().delete(key, Constants.VERSION, callback, ctx);
        LOGGER.debug("BaseProvider deleteOnlyCurrent:{},ctx:{}", key, ctx);
    }

    @Override
    public String getRealPath(final String path) {
        return PathUtil.getRealPath(rootNode, path);
    }
    
    @Override
    public List<String> getNecessaryPaths(final String key) {
        List<String> nodes = PathUtil.getPathOrderNodes(rootNode, key);
        nodes.remove(rootNode);
        return nodes;
    }
    
    @Override
    public Stack<String> getDeletingPaths(final String key) {
        return PathUtil.getPathReverseNodes(rootNode, key);
    }
    
    @Override
    public void executeContention(final LeaderElection election) throws KeeperException, InterruptedException {
        this.executeContention(rootNode, election);
    }
    
    private void executeContention(final String nodeBeCompete, final LeaderElection election) throws KeeperException, InterruptedException {
        election.executeContention(nodeBeCompete, this);
    }
    
    @Override
    public void resetConnection() {
        try {
            holder.reset();
            // CHECKSTYLE:OFF
        } catch (Exception e) {
            // CHECKSTYLE:ON
            LOGGER.error("resetConnection Exception:{}", e.getMessage(), e);
        }
    }
    
    @Override
    public BaseTransaction transaction() {
        return new BaseTransaction();
    }
}
