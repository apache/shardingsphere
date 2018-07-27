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

import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.section.ZookeeperEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/*
 * Base context.
 *
 * @author lidongbo
 */
@Getter
@Setter(value = AccessLevel.PROTECTED)
public abstract class BaseContext {
    
    private String servers;
    
    private int sessionTimeOut;
    
    private String scheme;
    
    private byte[] auth;
    
    private ZookeeperEventListener globalZookeeperEventListener;
    
    private final Map<String, ZookeeperEventListener> watchers = new ConcurrentHashMap<>();
    
    private final List<String> waitCheckPaths = new ArrayList<>();
    
    /**
     * Close.
     */
    public void close() {
        this.watchers.clear();
    }
}
