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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;

import java.io.IOException;
import java.util.List;

/*
 * @author lidongbo
 */
@Setter(value = AccessLevel.PROTECTED)
@Getter(value = AccessLevel.PROTECTED)
public abstract class BaseClientFactory {
    private BaseClient client;
    
    private io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.section.Listener globalListener;
    
    private String namespace;
    
    private String scheme;
    
    private byte[] auth;
    
    private List<ACL> authorities;
    
    private BaseContext context;
    
    /**
     * start.
     *
     * @return client
     * @throws IOException IO Exception
     * @throws InterruptedException InterruptedException
     */
    public IClient start() throws IOException, InterruptedException {
        client.setRootNode(namespace);
        if (scheme == null) {
            authorities = ZooDefs.Ids.OPEN_ACL_UNSAFE;
        }
        client.setAuthorities(scheme, auth, authorities);
        client.start();
        if (globalListener != null) {
            client.registerWatch(globalListener);
        }
        return client;
    }
}
