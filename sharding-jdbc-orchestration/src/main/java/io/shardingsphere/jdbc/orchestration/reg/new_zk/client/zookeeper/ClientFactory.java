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

package io.shardingsphere.jdbc.orchestration.reg.new_zk.client.zookeeper;

import io.shardingsphere.jdbc.orchestration.reg.new_zk.client.action.IClient;
import io.shardingsphere.jdbc.orchestration.reg.new_zk.client.retry.DelayRetryPolicy;
import io.shardingsphere.jdbc.orchestration.reg.new_zk.client.utility.Constants;
import io.shardingsphere.jdbc.orchestration.reg.new_zk.client.zookeeper.base.BaseClientFactory;
import io.shardingsphere.jdbc.orchestration.reg.new_zk.client.zookeeper.section.ClientContext;
import io.shardingsphere.jdbc.orchestration.reg.new_zk.client.zookeeper.section.Listener;
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
    private static final Logger logger = LoggerFactory.getLogger(ClientFactory.class);
    private DelayRetryPolicy delayRetryPolicy;
    
    public ClientFactory(){}
    
    public ClientFactory newClient(final String servers, final int sessionTimeoutMilliseconds) {
        int wait = sessionTimeoutMilliseconds;
        if (sessionTimeoutMilliseconds == 0){
            wait = Constants.WAIT;
        }
        this.context = new ClientContext(servers, wait);
        client = new UsualClient(context);
        logger.debug("new usual client");
        return this;
    }
    
    /*
    * used for create new clients through a existing client
    * this client is not perhaps the client
    */
    public synchronized BaseClientFactory newClientByOriginal(boolean closeOriginal) {
        IClient oldClient = this.client;
        client = new UsualClient(context);
        if (closeOriginal){
            oldClient.close();
        }
        logger.debug("new usual client by a existing client");
        return this;
    }
    
    public ClientFactory newCacheClient(final String servers, final int sessionTimeoutMilliseconds) {
        this.context = new ClientContext(servers, sessionTimeoutMilliseconds);
        client = new CacheClient(context);
        logger.debug("new cache client");
        return this;
    }
    
    public ClientFactory watch(final Listener listener){
        globalListener = listener;
        return this;
    }
    
    public ClientFactory setNamespace(String namespace) {
        if (!namespace.startsWith(Constants.PATH_SEPARATOR)){
            namespace = Constants.PATH_SEPARATOR + namespace;
        }
        this.namespace = namespace;
        return this;
    }
    
    public ClientFactory authorization(final String scheme, final byte[] auth, final List<ACL> authorities){
        this.scheme = scheme;
        this.auth = auth;
        this.authorities = authorities;
        return this;
    }
    
    public ClientFactory setRetryPolicy(final DelayRetryPolicy delayRetryPolicy){
        this.delayRetryPolicy = delayRetryPolicy;
        return this;
    }
    
    @Override
    public IClient start() throws IOException, InterruptedException {
        ((ClientContext)context).setDelayRetryPolicy(delayRetryPolicy);
        ((ClientContext)context).setClientFactory(this);
        return super.start();
    }
}
