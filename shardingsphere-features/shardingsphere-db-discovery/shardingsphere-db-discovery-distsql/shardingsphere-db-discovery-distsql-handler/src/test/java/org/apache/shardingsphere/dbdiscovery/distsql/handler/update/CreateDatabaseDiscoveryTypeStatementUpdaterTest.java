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

package org.apache.shardingsphere.dbdiscovery.distsql.handler.update;

import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.DatabaseDiscoveryTypeSegment;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.CreateDatabaseDiscoveryTypeStatement;
import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryType;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public final class CreateDatabaseDiscoveryTypeStatementUpdaterTest {
    
    static {
        ShardingSphereServiceLoader.register(DatabaseDiscoveryType.class);
    }
    
    @Mock
    private ShardingSphereMetaData shardingSphereMetaData;
    
    private final CreateDatabaseDiscoveryTypeStatementUpdater updater = new CreateDatabaseDiscoveryTypeStatementUpdater();
    
    @Test(expected = DuplicateRuleException.class)
    public void assertCheckSQLStatementWithDuplicate() throws DistSQLException {
        DatabaseDiscoveryDataSourceRuleConfiguration dataSourceRuleConfig = new DatabaseDiscoveryDataSourceRuleConfiguration("pr_ds", Collections.emptyList(), "ha-heartbeat", "test");
        List<DatabaseDiscoveryTypeSegment> databaseDiscoveryTypeSegments = Arrays.asList(
                new DatabaseDiscoveryTypeSegment("discovery_type", new AlgorithmSegment("mgr", new Properties())),
                new DatabaseDiscoveryTypeSegment("discovery_type", new AlgorithmSegment("mgr", new Properties())));
        updater.checkSQLStatement(shardingSphereMetaData, new CreateDatabaseDiscoveryTypeStatement(databaseDiscoveryTypeSegments),
                new DatabaseDiscoveryRuleConfiguration(Collections.singleton(dataSourceRuleConfig), Collections.emptyMap(), Collections.emptyMap()));
    }
    
    @Test(expected = DuplicateRuleException.class)
    public void assertCheckSQLStatementWithExist() throws DistSQLException {
        DatabaseDiscoveryDataSourceRuleConfiguration dataSourceRuleConfig = new DatabaseDiscoveryDataSourceRuleConfiguration("pr_ds", Collections.emptyList(), "ha-heartbeat", "test");
        List<DatabaseDiscoveryTypeSegment> databaseDiscoveryTypeSegments = Collections.singletonList(new DatabaseDiscoveryTypeSegment("discovery_type", new AlgorithmSegment("mgr", new Properties())));
        updater.checkSQLStatement(shardingSphereMetaData, new CreateDatabaseDiscoveryTypeStatement(databaseDiscoveryTypeSegments),
                new DatabaseDiscoveryRuleConfiguration(Collections.singleton(dataSourceRuleConfig), Collections.emptyMap(), 
                        Collections.singletonMap("discovery_type", new ShardingSphereAlgorithmConfiguration("mgr", new Properties()))));
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertCheckSQLStatementWithDatabaseDiscoveryType() throws DistSQLException {
        Set<DatabaseDiscoveryTypeSegment> discoveryTypeSegments = Collections.singleton(new DatabaseDiscoveryTypeSegment("discovery_type", new AlgorithmSegment("INVALID_TYPE", new Properties())));
        updater.checkSQLStatement(shardingSphereMetaData, new CreateDatabaseDiscoveryTypeStatement(discoveryTypeSegments), null);
    }
    
    @Test
    public void assertBuildAndUpdate() throws DistSQLException {
        Set<DatabaseDiscoveryTypeSegment> discoveryTypeSegments = Collections.singleton(new DatabaseDiscoveryTypeSegment("discovery_type", new AlgorithmSegment("MGR", new Properties())));
        updater.checkSQLStatement(shardingSphereMetaData, new CreateDatabaseDiscoveryTypeStatement(discoveryTypeSegments), null);
        DatabaseDiscoveryRuleConfiguration databaseDiscoveryRuleConfiguration
                = (DatabaseDiscoveryRuleConfiguration) updater.buildToBeCreatedRuleConfiguration(new CreateDatabaseDiscoveryTypeStatement(discoveryTypeSegments));
        DatabaseDiscoveryRuleConfiguration currentConfiguration = new DatabaseDiscoveryRuleConfiguration(new LinkedList<>(), new LinkedHashMap<>(), new LinkedHashMap<>());
        updater.updateCurrentRuleConfiguration(currentConfiguration, databaseDiscoveryRuleConfiguration);
        assertThat(currentConfiguration.getDiscoveryTypes().size(), is(1));
        assertThat(currentConfiguration.getDiscoveryTypes().get("discovery_type").getType(), is("MGR"));
    }
}
