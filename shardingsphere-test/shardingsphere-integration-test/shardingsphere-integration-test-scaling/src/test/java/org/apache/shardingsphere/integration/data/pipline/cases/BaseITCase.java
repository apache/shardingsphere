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

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.integration.data.pipline.container.ComposedContainer;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.mode.repository.cluster.zookeeper.CuratorZookeeperRepository;
import org.apache.shardingsphere.test.integration.framework.container.atomic.governance.GovernanceContainer;
import org.junit.Before;

import javax.sql.DataSource;
import java.util.List;
import java.util.Properties;

@Getter(AccessLevel.PROTECTED)
public abstract class BaseITCase {
    
    private DataSource targetDataSource;
    
    @Getter(AccessLevel.NONE)
    private final ComposedContainer composedContainer;
    
    private String databaseNetworkAlias;
    
    private ClusterPersistRepository clusterPersistRepository;
    
    public BaseITCase(final DatabaseType databaseType) {
        composedContainer = new ComposedContainer(databaseType);
    }
    
    @Before
    public void setUp() {
        composedContainer.start();
        targetDataSource = composedContainer.getTargetDataSource();
        GovernanceContainer governanceContainer = composedContainer.getGovernanceContainer();
        databaseNetworkAlias = composedContainer.getDatabaseNetworkAlias();
        clusterPersistRepository = new CuratorZookeeperRepository();
        clusterPersistRepository.init(new ClusterPersistRepositoryConfiguration("ZooKeeper", "it_db", governanceContainer.getServerLists(), new Properties()));
        System.out.println(clusterPersistRepository.get("/"));
    }
    
    /**
     * Query actual source database name.
     *
     * @return actual source database name list
     */
    public List<String> listSourceDatabaseName() {
        return composedContainer.listSourceDatabaseName();
    }
    
    /**
     * Query actual target database name.
     *
     * @return actual target database name list
     */
    public List<String> listTargetDatabaseName() {
        return composedContainer.listTargetDatabaseName();
    }
}
