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

import io.shardingsphere.orchestration.reg.newzk.client.action.IClient;
import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.section.ZookeeperEventListener;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Base client factory.
 *
 * @author lidongbo
 */
@Setter(value = AccessLevel.PROTECTED)
@Getter(value = AccessLevel.PROTECTED)
public abstract class BaseClientFactory {
    
    private BaseClient client;
    
    private ZookeeperEventListener globalZookeeperEventListener;
    
    private String namespace;
    
    private String scheme;
    
    private byte[] auth;
    
    private List<ACL> authorities;
    
    private BaseContext context;
    
    /**
     * Start.
     *
     * @return client
     * @throws IOException IO Exception
     * @throws InterruptedException InterruptedException
     */
    public IClient start() throws IOException, InterruptedException {
        prepareClient();
        client.start();
        return client;
    }
    
    /**
     * Start until Timeout.
     *
     * @param waitingTime waiting time
     * @param timeUnit time unit
     * @return connected or not
     * @throws IOException IO Exception
     * @throws InterruptedException interrupted exception
     * @throws KeeperException operation timeout exception
     */
    public IClient start(final int waitingTime, final TimeUnit timeUnit) throws IOException, InterruptedException, KeeperException {
        prepareClient();
        if (!client.start(waitingTime, timeUnit)) {
            client.close();
            throw new KeeperException.OperationTimeoutException();
        }
        return client;
    }
    
    private void prepareClient() {
        client.setRootNode(namespace);
        if (null == scheme) {
            authorities = ZooDefs.Ids.OPEN_ACL_UNSAFE;
        }
        client.setAuthorities(scheme, auth, authorities);
        if (null != globalZookeeperEventListener) {
            client.registerWatch(globalZookeeperEventListener);
        }
    }
}
