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
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.DatabaseDiscoveryProviderAlgorithmSegment;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.CreateDatabaseDiscoveryTypeStatement;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public final class CreateDatabaseDiscoveryProviderAlgorithmStatementUpdaterTest {
    
    private final CreateDatabaseDiscoveryTypeStatementUpdater updater = new CreateDatabaseDiscoveryTypeStatementUpdater();
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Test(expected = DuplicateRuleException.class)
    public void assertCheckSQLStatementWithDuplicate() throws DistSQLException {
        DatabaseDiscoveryDataSourceRuleConfiguration dataSourceRuleConfig = new DatabaseDiscoveryDataSourceRuleConfiguration("readwrite_ds", Collections.emptyList(), "ha-heartbeat", "test");
        Collection<DatabaseDiscoveryProviderAlgorithmSegment> algorithmSegments = Arrays.asList(
                new DatabaseDiscoveryProviderAlgorithmSegment("discovery_type", new AlgorithmSegment("DISTSQL.FIXTURE", new Properties())),
                new DatabaseDiscoveryProviderAlgorithmSegment("discovery_type", new AlgorithmSegment("DISTSQL.FIXTURE", new Properties())));
        updater.checkSQLStatement(database, new CreateDatabaseDiscoveryTypeStatement(algorithmSegments),
                new DatabaseDiscoveryRuleConfiguration(Collections.singleton(dataSourceRuleConfig), Collections.emptyMap(), Collections.emptyMap()));
    }
    
    @Test(expected = DuplicateRuleException.class)
    public void assertCheckSQLStatementWithExist() throws DistSQLException {
        DatabaseDiscoveryDataSourceRuleConfiguration dataSourceRuleConfig = new DatabaseDiscoveryDataSourceRuleConfiguration("readwrite_ds", Collections.emptyList(), "ha-heartbeat", "test");
        Collection<DatabaseDiscoveryProviderAlgorithmSegment> algorithmSegments = Collections.singletonList(
                new DatabaseDiscoveryProviderAlgorithmSegment("discovery_type", new AlgorithmSegment("DISTSQL.FIXTURE", new Properties())));
        updater.checkSQLStatement(database, new CreateDatabaseDiscoveryTypeStatement(algorithmSegments),
                new DatabaseDiscoveryRuleConfiguration(Collections.singleton(dataSourceRuleConfig), Collections.emptyMap(),
                        Collections.singletonMap("discovery_type", new AlgorithmConfiguration("DISTSQL.FIXTURE", new Properties()))));
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertCheckSQLStatementWithDatabaseDiscoveryType() throws DistSQLException {
        Collection<DatabaseDiscoveryProviderAlgorithmSegment> algorithmSegments = Collections.singleton(
                new DatabaseDiscoveryProviderAlgorithmSegment("discovery_type", new AlgorithmSegment("INVALID_TYPE", new Properties())));
        updater.checkSQLStatement(database, new CreateDatabaseDiscoveryTypeStatement(algorithmSegments), null);
    }
    
    @Test
    public void assertBuildAndUpdate() throws DistSQLException {
        Collection<DatabaseDiscoveryProviderAlgorithmSegment> algorithmSegments = Collections.singleton(
                new DatabaseDiscoveryProviderAlgorithmSegment("discovery_type", new AlgorithmSegment("DISTSQL.FIXTURE", new Properties())));
        updater.checkSQLStatement(database, new CreateDatabaseDiscoveryTypeStatement(algorithmSegments), null);
        DatabaseDiscoveryRuleConfiguration databaseDiscoveryRuleConfig =
                (DatabaseDiscoveryRuleConfiguration) updater.buildToBeCreatedRuleConfiguration(new CreateDatabaseDiscoveryTypeStatement(algorithmSegments));
        DatabaseDiscoveryRuleConfiguration currentConfig = new DatabaseDiscoveryRuleConfiguration(new LinkedList<>(), new LinkedHashMap<>(), new LinkedHashMap<>());
        updater.updateCurrentRuleConfiguration(currentConfig, databaseDiscoveryRuleConfig);
        assertThat(currentConfig.getDiscoveryTypes().size(), is(1));
        assertThat(currentConfig.getDiscoveryTypes().get("discovery_type").getType(), is("DISTSQL.FIXTURE"));
    }
}
