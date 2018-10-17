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

package io.shardingsphere.orchestration.reg.newzk.client.zookeeper.section;

import lombok.Getter;
import lombok.Setter;
import org.apache.zookeeper.WatchedEvent;

/**
 * Watch event listener.
 *
 * @author lidongbo
 */
@Getter
public abstract class ZookeeperEventListener {
    
    private final String key;
    
    @Setter
    private String path;
    
    public ZookeeperEventListener() {
        this(null);
    }
    
    public ZookeeperEventListener(final String path) {
        this.key = path + System.currentTimeMillis();
        this.path = path;
    }
    
    /**
     * Process.
     *
     * @param event event
     */
    public abstract void process(WatchedEvent event);
}
