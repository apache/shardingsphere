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

package org.apache.shardingsphere.readwritesplitting.distsql.update;

import org.apache.shardingsphere.infra.exception.rule.DuplicateRuleNamesException;
import org.apache.shardingsphere.readwritesplitting.distsql.exception.InvalidLoadBalancersException;
import org.apache.shardingsphere.infra.exception.rule.ResourceNotExistedException;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.segment.ReadwriteSplittingRuleSegment;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.CreateReadwriteSplittingRuleStatement;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class CreateReadwriteSplittingRuleStatementUpdaterTest {
    
    private final CreateReadwriteSplittingRuleStatementUpdater updater = new CreateReadwriteSplittingRuleStatementUpdater();
    
    @Test(expected = DuplicateRuleNamesException.class)
    public void assertCheckSQLStatementWithDuplicateRuleNames() {
        updater.checkSQLStatement("foo", createSQLStatement("TEST"), getCurrentRuleConfig(), mock(ShardingSphereResource.class));
    }
    
    @Test(expected = ResourceNotExistedException.class)
    public void assertCheckSQLStatementWithoutExistedResources() {
        ShardingSphereResource resource = mock(ShardingSphereResource.class);
        when(resource.getNotExistedResources(any())).thenReturn(Arrays.asList("read_ds_0", "read_ds_1"));
        updater.checkSQLStatement("foo", createSQLStatement("TEST"), null, resource);
    }
    
    @Test(expected = InvalidLoadBalancersException.class)
    public void assertCheckSQLStatementWithoutToBeCreatedLoadBalancers() {
        updater.checkSQLStatement("foo", createSQLStatement("INVALID_TYPE"), null, mock(ShardingSphereResource.class));
    }
    
    private CreateReadwriteSplittingRuleStatement createSQLStatement(final String loadBalancerName) {
        ReadwriteSplittingRuleSegment ruleSegment = new ReadwriteSplittingRuleSegment("readwrite_ds", "write_ds", Arrays.asList("read_ds_0", "read_ds_1"), loadBalancerName, new Properties());
        return new CreateReadwriteSplittingRuleStatement(Collections.singleton(ruleSegment));
    }
    
    private ReadwriteSplittingRuleConfiguration getCurrentRuleConfig() {
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig
                = new ReadwriteSplittingDataSourceRuleConfiguration("readwrite_ds", "", "write_ds", Arrays.asList("read_ds_0", "read_ds_1"), "TEST");
        return new ReadwriteSplittingRuleConfiguration(new LinkedList<>(Collections.singleton(dataSourceRuleConfig)), new HashMap<>());
    }
}
