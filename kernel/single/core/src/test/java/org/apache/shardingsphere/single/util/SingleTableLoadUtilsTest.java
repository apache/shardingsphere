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

package org.apache.shardingsphere.single.util;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.table.TableMapperRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.TreeSet;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
class SingleTableLoadUtilsTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertGetExcludedTables() {
        ShardingSphereRule builtRule1 = mock(ShardingSphereRule.class);
        TableMapperRuleAttribute tableMapperRuleAttribute = mock(TableMapperRuleAttribute.class);
        when(tableMapperRuleAttribute.getDistributedTableNames()).thenReturn(Collections.singleton("dist_tbl"));
        when(tableMapperRuleAttribute.getActualTableNames()).thenReturn(Collections.singleton("actual_tbl"));
        when(builtRule1.getAttributes()).thenReturn(new RuleAttributes(tableMapperRuleAttribute));
        ShardingSphereRule builtRule2 = mock(ShardingSphereRule.class);
        when(builtRule2.getAttributes()).thenReturn(new RuleAttributes());
        assertThat(SingleTableLoadUtils.getExcludedTables(Arrays.asList(builtRule1, builtRule2)), is(new TreeSet<>(Arrays.asList("dist_tbl", "actual_tbl"))));
    }
    
    @Test
    void assertGetFeatureRequiredSingleTablesWithEmptyEnhancedTableNames() {
        ShardingSphereRule builtRule1 = mock(ShardingSphereRule.class);
        TableMapperRuleAttribute tableMapperRuleAttribute = mock(TableMapperRuleAttribute.class);
        when(tableMapperRuleAttribute.getEnhancedTableNames()).thenReturn(Collections.emptyList());
        when(builtRule1.getAttributes()).thenReturn(new RuleAttributes(tableMapperRuleAttribute));
        ShardingSphereRule builtRule2 = mock(ShardingSphereRule.class);
        when(builtRule2.getAttributes()).thenReturn(new RuleAttributes());
        assertThat(SingleTableLoadUtils.getFeatureRequiredSingleTables(Arrays.asList(builtRule1, builtRule2)), is(Collections.emptySet()));
    }
    
    @Test
    void assertGetFeatureRequiredSingleTablesWithDistributedTableNames() {
        ShardingSphereRule builtRule1 = mock(ShardingSphereRule.class);
        TableMapperRuleAttribute tableMapperRuleAttribute = mock(TableMapperRuleAttribute.class);
        when(tableMapperRuleAttribute.getEnhancedTableNames()).thenReturn(Collections.singleton("enhanced_tbl"));
        when(tableMapperRuleAttribute.getDistributedTableNames()).thenReturn(Collections.singleton("dist_tbl"));
        when(builtRule1.getAttributes()).thenReturn(new RuleAttributes(tableMapperRuleAttribute));
        ShardingSphereRule builtRule2 = mock(ShardingSphereRule.class);
        when(builtRule2.getAttributes()).thenReturn(new RuleAttributes());
        assertThat(SingleTableLoadUtils.getFeatureRequiredSingleTables(Arrays.asList(builtRule1, builtRule2)), is(Collections.emptySet()));
    }
    
    @Test
    void assertGetFeatureRequiredSingleTablesWithoutDistributedTableNames() {
        ShardingSphereRule builtRule1 = mock(ShardingSphereRule.class);
        TableMapperRuleAttribute tableMapperRuleAttribute = mock(TableMapperRuleAttribute.class);
        when(tableMapperRuleAttribute.getEnhancedTableNames()).thenReturn(Collections.singleton("enhanced_tbl"));
        when(tableMapperRuleAttribute.getDistributedTableNames()).thenReturn(Collections.emptyList());
        when(builtRule1.getAttributes()).thenReturn(new RuleAttributes(tableMapperRuleAttribute));
        ShardingSphereRule builtRule2 = mock(ShardingSphereRule.class);
        when(builtRule2.getAttributes()).thenReturn(new RuleAttributes());
        assertThat(SingleTableLoadUtils.getFeatureRequiredSingleTables(Arrays.asList(builtRule1, builtRule2)), is(Collections.singleton("enhanced_tbl")));
    }
    
    @Test
    void assertSplitTableLines() {
        assertThat(SingleTableLoadUtils.splitTableLines(Arrays.asList("foo_tbl", "bar_tbl0,bar_tbl1")), is(new LinkedHashSet<>(Arrays.asList("foo_tbl", "bar_tbl0", "bar_tbl1"))));
    }
    
    @Test
    void assertConvertToDataNodes() {
        DataNode expectedDataNode1 = new DataNode("foo_ds", "foo_db", "foo_tbl");
        DataNode expectedDataNode2 = new DataNode("bar_ds", "foo_db", "bar_tbl");
        assertThat(SingleTableLoadUtils.convertToDataNodes("foo_db", databaseType, Arrays.asList("foo_ds.foo_tbl", "bar_ds.bar_tbl")),
                is(new LinkedHashSet<>(Arrays.asList(expectedDataNode1, expectedDataNode2))));
    }
    
    @Test
    void assertGetAllTablesNodeStr() {
        assertThat(SingleTableLoadUtils.getAllTablesNodeStr(databaseType), is("*.*"));
    }
    
    @Test
    void assertGetAllTablesNodeStrFromDataSource() {
        assertThat(SingleTableLoadUtils.getAllTablesNodeStrFromDataSource(databaseType, "foo_ds", "foo_schema"), is("foo_ds.*"));
    }
    
    @Test
    void assertGetDataNodeString() {
        assertThat(SingleTableLoadUtils.getDataNodeString(databaseType, "foo_ds", "foo_schema", "foo_tbl"), is("foo_ds.foo_tbl"));
    }
}
