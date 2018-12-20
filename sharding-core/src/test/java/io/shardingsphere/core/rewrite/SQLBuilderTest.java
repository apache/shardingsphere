/*
 * Copyright 2016-2018 shardingsphere.io.
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

package io.shardingsphere.core.rewrite;

import io.shardingsphere.api.config.rule.ShardingRuleConfiguration;
import io.shardingsphere.api.config.rule.TableRuleConfiguration;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.metadata.datasource.ShardingDataSourceMetaData;
import io.shardingsphere.core.rewrite.placeholder.IndexPlaceholder;
import io.shardingsphere.core.rewrite.placeholder.SchemaPlaceholder;
import io.shardingsphere.core.rewrite.placeholder.TablePlaceholder;
import io.shardingsphere.core.rule.ShardingRule;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class SQLBuilderTest {
    
    private ShardingDataSourceMetaData shardingDataSourceMetaData;
    
    @Before
    public void setUp() {
        Map<String, String> shardingDataSourceURLs = new LinkedHashMap<>();
        shardingDataSourceURLs.put("ds0", "jdbc:mysql://127.0.0.1:3306/actual_db");
        shardingDataSourceURLs.put("ds1", "jdbc:mysql://127.0.0.1:3306/actual_db");
        shardingDataSourceMetaData = new ShardingDataSourceMetaData(shardingDataSourceURLs, createShardingRule(), DatabaseType.MySQL);
    }
    
    @Test
    public void assertAppendLiteralsOnly() {
        SQLBuilder sqlBuilder = new SQLBuilder();
        sqlBuilder.appendLiterals("SELECT ");
        sqlBuilder.appendLiterals("table_x");
        sqlBuilder.appendLiterals(".id");
        sqlBuilder.appendLiterals(" FROM ");
        sqlBuilder.appendLiterals("table_x");
        assertThat(sqlBuilder.toSQL(null, Collections.<String, String>emptyMap(), null, null).getSql(), is("SELECT table_x.id FROM table_x"));
    }
    
    @Test
    public void assertAppendTableWithoutTableToken() {
        SQLBuilder sqlBuilder = new SQLBuilder();
        sqlBuilder.appendLiterals("SELECT ");
        sqlBuilder.appendPlaceholder(new TablePlaceholder("table_x", "table_x"));
        sqlBuilder.appendLiterals(".id");
        sqlBuilder.appendLiterals(" FROM ");
        sqlBuilder.appendPlaceholder(new TablePlaceholder("table_x", "table_x"));
        assertThat(sqlBuilder.toSQL(null, Collections.<String, String>emptyMap(), null, null).getSql(), is("SELECT table_x.id FROM table_x"));
    }
    
    @Test
    public void assertAppendTableWithTableToken() {
        SQLBuilder sqlBuilder = new SQLBuilder();
        sqlBuilder.appendLiterals("SELECT ");
        sqlBuilder.appendPlaceholder(new TablePlaceholder("table_x", "table_x"));
        sqlBuilder.appendLiterals(".id");
        sqlBuilder.appendLiterals(" FROM ");
        sqlBuilder.appendPlaceholder(new TablePlaceholder("table_x", "table_x"));
        Map<String, String> tableTokens = new HashMap<>(1, 1);
        tableTokens.put("table_x", "table_x_1");
        assertThat(sqlBuilder.toSQL(null, tableTokens, null, null).getSql(), is("SELECT table_x_1.id FROM table_x_1"));
    }
    
    @Test
    public void assertIndexPlaceholderAppendTableWithoutTableToken() {
        SQLBuilder sqlBuilder = new SQLBuilder();
        sqlBuilder.appendLiterals("CREATE INDEX ");
        sqlBuilder.appendPlaceholder(new IndexPlaceholder("index_name", "index_name"));
        sqlBuilder.appendLiterals(" ON ");
        sqlBuilder.appendPlaceholder(new TablePlaceholder("table_x", "table_x"));
        sqlBuilder.appendLiterals(" ('column')");
        assertThat(sqlBuilder.toSQL(null, Collections.<String, String>emptyMap(), null, null).getSql(), is("CREATE INDEX index_name ON table_x ('column')"));
    }
    
    @Test
    public void assertIndexPlaceholderAppendTableWithTableToken() {
        SQLBuilder sqlBuilder = new SQLBuilder();
        sqlBuilder.appendLiterals("CREATE INDEX ");
        sqlBuilder.appendPlaceholder(new IndexPlaceholder("index_name", "table_x"));
        sqlBuilder.appendLiterals(" ON ");
        sqlBuilder.appendPlaceholder(new TablePlaceholder("table_x", "table_x"));
        sqlBuilder.appendLiterals(" ('column')");
        Map<String, String> tableTokens = new HashMap<>(1, 1);
        tableTokens.put("table_x", "table_x_1");
        assertThat(sqlBuilder.toSQL(null, tableTokens, null, null).getSql(), is("CREATE INDEX index_name_table_x_1 ON table_x_1 ('column')"));
    }
    
    @Test(expected = ShardingException.class)
    public void assertSchemaPlaceholderAppendTableWithoutTableToken() {
        SQLBuilder sqlBuilder = new SQLBuilder();
        sqlBuilder.appendLiterals("SHOW ");
        sqlBuilder.appendLiterals("CREATE TABLE ");
        sqlBuilder.appendPlaceholder(new TablePlaceholder("table_x", "table_x"));
        sqlBuilder.appendLiterals("ON ");
        sqlBuilder.appendPlaceholder(new SchemaPlaceholder("dx", "table_x"));
        sqlBuilder.toSQL(null, Collections.<String, String>emptyMap(), createShardingRule(), null);
    }
    
    @Test
    public void assertSchemaPlaceholderAppendTableWithTableToken() {
        SQLBuilder sqlBuilder = new SQLBuilder();
        sqlBuilder.appendLiterals("SHOW ");
        sqlBuilder.appendLiterals("CREATE TABLE ");
        sqlBuilder.appendPlaceholder(new TablePlaceholder("table_0", "table_0"));
        sqlBuilder.appendLiterals(" ON ");
        sqlBuilder.appendPlaceholder(new SchemaPlaceholder("ds0", "table_0"));
        Map<String, String> tableTokens = new HashMap<>(1, 1);
        tableTokens.put("table_0", "table_1");
//        ShardingDataSourceMetaData shardingDataSourceMetaData = Mockito.mock(ShardingDataSourceMetaData.class);
//        Mockito.when(shardingDataSourceMetaData.getActualSchemaName(Mockito.anyString())).thenReturn("actual_db");
        assertThat(sqlBuilder.toSQL(null, tableTokens, createShardingRule(), shardingDataSourceMetaData).getSql(), is("SHOW CREATE TABLE table_1 ON actual_db"));
    }
    
    @Test
    public void assertAppendTableWithoutTableTokenWithBackQuotes() {
        SQLBuilder sqlBuilder = new SQLBuilder();
        sqlBuilder.appendLiterals("SELECT ");
        sqlBuilder.appendPlaceholder(new TablePlaceholder("table_x", "`table_x`"));
        sqlBuilder.appendLiterals(".id");
        sqlBuilder.appendLiterals(" FROM ");
        sqlBuilder.appendPlaceholder(new TablePlaceholder("table_x", "`table_x`"));
        assertThat(sqlBuilder.toSQL(null, Collections.<String, String>emptyMap(), null, null).getSql(), is("SELECT `table_x`.id FROM `table_x`"));
    }
    
    @Test
    public void assertAppendTableWithTableTokenWithBackQuotes() {
        SQLBuilder sqlBuilder = new SQLBuilder();
        sqlBuilder.appendLiterals("SELECT ");
        sqlBuilder.appendPlaceholder(new TablePlaceholder("table_x", "`table_x`"));
        sqlBuilder.appendLiterals(".id");
        sqlBuilder.appendLiterals(" FROM ");
        sqlBuilder.appendPlaceholder(new TablePlaceholder("table_x", "`table_x`"));
        Map<String, String> tableTokens = new HashMap<>(1, 1);
        tableTokens.put("table_x", "table_x_1");
        assertThat(sqlBuilder.toSQL(null, tableTokens, null, null).getSql(), is("SELECT `table_x_1`.id FROM `table_x_1`"));
    }
    
    @Test
    public void assertIndexPlaceholderAppendTableWithoutTableTokenWithBackQuotes() {
        SQLBuilder sqlBuilder = new SQLBuilder();
        sqlBuilder.appendLiterals("CREATE INDEX ");
        sqlBuilder.appendPlaceholder(new IndexPlaceholder("index_name", "index_name"));
        sqlBuilder.appendLiterals(" ON ");
        sqlBuilder.appendPlaceholder(new TablePlaceholder("table_x", "`table_x`"));
        sqlBuilder.appendLiterals(" ('column')");
        assertThat(sqlBuilder.toSQL(null, Collections.<String, String>emptyMap(), null, null).getSql(), is("CREATE INDEX index_name ON `table_x` ('column')"));
    }
    
    @Test
    public void assertIndexPlaceholderAppendTableWithTableTokenWithBackQuotes() {
        SQLBuilder sqlBuilder = new SQLBuilder();
        sqlBuilder.appendLiterals("CREATE INDEX ");
        sqlBuilder.appendPlaceholder(new IndexPlaceholder("index_name", "table_x"));
        sqlBuilder.appendLiterals(" ON ");
        sqlBuilder.appendPlaceholder(new TablePlaceholder("table_x", "`table_x`"));
        sqlBuilder.appendLiterals(" ('column')");
        Map<String, String> tableTokens = new HashMap<>(1, 1);
        tableTokens.put("table_x", "table_x_1");
        assertThat(sqlBuilder.toSQL(null, tableTokens, null, null).getSql(), is("CREATE INDEX index_name_table_x_1 ON `table_x_1` ('column')"));
    }
    
    @Test
    public void assertSchemaPlaceholderAppendTableWithTableTokenWithBackQuotes() {
        SQLBuilder sqlBuilder = new SQLBuilder();
        sqlBuilder.appendLiterals("SHOW ");
        sqlBuilder.appendLiterals("CREATE TABLE ");
        sqlBuilder.appendPlaceholder(new TablePlaceholder("table_0", "`table_0`"));
        sqlBuilder.appendLiterals(" ON ");
        sqlBuilder.appendPlaceholder(new SchemaPlaceholder("ds", "table_0"));
        Map<String, String> tableTokens = new HashMap<>(1, 1);
        tableTokens.put("table_0", "table_1");
        assertThat(sqlBuilder.toSQL(null, tableTokens, createShardingRule(), shardingDataSourceMetaData).getSql(), is("SHOW CREATE TABLE `table_1` ON actual_db"));
    }
    
    @Test
    public void assertAppendTableWithoutTableTokenWithDoubleQuotes() {
        SQLBuilder sqlBuilder = new SQLBuilder();
        sqlBuilder.appendLiterals("SELECT ");
        sqlBuilder.appendPlaceholder(new TablePlaceholder("table_x", "\"table_x\""));
        sqlBuilder.appendLiterals(".id");
        sqlBuilder.appendLiterals(" FROM ");
        sqlBuilder.appendPlaceholder(new TablePlaceholder("table_x", "\"table_x\""));
        assertThat(sqlBuilder.toSQL(null, Collections.<String, String>emptyMap(), null, null).getSql(), is("SELECT \"table_x\".id FROM \"table_x\""));
    }
    
    @Test
    public void assertAppendTableWithTableTokenWithDoubleQuotes() {
        SQLBuilder sqlBuilder = new SQLBuilder();
        sqlBuilder.appendLiterals("SELECT ");
        sqlBuilder.appendPlaceholder(new TablePlaceholder("table_x", "\"table_x\""));
        sqlBuilder.appendLiterals(".id");
        sqlBuilder.appendLiterals(" FROM ");
        sqlBuilder.appendPlaceholder(new TablePlaceholder("table_x", "\"table_x\""));
        Map<String, String> tableTokens = new HashMap<>(1, 1);
        tableTokens.put("table_x", "table_x_1");
        assertThat(sqlBuilder.toSQL(null, tableTokens, null, null).getSql(), is("SELECT \"table_x_1\".id FROM \"table_x_1\""));
    }
    
    @Test
    public void assertIndexPlaceholderAppendTableWithoutTableTokenWithDoubleQuotes() {
        SQLBuilder sqlBuilder = new SQLBuilder();
        sqlBuilder.appendLiterals("CREATE INDEX ");
        sqlBuilder.appendPlaceholder(new IndexPlaceholder("index_name", "index_name"));
        sqlBuilder.appendLiterals(" ON ");
        sqlBuilder.appendPlaceholder(new TablePlaceholder("table_x", "\"table_x\""));
        sqlBuilder.appendLiterals(" ('column')");
        assertThat(sqlBuilder.toSQL(null, Collections.<String, String>emptyMap(), null, null).getSql(), is("CREATE INDEX index_name ON \"table_x\" ('column')"));
    }
    
    @Test
    public void assertIndexPlaceholderAppendTableWithTableTokenWithDoubleQuotes() {
        SQLBuilder sqlBuilder = new SQLBuilder();
        sqlBuilder.appendLiterals("CREATE INDEX ");
        sqlBuilder.appendPlaceholder(new IndexPlaceholder("index_name", "table_x"));
        sqlBuilder.appendLiterals(" ON ");
        sqlBuilder.appendPlaceholder(new TablePlaceholder("table_x", "\"table_x\""));
        sqlBuilder.appendLiterals(" ('column')");
        Map<String, String> tableTokens = new HashMap<>(1, 1);
        tableTokens.put("table_x", "table_x_1");
        assertThat(sqlBuilder.toSQL(null, tableTokens, null, null).getSql(), is("CREATE INDEX index_name_table_x_1 ON \"table_x_1\" ('column')"));
    }
    
    @Test
    public void assertSchemaPlaceholderAppendTableWithTableTokenWithDoubleQuotes() {
        SQLBuilder sqlBuilder = new SQLBuilder();
        sqlBuilder.appendLiterals("SHOW ");
        sqlBuilder.appendLiterals("CREATE TABLE ");
        sqlBuilder.appendPlaceholder(new TablePlaceholder("table_0", "\"table_0\""));
        sqlBuilder.appendLiterals(" ON ");
        sqlBuilder.appendPlaceholder(new SchemaPlaceholder("ds", "table_0"));
        Map<String, String> tableTokens = new HashMap<>(1, 1);
        tableTokens.put("table_0", "table_1");
        assertThat(sqlBuilder.toSQL(null, tableTokens, createShardingRule(), shardingDataSourceMetaData).getSql(), is("SHOW CREATE TABLE \"table_1\" ON actual_db"));
    }
    
    @Test
    public void assertShardingPlaceholderToString() {
        assertThat(new IndexPlaceholder("index_name", "table_x").toString(), is("index_name"));
        assertThat(new SchemaPlaceholder("schema_name", "table_x").toString(), is("schema_name"));
        assertThat(new TablePlaceholder("table_name", "`table_name`").toString(), is("table_name"));
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
