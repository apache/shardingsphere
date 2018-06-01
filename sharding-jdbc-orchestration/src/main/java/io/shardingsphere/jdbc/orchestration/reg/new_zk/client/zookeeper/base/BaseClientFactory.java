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

package io.shardingsphere.jdbc.orchestration.reg.new_zk.client.zookeeper.base;

import io.shardingsphere.jdbc.orchestration.reg.new_zk.client.action.IClient;
import io.shardingsphere.jdbc.orchestration.reg.new_zk.client.zookeeper.section.Listener;
import org.apache.zookeeper.data.ACL;

import java.io.IOException;
import java.util.List;

import static org.apache.zookeeper.ZooDefs.Ids.OPEN_ACL_UNSAFE;

/*
 * @author lidongbo
 */
public abstract class BaseClientFactory {
    protected BaseClient client;
    protected Listener globalListener;
    protected String namespace;
    protected String scheme;
    protected byte[] auth;
    protected List<ACL> authorities;
    protected BaseContext context;
    
    public IClient start() throws IOException, InterruptedException {
        client.setRootNode(namespace);
        if(scheme == null) {
            authorities = OPEN_ACL_UNSAFE;
        }
        client.setAuthorities(scheme , auth, authorities);
        client.start();
        if (globalListener != null) {
            client.registerWatch(globalListener);
        }
        return client;
    }
}
