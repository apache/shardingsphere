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

package org.apache.shardingsphere.integration.data.pipline.cases;

import com.google.common.base.Joiner;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.integration.data.pipline.container.compose.BaseComposedContainer;
import org.apache.shardingsphere.integration.data.pipline.container.compose.DockerComposedContainer;
import org.apache.shardingsphere.integration.data.pipline.container.compose.LocalComposedContainer;
import org.apache.shardingsphere.integration.data.pipline.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.integration.data.pipline.env.enums.ITEnvTypeEnum;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.mode.repository.cluster.zookeeper.CuratorZookeeperRepository;
import org.junit.Before;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

@Getter(AccessLevel.PROTECTED)
public abstract class BaseITCase {
    
    @Getter(AccessLevel.NONE)
    private final BaseComposedContainer composedContainer;
    
    private ClusterPersistRepository clusterPersistRepository;
    
    public BaseITCase(final DatabaseType databaseType) {
        if (StringUtils.equalsIgnoreCase(IntegrationTestEnvironment.getInstance().getItEnvType(), ITEnvTypeEnum.DOCKER.name())) {
            composedContainer = new DockerComposedContainer(databaseType);
        } else {
            composedContainer = new LocalComposedContainer(databaseType);
        }
    }
    
    @Before
    public void setUp() {
        composedContainer.start();
        clusterPersistRepository = new CuratorZookeeperRepository();
        clusterPersistRepository.init(new ClusterPersistRepositoryConfiguration("ZooKeeper", "it_db", composedContainer.getGovernanceContainer().getServerLists(), new Properties()));
    }
    
    /**
     * Get proxy database connection.
     *
     * @return proxy database connection
     */
    @SneakyThrows(SQLException.class)
    public Connection getProxyConnection() {
        return composedContainer.getProxyConnection();
    }
    
    /**
     * Get database url, such as  ip:port.
     *
     * @return database url
     */
    public String getDatabaseUrl() {
        if (StringUtils.equalsIgnoreCase(IntegrationTestEnvironment.getInstance().getItEnvType(), ITEnvTypeEnum.DOCKER.name())) {
            return Joiner.on(":").join("db.host", composedContainer.getDatabaseContainer().getPort());
        } else {
            return Joiner.on(":").join(composedContainer.getDatabaseContainer().getHost(), composedContainer.getDatabaseContainer().getFirstMappedPort());
        }
    }
    
    /**
     * Query actual source database name.
     *
     * @return actual source database name list
     */
    public List<String> listSourceDatabaseName() {
        return composedContainer.getDatabaseContainer().getSourceDatabaseNames();
    }
    
    /**
     * Query actual target database name.
     *
     * @return actual target database name list
     */
    public List<String> listTargetDatabaseName() {
        return composedContainer.getDatabaseContainer().getTargetDatabaseNames();
    }
}
