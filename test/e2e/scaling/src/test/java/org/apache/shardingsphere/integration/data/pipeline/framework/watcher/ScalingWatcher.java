/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.integration.data.pipeline.framework.watcher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.integration.data.pipeline.framework.container.compose.BaseContainerComposer;
import org.apache.shardingsphere.integration.data.pipeline.framework.container.compose.DockerContainerComposer;
import org.apache.shardingsphere.integration.data.pipeline.framework.container.compose.NativeContainerComposer;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.mode.repository.cluster.zookeeper.ZookeeperRepository;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@RequiredArgsConstructor
@Slf4j
public class ScalingWatcher extends TestWatcher {
    
    private final BaseContainerComposer containerComposer;
    
    @Override
    protected void failed(final Throwable e, final Description description) {
        if (containerComposer instanceof NativeContainerComposer) {
            super.failed(e, description);
        }
    }
    
    // TODO now the metadata mistake is not reproduce, but keep this method, it may be used again later
    private void outputZookeeperData() {
        DockerContainerComposer dockerContainerComposer = (DockerContainerComposer) containerComposer;
        DatabaseType databaseType = dockerContainerComposer.getStorageContainer().getDatabaseType();
        String namespace = "it_db_" + databaseType.getType().toLowerCase();
        ClusterPersistRepositoryConfiguration config = new ClusterPersistRepositoryConfiguration("ZooKeeper", namespace,
                dockerContainerComposer.getGovernanceContainer().getServerLists(), new Properties());
        ClusterPersistRepository zookeeperRepository = new ZookeeperRepository();
        zookeeperRepository.init(config);
        List<String> childrenKeys = zookeeperRepository.getChildrenKeys("/");
        for (String each : childrenKeys) {
            if (!"scaling".equals(each)) {
                continue;
            }
            Map<String, String> nodeMap = new LinkedHashMap<>();
            addZookeeperData(each, "", zookeeperRepository, nodeMap);
            log.warn("zookeeper data, node:{}, data:{}", each, nodeMap);
        }
    }
    
    private void addZookeeperData(final String key, final String parentPath, final ClusterPersistRepository zookeeperRepository, final Map<String, String> nodeMap) {
        String path = String.join("/", parentPath, key);
        String data = zookeeperRepository.getDirectly(path);
        nodeMap.put(path, data);
        List<String> childrenKeys = zookeeperRepository.getChildrenKeys(path);
        for (String each : childrenKeys) {
            addZookeeperData(each, path, zookeeperRepository, nodeMap);
        }
    }
    
    @Override
    protected void finished(final Description description) {
        containerComposer.close();
    }
}
