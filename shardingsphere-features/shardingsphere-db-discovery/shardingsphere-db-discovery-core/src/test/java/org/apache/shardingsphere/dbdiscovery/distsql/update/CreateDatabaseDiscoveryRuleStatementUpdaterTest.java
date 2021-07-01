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

package org.apache.shardingsphere.dbdiscovery.distsql.update;

import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.distsql.exception.InvalidDatabaseDiscoveryTypesException;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.DatabaseDiscoveryRuleSegment;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.CreateDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.infra.exception.rule.DuplicateRuleNamesException;
import org.apache.shardingsphere.infra.exception.rule.ResourceNotExistedException;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class CreateDatabaseDiscoveryRuleStatementUpdaterTest {
    
    private final CreateDatabaseDiscoveryRuleStatementUpdater updater = new CreateDatabaseDiscoveryRuleStatementUpdater();
    
    @Test(expected = DuplicateRuleNamesException.class)
    public void assertCheckSQLStatementWithDuplicateRuleNames() {
        DatabaseDiscoveryDataSourceRuleConfiguration dataSourceRuleConfig = new DatabaseDiscoveryDataSourceRuleConfiguration("pr_ds", Collections.emptyList(), "test");
        updater.checkSQLStatement(
                "foo", createSQLStatement("TEST"), new DatabaseDiscoveryRuleConfiguration(Collections.singleton(dataSourceRuleConfig), Collections.emptyMap()), mock(ShardingSphereResource.class));
    }
    
    @Test(expected = ResourceNotExistedException.class)
    public void assertCheckSQLStatementWithoutExistedResources() {
        ShardingSphereResource resource = mock(ShardingSphereResource.class);
        when(resource.getNotExistedResources(any())).thenReturn(Collections.singleton("ds_read_0"));
        updater.checkSQLStatement("foo", createSQLStatement("TEST"), null, resource);
    }
    
    @Test(expected = InvalidDatabaseDiscoveryTypesException.class)
    public void assertCheckSQLStatementWithDatabaseDiscoveryType() {
        updater.checkSQLStatement("foo", createSQLStatement("INVALID_TYPE"), null, mock(ShardingSphereResource.class));
    }
    
    private CreateDatabaseDiscoveryRuleStatement createSQLStatement(final String discoveryTypeName) {
        DatabaseDiscoveryRuleSegment ruleSegment = new DatabaseDiscoveryRuleSegment("pr_ds", Arrays.asList("ds_read_0", "ds_read_1"), discoveryTypeName, new Properties());
        return new CreateDatabaseDiscoveryRuleStatement(Collections.singleton(ruleSegment));
    }
}
