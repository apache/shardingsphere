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

import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.section.ZookeeperEventListener;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class TestSupport {
    
    public static final String AUTH = "digest";
    
    public static final String SERVERS = "localhost:3181";
    
    public static final int SESSION_TIMEOUT = 200000;
    
    public static final String ROOT = "test";
    
    /**
     * Test exec.
     *
     * @return listener ZookeeperEventListener
     */
    public static ZookeeperEventListener buildListener() {
        return new ZookeeperEventListener() {
            
            @Override
            public void process(final WatchedEvent event) {
                log.debug(event.toString());
            }
        };
    }
}
