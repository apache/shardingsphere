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

package org.apache.shardingsphere.readwritesplitting.distsql.handler.query;

import org.apache.shardingsphere.distsql.handler.executor.rql.resource.InUsedStorageUnitRetriever;
import org.apache.shardingsphere.distsql.statement.type.rql.rule.database.ShowRulesUsedStorageUnitStatement;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.readwritesplitting.config.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InUsedReadwriteSplittingStorageUnitRetrieverTest {
    
    @SuppressWarnings("unchecked")
    private final InUsedStorageUnitRetriever<ReadwriteSplittingRule> retriever = TypedSPILoader.getService(InUsedStorageUnitRetriever.class, ReadwriteSplittingRule.class);
    
    @Test
    void assertGetInUsedResourcesWithWriteDataSource() {
        ShowRulesUsedStorageUnitStatement sqlStatement = new ShowRulesUsedStorageUnitStatement("foo_unit_write", null);
        assertThat(retriever.getInUsedResources(sqlStatement, mockRule()), is(Collections.singleton("foo_ds")));
    }
    
    @Test
    void assertGetInUsedResourcesWithReadDataSource() {
        ShowRulesUsedStorageUnitStatement sqlStatement = new ShowRulesUsedStorageUnitStatement("foo_unit_read", null);
        assertThat(retriever.getInUsedResources(sqlStatement, mockRule()), is(Collections.singleton("foo_ds")));
    }
    
    private ReadwriteSplittingRule mockRule() {
        ReadwriteSplittingRule result = mock(ReadwriteSplittingRule.class, RETURNS_DEEP_STUBS);
        ReadwriteSplittingDataSourceGroupRuleConfiguration dataSourceGroupRuleConfig = new ReadwriteSplittingDataSourceGroupRuleConfiguration(
                "foo_ds", "foo_unit_write", Collections.singletonList("foo_unit_read"), "");
        when(result.getConfiguration().getDataSourceGroups()).thenReturn(Collections.singleton(dataSourceGroupRuleConfig));
        return result;
    }
}
