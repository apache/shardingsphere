/**
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

package com.dangdang.ddframe.rdb.sharding.config.common.internal;

import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import com.dangdang.ddframe.rdb.sharding.api.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.exception.SQLParserException;
import com.dangdang.ddframe.rdb.sharding.router.SQLRouteEngine;
import com.dangdang.ddframe.rdb.sharding.router.SQLRouteResult;
import com.google.common.collect.Lists;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class TableRuleTest extends AbstractConfigTest {
    
    @Test
    public void testArgument() {
        Map<String, TableRule> tableRuleMap = getTableRule("argument");
        assertThat(tableRuleMap.size(), is(5));
    }
    
    @Test
    public void testActualTableName() {
        Map<String, TableRule> tableRuleMap = getTableRule("actual_table_name");
        assertThat(tableRuleMap.get("order_1").getActualTables().toString(), is("[DataNode(dataSourceName=db0, tableName=t_order_1), DataNode(dataSourceName=db0, tableName=t_order_3), DataNode(dataSourceName=db1, tableName=t_order_2), DataNode(dataSourceName=db1, tableName=t_order_4)]"));
        assertThat(tableRuleMap.get("order_2").getActualTables().toString(), is("[DataNode(dataSourceName=db0, tableName=t_order_1), DataNode(dataSourceName=db1, tableName=t_order_1), DataNode(dataSourceName=db0, tableName=t_order_2), DataNode(dataSourceName=db1, tableName=t_order_2), DataNode(dataSourceName=db0, tableName=t_order_3), DataNode(dataSourceName=db1, tableName=t_order_3), DataNode(dataSourceName=db0, tableName=t_order_bak), DataNode(dataSourceName=db1, tableName=t_order_bak)]"));
        assertThat(tableRuleMap.get("order_3").getActualTables().toString(), is("[DataNode(dataSourceName=db0, tableName=table_1), DataNode(dataSourceName=db1, tableName=table_1), DataNode(dataSourceName=db0, tableName=table_1_bak), DataNode(dataSourceName=db1, tableName=table_1_bak), " +
                "DataNode(dataSourceName=db0, tableName=table_2), DataNode(dataSourceName=db1, tableName=table_2), DataNode(dataSourceName=db0, tableName=table_2_bak), DataNode(dataSourceName=db1, tableName=table_2_bak), " +
                "DataNode(dataSourceName=db0, tableName=table_3), DataNode(dataSourceName=db1, tableName=table_3), DataNode(dataSourceName=db0, tableName=table_3_bak), DataNode(dataSourceName=db1, tableName=table_3_bak)]"));
        assertThat(tableRuleMap.get("order_4").getActualTables().toString(), is("[DataNode(dataSourceName=db0, tableName=table_1), DataNode(dataSourceName=db1, tableName=table_1)]"));
    }
    
    @Test
    public void testLongValueAlgorithm() throws FileNotFoundException, SQLParserException {
        SQLRouteEngine engine = new SQLRouteEngine(getShardingRuleBuilder("algorithm").build(), DatabaseType.MySQL);
        
        SQLRouteResult result = engine.route("select * from order1 where order_id = 1", null);
        assertThat(result.getExecutionUnits().get(0).getSql(), is("SELECT * FROM t_order_1 WHERE order_id = 1"));
        result = engine.route("select * from order1 where order_id = ?", Lists.newArrayList((Object) 1));
        assertThat(result.getExecutionUnits().get(0).getSql(), is("SELECT * FROM t_order_1 WHERE order_id = ?"));
        
        result = engine.route("select * from order1 where order_id = '1'", null);
        assertThat(result.getExecutionUnits().get(0).getSql(), is("SELECT * FROM t_order_1 WHERE order_id = '1'"));
        result = engine.route("select * from order1 where order_id = ?", Lists.newArrayList((Object) "1"));
        assertThat(result.getExecutionUnits().get(0).getSql(), is("SELECT * FROM t_order_1 WHERE order_id = ?"));
        
        result = engine.route("select * from order1 where order_id = ?", Lists.newArrayList((Object) new java.sql.Date(12000111L)));
        assertThat(result.getExecutionUnits().get(0).getSql(), is("SELECT * FROM t_order_1 WHERE order_id = ?"));
    }
    
    @Test
    public void testDoubleValueAlgorithm() throws FileNotFoundException, SQLParserException {
        SQLRouteEngine engine = new SQLRouteEngine(getShardingRuleBuilder("algorithm").build(), DatabaseType.MySQL);
        
        SQLRouteResult result = engine.route("select * from order2 where order_id = 1.11", null);
        assertThat(result.getExecutionUnits().get(0).getSql(), is("SELECT * FROM t_order_1 WHERE order_id = 1.11"));
        result = engine.route("select * from order2 where order_id = ?", Lists.newArrayList((Object) 1.11));
        assertThat(result.getExecutionUnits().get(0).getSql(), is("SELECT * FROM t_order_1 WHERE order_id = ?"));
        
        result = engine.route("select * from order2 where order_id = '1.11'", null);
        assertThat(result.getExecutionUnits().get(0).getSql(), is("SELECT * FROM t_order_1 WHERE order_id = '1.11'"));
        result = engine.route("select * from order2 where order_id = ?", Lists.newArrayList((Object) "1.11"));
        assertThat(result.getExecutionUnits().get(0).getSql(), is("SELECT * FROM t_order_1 WHERE order_id = ?"));
        
        result = engine.route("select * from order2 where order_id = ?", Lists.newArrayList((Object) new java.sql.Date(12000111L)));
        assertThat(result.getExecutionUnits().get(0).getSql(), is("SELECT * FROM t_order_0 WHERE order_id = ?"));
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void testUnsupportedNumberValueAlgorithm() throws FileNotFoundException, SQLParserException {
        SQLRouteEngine engine = new SQLRouteEngine(getShardingRuleBuilder("algorithm").build(), DatabaseType.MySQL);
        engine.route("select * from order1 where order_id = ?", Lists.newArrayList((Object) true));
    }
    
    @Test
    public void testDateValueAlgorithm() throws FileNotFoundException, SQLParserException, ParseException {
        SQLRouteEngine engine = new SQLRouteEngine(getShardingRuleBuilder("algorithm").build(), DatabaseType.MySQL);
        
        SQLRouteResult result = engine.route("select * from order3 where date = ?", Lists.newArrayList((Object) new SimpleDateFormat("yyyyMMdd").parse("20151015")));
        assertThat(result.getExecutionUnits().get(0).getSql(), is("SELECT * FROM t_order_0 WHERE date = ?"));
        
        result = engine.route("select * from order3 where date = ?", Lists.newArrayList((Object) "20151115"));
        assertThat(result.getExecutionUnits().get(0).getSql(), is("SELECT * FROM t_order_1 WHERE date = ?"));
        
        result = engine.route("select * from order3 where date = ?", Lists.newArrayList((Object) new SimpleDateFormat("yyyyMMdd").parse("20151015").getTime()));
        assertThat(result.getExecutionUnits().get(0).getSql(), is("SELECT * FROM t_order_0 WHERE date = ?"));
        
    }
    
    @Test
    public void testDateValueToStringAlgorithm() throws FileNotFoundException, SQLParserException, ParseException {
        SQLRouteEngine engine = new SQLRouteEngine(getShardingRuleBuilder("algorithm").build(), DatabaseType.MySQL);
        
        SQLRouteResult result = engine.route("select * from order4 where date = ?", Lists.newArrayList((Object) new SimpleDateFormat("yyyyMMdd").parse("20151015")));
        assertThat(result.getExecutionUnits().get(0).getSql(), is("SELECT * FROM t_order_201510 WHERE date = ?"));
        
        result = engine.route("select * from order4 where date = ?", Lists.newArrayList((Object) new SimpleDateFormat("yyyyMMdd").parse("20151015").getTime()));
        assertThat(result.getExecutionUnits().get(0).getSql(), is("SELECT * FROM t_order_201510 WHERE date = ?"));
        
        result = engine.route("select * from order4 where date = ?", Lists.newArrayList((Object) "201511"));
        assertThat(result.getExecutionUnits().get(0).getSql(), is("SELECT * FROM t_order_201511 WHERE date = ?"));
    }
    
    @Test
    public void testDateValueToDateAlgorithm() throws FileNotFoundException, SQLParserException, ParseException {
        SQLRouteEngine engine = new SQLRouteEngine(getShardingRuleBuilder("algorithm").build(), DatabaseType.MySQL);
        
        SQLRouteResult result = engine.route("select * from order5 where date = ?", Lists.newArrayList((Object) new SimpleDateFormat("yyyyMMdd").parse("20151015")));
        assertThat(result.getExecutionUnits().get(0).getSql(), is("SELECT * FROM t_order_0 WHERE date = ?"));
        
        result = engine.route("select * from order5 where date = ?", Lists.newArrayList((Object) new SimpleDateFormat("yyyyMMdd").parse("20151015").getTime()));
        assertThat(result.getExecutionUnits().get(0).getSql(), is("SELECT * FROM t_order_0 WHERE date = ?"));
        
    }
    
    @Test
    public void testInValueAlgorithm() throws FileNotFoundException, SQLParserException {
        SQLRouteEngine engine = new SQLRouteEngine(getShardingRuleBuilder("algorithm").build(), DatabaseType.MySQL);
        
        SQLRouteResult result = engine.route("select * from order1 where order_id in (1,3)", null);
        assertThat(result.getExecutionUnits().get(0).getSql(), is("SELECT * FROM t_order_1 WHERE order_id IN (1, 3)"));
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void testBetweenValueAlgorithm() throws FileNotFoundException, SQLParserException {
        SQLRouteEngine engine = new SQLRouteEngine(getShardingRuleBuilder("algorithm").build(), DatabaseType.MySQL);
        engine.route("select * from order1 where order_id between 1 and 3", null);
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void testUnsupportedDateValueAlgorithm() throws FileNotFoundException, SQLParserException {
        SQLRouteEngine engine = new SQLRouteEngine(getShardingRuleBuilder("algorithm").build(), DatabaseType.MySQL);
        engine.route("select * from order3 where date = ?", Lists.newArrayList((Object) true));
    }
    
    @Test(expected = SQLParserException.class)
    public void testUnsupportedDateValueToDateAlgorithm() throws FileNotFoundException, SQLParserException, ParseException {
        SQLRouteEngine engine = new SQLRouteEngine(getShardingRuleBuilder("algorithm").build(), DatabaseType.MySQL);
        engine.route("select * from order4 where date = ?", Lists.newArrayList((Object) new SimpleDateFormat("yyyyMMdd").parse("20151215")));
    }
    
    @Test(expected = SQLParserException.class)
    public void testNullRouteResult() throws FileNotFoundException, SQLParserException, ParseException {
        SQLRouteEngine engine = new SQLRouteEngine(getShardingRuleBuilder("algorithm").build(), DatabaseType.MySQL);
        SQLRouteResult result = engine.route("select * from order6 where order_id = 1", null);
        assertThat(result.getExecutionUnits().get(0).getSql(), is("SELECT * FROM t_order_0 WHERE order_id = 1"));
    }
    
    @Test
    public void testDefaultStrategy() throws SQLParserException, FileNotFoundException {
        SQLRouteEngine engine = new SQLRouteEngine(getShardingRuleBuilder("defaultStrategy").build(), DatabaseType.MySQL);
        SQLRouteResult result = engine.route("select * from order1 where order_id = 1", null);
        assertThat(result.getExecutionUnits().get(0).getSql(), is("SELECT * FROM t_order_0 WHERE order_id = 1"));
    }
    
    @Test
    public void testBindingTable() {
        SQLRouteEngine engine = new SQLRouteEngine(getShardingRuleBuilder("binding_table").build(), DatabaseType.MySQL);
        SQLRouteResult result = engine.route("select * from t_order o ,t_order_item i where o.order_id = i.order_id and o.order_id = 11", null);
        assertThat(result.getExecutionUnits().get(0).getSql(), is("SELECT * FROM t_order_1 o, t_order_item_1 i WHERE o.order_id = i.order_id AND o.order_id = 11"));
    }
    
    @Test
    public void testReturnMultiResult() {
        SQLRouteEngine engine = new SQLRouteEngine(getShardingRuleBuilder("algorithm").build(), DatabaseType.MySQL);
        SQLRouteResult result = engine.route("select * from order7 o where o.order_id = 1", null);
        assertThat(result.getExecutionUnits().get(0).getSql(), is("SELECT * FROM t_order_0 o WHERE o.order_id = 1"));
        assertThat(result.getExecutionUnits().get(1).getSql(), is("SELECT * FROM t_order_1 o WHERE o.order_id = 1"));
    }
    
    @Override
    protected String packageName() {
        return "table_rule";
    }
    
    protected Map<String, TableRule> getTableRule(final String path) {
        Map<String, TableRule> tableRuleMap = new HashMap<>();
        for (TableRule each : getDelegate(path).getTableRules()) {
            tableRuleMap.put(each.getLogicTable(), each);
        }
        return tableRuleMap;
    }
}
