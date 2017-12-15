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

package io.shardingjdbc.core.rule;

import io.shardingjdbc.core.api.config.TableRuleConfiguration;
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
    
    @Test
    public void assertGetAllLogicTables() {
        assertThat(createBindingTableRule().getAllLogicTables(), is((Collection<String>) Arrays.asList("logicTable", "subLogicTable")));
    }
    
    @Test
    public void assertGetTableRules() {
        assertThat(createBindingTableRule().getTableRules().size(), is(2));
        assertThat(createBindingTableRule().getTableRules().get(0).getLogicTable(), is(createTableRule().getLogicTable()));
        assertThat(createBindingTableRule().getTableRules().get(0).getActualDataNodes(), is(createTableRule().getActualDataNodes()));
        assertThat(createBindingTableRule().getTableRules().get(1).getLogicTable(), is(createSubTableRule().getLogicTable()));
        assertThat(createBindingTableRule().getTableRules().get(1).getActualDataNodes(), is(createSubTableRule().getActualDataNodes()));
    }
    
    private BindingTableRule createBindingTableRule() {
        return new BindingTableRule(Arrays.asList(createTableRule(), createSubTableRule()));
    }
    
    private TableRule createTableRule() {
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
        tableRuleConfig.setLogicTable("logicTable");
        tableRuleConfig.setActualDataNodes("ds1.table_0, ds1.table_1, ds2.table_0, ds2.table_1");
        return tableRuleConfig.build(createDataSourceMap());
    }
    
    private TableRule createSubTableRule() {
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
        tableRuleConfig.setLogicTable("subLogicTable");
        tableRuleConfig.setActualDataNodes("ds1.sub_table_0, ds1.sub_table_1, ds2.sub_table_0, ds2.sub_table_1");
        return tableRuleConfig.build(createDataSourceMap());
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>(2, 1);
        result.put("ds1", null);
        result.put("ds2", null);
        return result;
    }
}
