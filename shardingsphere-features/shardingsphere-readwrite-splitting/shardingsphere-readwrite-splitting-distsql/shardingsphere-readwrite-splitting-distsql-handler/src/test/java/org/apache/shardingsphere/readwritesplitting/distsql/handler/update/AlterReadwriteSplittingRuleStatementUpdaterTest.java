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

package org.apache.shardingsphere.readwritesplitting.distsql.handler.update;

import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.resource.RequiredResourceMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.segment.ReadwriteSplittingRuleSegment;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.AlterReadwriteSplittingRuleStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class AlterReadwriteSplittingRuleStatementUpdaterTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereMetaData shardingSphereMetaData;
    
    @Mock
    private ShardingSphereResource resource;
    
    private final AlterReadwriteSplittingRuleStatementUpdater updater = new AlterReadwriteSplittingRuleStatementUpdater();
    
    @Before
    public void before() {
        when(shardingSphereMetaData.getResource()).thenReturn(resource);
    }
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertCheckSQLStatementWithoutCurrentRule() throws DistSQLException {
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement("TEST"), null);
    }
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertCheckSQLStatementWithoutToBeAlteredRules() throws DistSQLException {
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement("TEST"), new ReadwriteSplittingRuleConfiguration(Collections.emptyList(), Collections.emptyMap()));
    }
    
    @Test(expected = RequiredResourceMissedException.class)
    public void assertCheckSQLStatementWithoutExistedResources() throws DistSQLException {
        when(resource.getNotExistedResources(any())).thenReturn(Collections.singleton("read_ds_0"));
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement("TEST"), createCurrentRuleConfiguration());
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertCheckSQLStatementWithoutToBeAlteredLoadBalancers() throws DistSQLException {
        when(shardingSphereMetaData.getRuleMetaData().findRules(any())).thenReturn(Collections.emptyList());
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement("INVALID_TYPE"), createCurrentRuleConfiguration());
    }
    
    private AlterReadwriteSplittingRuleStatement createSQLStatement(final String loadBalancerTypeName) {
        ReadwriteSplittingRuleSegment ruleSegment = new ReadwriteSplittingRuleSegment("readwrite_ds", "write_ds", Arrays.asList("read_ds_0", "ds_read_ds_1"), loadBalancerTypeName, new Properties());
        return new AlterReadwriteSplittingRuleStatement(Collections.singleton(ruleSegment));
    }
    
    private ReadwriteSplittingRuleConfiguration createCurrentRuleConfiguration() {
        Properties props = new Properties();
        props.setProperty("write-data-source-name", "ds_write");
        props.setProperty("read-data-source-names", "read_ds_0,read_ds_1");
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig =
                new ReadwriteSplittingDataSourceRuleConfiguration("readwrite_ds", "Static", props, "TEST");
        return new ReadwriteSplittingRuleConfiguration(new LinkedList<>(Collections.singleton(dataSourceRuleConfig)), Collections.emptyMap());
    }
}
