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
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.utility.Constants;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.utility.PathUtil;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.base.BaseClientFactory;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.section.ClientContext;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.section.Listener;
import org.apache.zookeeper.data.ACL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/*
 * @author lidongbo
 */
public class ClientFactory extends BaseClientFactory {
    //    private static final String CLIENT_EXCLUSIVE_NODE = "ZKC";
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientFactory.class);
    
    private DelayRetryPolicy delayRetryPolicy;
    
    /**
     * create a new client.
     *
     * @param servers servers
     * @param sessionTimeoutMilliseconds sessionTimeoutMilliseconds
     * @return ClientFactory this
     */
    public ClientFactory newClient(final String servers, final int sessionTimeoutMilliseconds) {
        int wait = sessionTimeoutMilliseconds;
        if (sessionTimeoutMilliseconds == 0) {
            wait = Constants.WAIT;
        }
        setContext(new ClientContext(servers, wait));
        setClient(new UsualClient(getContext()));
        LOGGER.debug("new usual client");
        return this;
    }
    
    /*
    * used for create new clients through a existing client
    * this client is not perhaps the client
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
     * wait to register global listener.
     *
     * @param globalListener globalListener
     * @return ClientFactory this
     */
    public ClientFactory watch(final Listener globalListener) {
        setGlobalListener(globalListener);
        return this;
    }
    
    /**
     * set client namespace.
     *
     * @param namespace namespace
     * @return ClientFactory this
     */
    public ClientFactory setClientNamespace(final String namespace) {
        setNamespace(PathUtil.checkPath(namespace));
        return this;
    }
    
    /**
     * authorization.
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
     * set delay retry policy.
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
