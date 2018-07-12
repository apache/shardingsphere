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

package io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper;

import io.shardingsphere.jdbc.orchestration.reg.newzk.client.action.IClient;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.retry.DelayRetryPolicy;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.utility.PathUtil;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.utility.ZookeeperConstants;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.base.BaseClientFactory;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.section.ClientContext;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.section.ZookeeperEventListener;
import java.io.IOException;
import java.util.List;
import org.apache.zookeeper.data.ACL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Client factory.
 *
 * @author lidongbo
 */
public class ClientFactory extends BaseClientFactory {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientFactory.class);
    
    private DelayRetryPolicy delayRetryPolicy;
    
    /**
     * Create a new client.
     *
     * @param servers servers
     * @param sessionTimeoutMilliseconds sessionTimeoutMilliseconds
     * @return ClientFactory this
     */
    public ClientFactory newClient(final String servers, final int sessionTimeoutMilliseconds) {
        int wait = sessionTimeoutMilliseconds;
        if (sessionTimeoutMilliseconds == 0) {
            wait = ZookeeperConstants.WAIT;
        }
        setContext(new ClientContext(servers, wait));
        setClient(new UsualClient(getContext()));
        LOGGER.debug("new usual client");
        return this;
    }
    
    /*
    * Used for create new clients through a existing client.
    * This client is not perhaps the client.
    */
    synchronized BaseClientFactory newClientByOriginal(final boolean closeOriginal) {
        IClient oldClient = getClient();
        setClient(new UsualClient(getContext()));
        if (closeOriginal) {
            oldClient.close();
        }
        LOGGER.debug("new usual client by a existing client");
        return this;
    }
    
    ClientFactory newCacheClient(final String servers, final int sessionTimeoutMilliseconds) {
        setContext(new ClientContext(servers, sessionTimeoutMilliseconds));
        setClient(new CacheClient(getContext()));
        LOGGER.debug("new cache client");
        return this;
    }
    
    /**
     * Wait to register global listener.
     *
     * @param globalZookeeperEventListener globalListener
     * @return ClientFactory this
     */
    public ClientFactory watch(final ZookeeperEventListener globalZookeeperEventListener) {
        setGlobalZookeeperEventListener(globalZookeeperEventListener);
        return this;
    }
    
    /**
     * Set client namespace.
     *
     * @param namespace namespace
     * @return ClientFactory this
     */
    public ClientFactory setClientNamespace(final String namespace) {
        setNamespace(PathUtil.checkPath(namespace));
        return this;
    }
    
    /**
     * Authorization.
     *
     * @param scheme scheme
     * @param auth auth
     * @param authorities authorities
     * @return ClientFactory this
     */
    public ClientFactory authorization(final String scheme, final byte[] auth, final List<ACL> authorities) {
        setScheme(scheme);
        setAuth(auth);
        setAuthorities(authorities);
        return this;
    }
    
    /**
     * Set delay retry policy.
     *
     * @param delayRetryPolicy delayRetryPolicy
     * @return ClientFactory this
     */
    public ClientFactory setRetryPolicy(final DelayRetryPolicy delayRetryPolicy) {
        this.delayRetryPolicy = delayRetryPolicy;
        return this;
    }
    
    @Override
    public IClient start() throws IOException, InterruptedException {
        ((ClientContext) getContext()).setDelayRetryPolicy(delayRetryPolicy);
        ((ClientContext) getContext()).setClientFactory(this);
        return super.start();
    }
}
