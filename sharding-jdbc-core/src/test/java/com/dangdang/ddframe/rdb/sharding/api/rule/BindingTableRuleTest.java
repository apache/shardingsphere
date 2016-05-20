/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.api.rule;

import org.junit.Test;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class BindingTableRuleTest {
    
    @Test
    public void assertHasLogicTable() {
        assertTrue(createBindingTableRule().hasLogicTable("logicTable"));
    }
    
    @Test
    public void assertNotHasLogicTable() {
        assertFalse(createBindingTableRule().hasLogicTable("newTable"));
    }
    
    @Test
    public void assertGetBindingActualTablesSuccess() {
        assertThat(createBindingTableRule().getBindingActualTable("ds1", "subLogicTable", "table_1"), is("sub_table_1"));
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertGetBindingActualTablesFailureWhenNotFound() {
        createBindingTableRule().getBindingActualTable("no_ds", "subLogicTable", "table_1");
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void assertGetBindingActualTablesFailureWhenIsDynamicTable() {
        createDynamicBindingTableRule().getBindingActualTable("no_ds", "subLogicTable", "table_1");
    }
    
    @Test
    public void assertGetAllLogicTables() {
        assertThat(createBindingTableRule().getAllLogicTables(), is((Collection<String>) Arrays.asList("logicTable", "subLogicTable")));
    }
    
    @Test
    public void assertGetTableRules() {
        assertThat(createBindingTableRule().getTableRules().size(), is(2));
        assertThat(createBindingTableRule().getTableRules().get(0).getLogicTable(), is(createTableRule().getLogicTable()));
        assertThat(createBindingTableRule().getTableRules().get(0).getActualTables(), is(createTableRule().getActualTables()));
        assertThat(createBindingTableRule().getTableRules().get(1).getLogicTable(), is(createSubTableRule().getLogicTable()));
        assertThat(createBindingTableRule().getTableRules().get(1).getActualTables(), is(createSubTableRule().getActualTables()));
    }
    
    private BindingTableRule createBindingTableRule() {
        return new BindingTableRule(Arrays.asList(createTableRule(), createSubTableRule()));
    }
    
    private TableRule createTableRule() {
        return TableRule.builder("logicTable").actualTables(Arrays.asList("ds1.table_0", "ds1.table_1", "ds2.table_0", "ds2.table_1")).dataSourceRule(createDataSourceRule()).build();
    }
    
    private TableRule createSubTableRule() {
        return TableRule.builder("subLogicTable").actualTables(Arrays.asList("ds1.sub_table_0", "ds1.sub_table_1", "ds2.sub_table_0", "ds2.sub_table_1"))
                .dataSourceRule(createDataSourceRule()).build();
    }
    
    private DataSourceRule createDataSourceRule() {
        Map<String, DataSource> dataSourceMap = new HashMap<>(2);
        dataSourceMap.put("ds1", null);
        dataSourceMap.put("ds2", null);
        return new DataSourceRule(dataSourceMap);
    }
    
    private BindingTableRule createDynamicBindingTableRule() {
        return new BindingTableRule(Arrays.asList(createDynamicTableRule(), createDynamicSubTableRule()));
    }
    
    private TableRule createDynamicTableRule() {
        return TableRule.builder("logicTable").dynamic(true).dataSourceRule(createDataSourceRule()).build();
    }
    
    private TableRule createDynamicSubTableRule() {
        return TableRule.builder("subLogicTable").dynamic(true).dataSourceRule(createDataSourceRule()).build();
    }
}
