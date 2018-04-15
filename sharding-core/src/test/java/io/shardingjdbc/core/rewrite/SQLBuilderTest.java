/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.rewrite;

import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.core.api.config.TableRuleConfiguration;
import io.shardingjdbc.core.exception.ShardingJdbcException;
import io.shardingjdbc.core.rewrite.placeholder.IndexPlaceholder;
import io.shardingjdbc.core.rewrite.placeholder.SchemaPlaceholder;
import io.shardingjdbc.core.rewrite.placeholder.TablePlaceholder;
import io.shardingjdbc.core.rule.ShardingRule;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class SQLBuilderTest {
    
    @Test
    public void assertAppendLiteralsOnly() {
        SQLBuilder sqlBuilder = new SQLBuilder();
        sqlBuilder.appendLiterals("SELECT ");
        sqlBuilder.appendLiterals("table_x");
        sqlBuilder.appendLiterals(".id");
        sqlBuilder.appendLiterals(" FROM ");
        sqlBuilder.appendLiterals("table_x");
        assertThat(sqlBuilder.toSQL(Collections.<String, String>emptyMap(), null), is("SELECT table_x.id FROM table_x"));
    }
    
    @Test
    public void assertAppendTableWithoutTableToken() {
        SQLBuilder sqlBuilder = new SQLBuilder();
        sqlBuilder.appendLiterals("SELECT ");
        sqlBuilder.appendPlaceholder(new TablePlaceholder("table_x"));
        sqlBuilder.appendLiterals(".id");
        sqlBuilder.appendLiterals(" FROM ");
        sqlBuilder.appendPlaceholder(new TablePlaceholder("table_x"));
        assertThat(sqlBuilder.toSQL(Collections.<String, String>emptyMap(), null), is("SELECT table_x.id FROM table_x"));
    }
    
    @Test
    public void assertAppendTableWithTableToken() {
        SQLBuilder sqlBuilder = new SQLBuilder();
        sqlBuilder.appendLiterals("SELECT ");
        sqlBuilder.appendPlaceholder(new TablePlaceholder("table_x"));
        sqlBuilder.appendLiterals(".id");
        sqlBuilder.appendLiterals(" FROM ");
        sqlBuilder.appendPlaceholder(new TablePlaceholder("table_x"));
        Map<String, String> tableTokens = new HashMap<>(1, 1);
        tableTokens.put("table_x", "table_x_1");
        assertThat(sqlBuilder.toSQL(tableTokens, null), is("SELECT table_x_1.id FROM table_x_1"));
    }
    
    @Test
    public void assertIndexPlaceholderAppendTableWithoutTableToken() {
        SQLBuilder sqlBuilder = new SQLBuilder();
        sqlBuilder.appendLiterals("CREATE INDEX ");
        sqlBuilder.appendPlaceholder(new IndexPlaceholder("index_name", "table_x"));
        sqlBuilder.appendLiterals(" ON ");
        sqlBuilder.appendPlaceholder(new TablePlaceholder("table_x"));
        sqlBuilder.appendLiterals(" ('column')");
        assertThat(sqlBuilder.toSQL(Collections.<String, String>emptyMap(), null), is("CREATE INDEX index_name ON table_x ('column')"));
    }
    
    @Test
    public void assertIndexPlaceholderAppendTableWithTableToken() {
        SQLBuilder sqlBuilder = new SQLBuilder();
        sqlBuilder.appendLiterals("CREATE INDEX ");
        sqlBuilder.appendPlaceholder(new IndexPlaceholder("index_name", "table_x"));
        sqlBuilder.appendLiterals(" ON ");
        sqlBuilder.appendPlaceholder(new TablePlaceholder("table_x"));
        sqlBuilder.appendLiterals(" ('column')");
        Map<String, String> tableTokens = new HashMap<>(1, 1);
        tableTokens.put("table_x", "table_x_1");
        assertThat(sqlBuilder.toSQL(tableTokens, null), is("CREATE INDEX index_name_table_x_1 ON table_x_1 ('column')"));
    }
    
    @Test(expected = ShardingJdbcException.class)
    public void assertSchemaPlaceholderAppendTableWithoutTableToken() {
        SQLBuilder sqlBuilder = new SQLBuilder();
        sqlBuilder.appendLiterals("SHOW ");
        sqlBuilder.appendLiterals("CREATE TABLE ");
        sqlBuilder.appendPlaceholder(new TablePlaceholder("table_x"));
        sqlBuilder.appendLiterals("ON ");
        sqlBuilder.appendPlaceholder(new SchemaPlaceholder("dx", "table_x"));
        sqlBuilder.toSQL(Collections.<String, String>emptyMap(), createShardingRule());
    }
    
    @Test
    public void assertSchemaPlaceholderAppendTableWithTableToken() {
        SQLBuilder sqlBuilder = new SQLBuilder();
        sqlBuilder.appendLiterals("SHOW ");
        sqlBuilder.appendLiterals("CREATE TABLE ");
        sqlBuilder.appendPlaceholder(new TablePlaceholder("table_0"));
        sqlBuilder.appendLiterals(" ON ");
        sqlBuilder.appendPlaceholder(new SchemaPlaceholder("ds", "table_0"));
        Map<String, String> tableTokens = new HashMap<>(1, 1);
        tableTokens.put("table_0", "table_1");
        assertThat(sqlBuilder.toSQL(tableTokens, createShardingRule()), is("SHOW CREATE TABLE table_1 ON ds0"));
    }
    
    @Test
    public void assertShardingPlaceholderToString() {
        assertThat(new IndexPlaceholder("index_name", "table_x").toString(), is("index_name"));
        assertThat(new SchemaPlaceholder("schema_name", "table_x").toString(), is("schema_name"));
        assertThat(new TablePlaceholder("table_name").toString(), is("table_name"));
    }
    
    private ShardingRule createShardingRule() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = createTableRuleConfig();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        return new ShardingRule(shardingRuleConfig, createDataSourceNames());
    }
    
    private Collection<String> createDataSourceNames() {
        return Arrays.asList("ds0", "ds1");
    }
    
    private TableRuleConfiguration createTableRuleConfig() {
        TableRuleConfiguration result = new TableRuleConfiguration();
        result.setLogicTable("LOGIC_TABLE");
        result.setActualDataNodes("ds${0..1}.table_${0..2}");
        return result;
    }
}
