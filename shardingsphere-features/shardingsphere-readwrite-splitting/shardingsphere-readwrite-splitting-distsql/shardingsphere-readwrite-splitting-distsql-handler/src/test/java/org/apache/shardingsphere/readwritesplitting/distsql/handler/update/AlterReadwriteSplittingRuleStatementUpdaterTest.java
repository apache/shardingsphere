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
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.segment.ReadwriteSplittingRuleSegment;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.AlterReadwriteSplittingRuleStatement;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class AlterReadwriteSplittingRuleStatementUpdaterTest {
    
    private final AlterReadwriteSplittingRuleStatementUpdater updater = new AlterReadwriteSplittingRuleStatementUpdater();
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertCheckSQLStatementWithoutCurrentRule() throws DistSQLException {
        updater.checkSQLStatement("foo", createSQLStatement("TEST"), null, mock(ShardingSphereResource.class));
    }
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertCheckSQLStatementWithoutToBeAlteredRules() throws DistSQLException {
        updater.checkSQLStatement("foo", createSQLStatement("TEST"), new ReadwriteSplittingRuleConfiguration(Collections.emptyList(), Collections.emptyMap()), mock(ShardingSphereResource.class));
    }
    
    @Test(expected = RequiredResourceMissedException.class)
    public void assertCheckSQLStatementWithoutExistedResources() throws DistSQLException {
        ShardingSphereResource resource = mock(ShardingSphereResource.class);
        when(resource.getNotExistedResources(any())).thenReturn(Collections.singleton("read_ds_0"));
        updater.checkSQLStatement("foo", createSQLStatement("TEST"), createCurrentRuleConfiguration(), resource);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertCheckSQLStatementWithoutToBeAlteredLoadBalancers() throws DistSQLException {
        updater.checkSQLStatement("foo", createSQLStatement("INVALID_TYPE"), createCurrentRuleConfiguration(), mock(ShardingSphereResource.class));
    }
    
    private AlterReadwriteSplittingRuleStatement createSQLStatement(final String loadBalancerTypeName) {
        ReadwriteSplittingRuleSegment ruleSegment = new ReadwriteSplittingRuleSegment("readwrite_ds", "write_ds", Arrays.asList("read_ds_0", "ds_read_ds_1"), loadBalancerTypeName, new Properties());
        return new AlterReadwriteSplittingRuleStatement(Collections.singleton(ruleSegment));
    }
    
    private ReadwriteSplittingRuleConfiguration createCurrentRuleConfiguration() {
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig = 
                new ReadwriteSplittingDataSourceRuleConfiguration("readwrite_ds", null, "ds_write", Arrays.asList("read_ds_0", "read_ds_1"), "TEST");
        return new ReadwriteSplittingRuleConfiguration(new LinkedList<>(Collections.singleton(dataSourceRuleConfig)), new HashMap<>());
    }
}
