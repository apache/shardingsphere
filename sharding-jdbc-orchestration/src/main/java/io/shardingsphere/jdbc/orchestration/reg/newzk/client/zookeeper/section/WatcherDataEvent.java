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

package io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.section;

import com.google.common.base.Strings;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.utility.ZookeeperConstants;
import lombok.Getter;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.proto.WatcherEvent;

/*
 * Watch event with path data.
 *
 * @author lidongbo
 */
public class WatcherDataEvent extends WatchedEvent {
    
    @Getter
    private final String data;
    
    public WatcherDataEvent(final WatcherEvent event, final ZooKeeper zooKeeper) {
        super(event);
        data = initData(event, zooKeeper);
    }
    
    private String initData(final WatcherEvent event, final ZooKeeper zooKeeper) {
        if (Strings.isNullOrEmpty(event.getPath())) {
            return null;
        }
        byte[] result;
        try {
            result = zooKeeper.getData(event.getPath(), true, null);
        } catch (final KeeperException | InterruptedException ex) {
            return null;
        }
        return new String(result, ZookeeperConstants.UTF_8);
    }
}
