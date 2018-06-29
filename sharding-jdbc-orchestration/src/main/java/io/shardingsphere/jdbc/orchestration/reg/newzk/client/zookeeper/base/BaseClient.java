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
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.action.IExecStrategy;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.action.IProvider;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.utility.Constants;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.utility.PathUtil;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.utility.StringUtil;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.provider.TransactionProvider;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.section.ClientContext;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.section.Listener;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.section.StrategyType;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.section.WatcherCreator;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.strategy.AsyncRetryStrategy;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.strategy.ContentionStrategy;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.strategy.SyncRetryStrategy;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.strategy.UsualStrategy;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/*
 * @author lidongbo
 */
public abstract class BaseClient implements IClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseClient.class);
    
    private final Map<StrategyType, IExecStrategy> strategies = new ConcurrentHashMap<>();
    
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
    private String rootNode = "/InitValue";
    
    @Getter(value = AccessLevel.PROTECTED)
    private BaseContext context;
    
    @Getter
    private IExecStrategy strategy;
    
    protected BaseClient(final BaseContext context) {
        this.context = context;
    }
    
    @Override
    public void start() throws IOException, InterruptedException {
        prepareStart();
        holder.start();
    }
    
    @Override
    public synchronized boolean start(final int wait, final TimeUnit units) throws InterruptedException, IOException {
        prepareStart();
        holder.start(wait, units);
        return holder.isConnected();
    }
    
    private void prepareStart() {
        holder = new Holder(getContext());
        useExecStrategy(StrategyType.USUAL);
    }
    
    @Override
    public synchronized void useExecStrategy(final StrategyType strategyType) {
        LOGGER.debug("useExecStrategy:{}", strategyType);
        if (strategies.containsKey(strategyType)) {
            strategy = strategies.get(strategyType);
            return;
        }
        
        IProvider provider = new TransactionProvider(getRootNode(), getHolder(), Constants.WATCHED, getAuthorities());
        switch (strategyType) {
            case USUAL:
                strategy = new UsualStrategy(provider);
                break;
            case CONTEND:
                strategy = new ContentionStrategy(provider);
                break;
            case SYNC_RETRY:
                strategy = new SyncRetryStrategy(provider, ((ClientContext) getContext()).getDelayRetryPolicy());
                break;
            case ASYNC_RETRY:
                strategy = new AsyncRetryStrategy(provider, ((ClientContext) getContext()).getDelayRetryPolicy());
                break;
            default:
                strategy = new UsualStrategy(provider);
                break;
        }
        
        strategies.put(strategyType, strategy);
    }
    
    @Override
    public void close() {
        this.strategies.clear();
        context.close();
        try {
            if (rootExist) {
                this.deleteNamespace();
            }
            // CHECKSTYLE:OFF
        } catch (Exception e) {
            // CHECKSTYLE:ON
            LOGGER.error("zk client close delete root error:{}", e.getMessage(), e);
        }
        holder.close();
    }
    
    void registerWatch(final Listener globalListener) {
        if (context.getGlobalListener() != null) {
            LOGGER.warn("global listener can only register one");
            return;
        }
        context.setGlobalListener(globalListener);
        LOGGER.debug("globalListenerRegistered:{}", globalListener.getKey());
    }
    
    @Override
    public void registerWatch(final String key, final Listener listener) {
        String path = PathUtil.getRealPath(rootNode, key);
        listener.setPath(path);
        context.getWatchers().put(listener.getKey(), listener);
        LOGGER.debug("register watcher:{}", path);
    }
    
    @Override
    public void unregisterWatch(final String key) {
        if (StringUtil.isNullOrBlank(key)) {
            throw new IllegalArgumentException("key should not be blank");
        }
        if (context.getWatchers().containsKey(key)) {
            context.getWatchers().remove(key);
            LOGGER.debug("unregisterWatch:{}", key);
        }
    }
    
    protected void createNamespace() throws KeeperException, InterruptedException {
        createNamespace(Constants.NOTHING_DATA);
    }
    
    private void createNamespace(final byte[] date) throws KeeperException, InterruptedException {
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
        holder.getZooKeeper().exists(rootNode, WatcherCreator.deleteWatcher(new Listener(rootNode) {
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
