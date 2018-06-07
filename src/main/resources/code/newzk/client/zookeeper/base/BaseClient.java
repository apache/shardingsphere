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

package io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.base;

import io.shardingsphere.jdbc.orchestration.reg.newzk.client.action.IClient;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.utility.Constants;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.utility.PathUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.data.ACL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/*
 * @author lidongbo
 */
public abstract class BaseClient implements IClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.base.BaseClient.class);
    
    private final int circleWait = 30;
    
    @Getter(value = AccessLevel.PROTECTED)
    private List<ACL> authorities;
    
    // false
    @Setter(value = AccessLevel.PROTECTED)
    private boolean rootExist;
    
    @Getter(value = AccessLevel.PROTECTED)
    private Holder holder;
    
    @Setter(value = AccessLevel.PROTECTED)
    @Getter(value = AccessLevel.PROTECTED)
    private String rootNode = "/InitValue";
    
    @Getter
    private BaseContext context;
    
    protected BaseClient(final BaseContext context) {
        this.context = context;
    }
    
    @Override
    public void start() throws IOException, InterruptedException {
        holder = new Holder(getContext());
        holder.start();
    }
    
    @Override
    public synchronized boolean blockUntilConnected(final int wait, final TimeUnit units) throws InterruptedException {
        long maxWait = units != null ? TimeUnit.MILLISECONDS.convert(wait, units) : 0;

        while (!holder.isConnected()) {
            long waitTime = maxWait - circleWait;
            if (waitTime <= 0) {
                return holder.isConnected();
            }
            wait(circleWait);
        }
        return true;
    }
    
    @Override
    public void close() {
        context.close();
        try {
            this.deleteNamespace();
            // CHECKSTYLE:OFF
        } catch (Exception e) {
            // CHECKSTYLE:ON
            LOGGER.error("zk client close delete root error:{}", e.getMessage(), e);
        }
        holder.close();
    }
    
    void registerWatch(final io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.section.Listener globalListener) {
        if (context.getGlobalListener() != null) {
            LOGGER.warn("global listener can only register one");
            return;
        }
        context.setGlobalListener(globalListener);
        LOGGER.debug("globalListenerRegistered:{}", globalListener.getKey());
    }
    
    @Override
    public void registerWatch(final String key, final io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.section.Listener listener) {
        String path = PathUtil.getRealPath(rootNode, key);
        listener.setPath(path);
        context.getWatchers().put(listener.getKey(), listener);
        LOGGER.debug("register watcher:{}", path);
    }
    
    @Override
    public void unregisterWatch(final String key) {
        if (io.shardingsphere.jdbc.orchestration.reg.newzk.client.utility.StringUtil.isNullOrBlank(key)) {
            throw new IllegalArgumentException("key should not be blank");
        }
//        String path = PathUtil.getRealPath(rootNode, key);
        if (context.getWatchers().containsKey(key)) {
            context.getWatchers().remove(key);
            LOGGER.debug("unregisterWatch:{}", key);
        }
    }
    
    protected void createNamespace() throws KeeperException, InterruptedException {
        createNamespace(Constants.NOTHING_DATA);
    }
    
    protected void createNamespace(final byte[] date) throws KeeperException, InterruptedException {
        if (rootExist) {
            LOGGER.debug("root exist");
            return;
        }
        try {
            if (null == holder.getZooKeeper().exists(rootNode, false)) {
                holder.getZooKeeper().create(rootNode, date, authorities, CreateMode.PERSISTENT);
            }
            rootExist = true;
            LOGGER.debug("creating root:{}", rootNode);
        } catch (KeeperException.NodeExistsException e) {
            LOGGER.warn("root create:{}", e.getMessage());
            rootExist = true;
            return;
        }
        holder.getZooKeeper().exists(rootNode, io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.section.WatcherCreator.deleteWatcher(new io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.section.Listener(rootNode) {
            @Override
            public void process(final WatchedEvent event) {
                rootExist = false;
            }
        }));
        LOGGER.debug("created root:{}", rootNode);
    }
    
    protected void deleteNamespace() throws KeeperException, InterruptedException {
        try {
            holder.getZooKeeper().delete(rootNode, Constants.VERSION);
        } catch (KeeperException.NodeExistsException | KeeperException.NotEmptyException e) {
            LOGGER.info("delete root :{}", e.getMessage());
        }
        rootExist = false;
        LOGGER.debug("delete root:{},rootExist:{}", rootNode, rootExist);
    }
    
    void setAuthorities(final String scheme, final byte[] auth, final List<ACL> authorities) {
        context.setScheme(scheme);
        context.setAuth(auth);
        this.authorities = authorities;
    }
}
