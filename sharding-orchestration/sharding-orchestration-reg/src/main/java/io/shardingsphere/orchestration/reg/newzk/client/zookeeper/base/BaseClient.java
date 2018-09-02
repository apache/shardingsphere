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

package io.shardingsphere.orchestration.reg.newzk.client.zookeeper.base;

import com.google.common.base.Strings;
import io.shardingsphere.orchestration.reg.newzk.client.action.IClient;
import io.shardingsphere.orchestration.reg.newzk.client.utility.PathUtil;
import io.shardingsphere.orchestration.reg.newzk.client.utility.ZookeeperConstants;
import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.section.StrategyType;
import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.section.WatcherCreator;
import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.section.ZookeeperEventListener;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.data.ACL;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Base client.
 *
 * @author lidongbo
 */
@Slf4j
public abstract class BaseClient implements IClient {
    
    private static final int CIRCLE_WAIT = 30;
    
    @Getter(value = AccessLevel.PROTECTED)
    private List<ACL> authorities;
    
    // false
    @Setter(value = AccessLevel.PROTECTED)
    private boolean rootExist;
    
    @Getter(value = AccessLevel.PROTECTED)
    @Setter(value = AccessLevel.PROTECTED)
    private Holder holder;
    
    @Setter(value = AccessLevel.PROTECTED)
    @Getter(value = AccessLevel.PROTECTED)
    private String rootNode = ZookeeperConstants.ROOT_INIT_PATH;
    
    @Getter(value = AccessLevel.PROTECTED)
    private BaseContext context;
    
    protected BaseClient(final BaseContext context) {
        this.context = context;
    }
    
    @Override
    public void start() throws IOException, InterruptedException {
        prepareStart();
        holder.start();
    }
    
    @Override
    public synchronized boolean start(final int waitingTime, final TimeUnit timeUnit) throws InterruptedException, IOException {
        prepareStart();
        holder.start(waitingTime, timeUnit);
        return holder.isConnected();
    }
    
    private void prepareStart() {
        holder = new Holder(getContext());
        useExecStrategy(StrategyType.USUAL);
    }
    
    @Override
    public synchronized boolean blockUntilConnected(final int waitingTime, final TimeUnit timeUnit) throws InterruptedException {
        long maxWait = timeUnit != null ? TimeUnit.MILLISECONDS.convert(waitingTime, timeUnit) : 0;
        while (!holder.isConnected()) {
            long waitTime = maxWait - CIRCLE_WAIT;
            if (waitTime <= 0) {
                return holder.isConnected();
            }
            wait(CIRCLE_WAIT);
        }
        return true;
    }
    
    @Override
    public void close() {
        context.close();
        try {
            if (rootExist) {
                this.deleteNamespace();
            }
        } catch (final KeeperException | InterruptedException ex) {
            log.error("zk client close delete root error:{}", ex.getMessage(), ex);
        }
        holder.close();
    }
    
    void registerWatch(final ZookeeperEventListener globalZookeeperEventListener) {
        if (context.getGlobalZookeeperEventListener() != null) {
            log.warn("global listener can only register one");
            return;
        }
        context.setGlobalZookeeperEventListener(globalZookeeperEventListener);
    }
    
    @Override
    public final void registerWatch(final String key, final ZookeeperEventListener zookeeperEventListener) {
        final String path = PathUtil.getRealPath(rootNode, key);
        zookeeperEventListener.setPath(path);
        context.getWatchers().put(zookeeperEventListener.getKey(), zookeeperEventListener);
    }
    
    @Override
    public final void unregisterWatch(final String key) {
        if (Strings.isNullOrEmpty(key)) {
            throw new IllegalArgumentException("key should not be blank");
        }
        if (context.getWatchers().containsKey(key)) {
            context.getWatchers().remove(key);
        }
    }
    
    protected void createNamespace() throws KeeperException, InterruptedException {
        createNamespace(ZookeeperConstants.NOTHING_DATA);
    }
    
    private void createNamespace(final byte[] date) throws KeeperException, InterruptedException {
        if (rootExist) {
            return;
        }
        try {
            if (null == holder.getZooKeeper().exists(rootNode, false)) {
                holder.getZooKeeper().create(rootNode, date, authorities, CreateMode.PERSISTENT);
            }
            rootExist = true;
        } catch (final KeeperException.NodeExistsException ex) {
            rootExist = true;
            return;
        }
        holder.getZooKeeper().exists(rootNode, WatcherCreator.deleteWatcher(new ZookeeperEventListener(rootNode) {
            
            @Override
            public void process(final WatchedEvent event) {
                rootExist = false;
            }
        }));
    }
    
    protected final void deleteNamespace() throws KeeperException, InterruptedException {
        try {
            holder.getZooKeeper().delete(rootNode, ZookeeperConstants.VERSION);
        } catch (final KeeperException.NodeExistsException | KeeperException.NotEmptyException ex) {
            log.info("delete root :{}", ex.getMessage());
        }
        rootExist = false;
    }
    
    final void setAuthorities(final String scheme, final byte[] auth, final List<ACL> authorities) {
        context.setScheme(scheme);
        context.setAuth(auth);
        this.authorities = authorities;
    }
}
