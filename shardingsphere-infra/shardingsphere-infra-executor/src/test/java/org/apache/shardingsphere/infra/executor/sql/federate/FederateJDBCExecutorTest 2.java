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

package org.apache.shardingsphere.infra.executor.sql.federate;

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.optimize.ShardingSphereOptimizer;
import org.apache.shardingsphere.infra.optimize.context.OptimizerContextFactory;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.Before;
import org.junit.Test;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class FederateJDBCExecutorTest {
    
    private static final String SELECT_SQL_BY_ID_ACROSS_SINGLE_AND_SHARDING_TABLES =
        "SELECT t_order_federate.order_id, t_order_federate.user_id, t_user_info.information "
            + "FROM t_order_federate , t_user_info "
            + "WHERE t_order_federate.user_id = t_user_info.user_id";
    
    private final String schemaName = "federate_jdbc";
    
    private ShardingSphereOptimizer optimizer;
    
    @Before
    public void init() throws Exception {
        Map<String, TableMetaData> tableMetaDataMap = new HashMap<>(2, 1);
        tableMetaDataMap.put("t_order_federate", createOrderTableMetaData());
        tableMetaDataMap.put("t_user_info", createUserInfoTableMetaData());
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(schemaName, mockResource(), null, new ShardingSphereSchema(tableMetaDataMap));
        optimizer = new ShardingSphereOptimizer(OptimizerContextFactory.create(Collections.singletonMap(schemaName, metaData)));
    }
    
    private ShardingSphereResource mockResource() {
        ShardingSphereResource result = mock(ShardingSphereResource.class);
        when(result.getDatabaseType()).thenReturn(new H2DatabaseType());
        return result;
    }
    
    private TableMetaData createOrderTableMetaData() {
        ColumnMetaData orderIdColumn = new ColumnMetaData("order_id", Types.VARCHAR, true, false, false);
        ColumnMetaData userIdColumn = new ColumnMetaData("user_id", Types.VARCHAR, false, false, false);
        ColumnMetaData statusColumn = new ColumnMetaData("status", Types.VARCHAR, false, false, false);
        return new TableMetaData("t_order_federate", Arrays.asList(orderIdColumn, userIdColumn, statusColumn), Collections.emptyList());
    }
    
    private TableMetaData createUserInfoTableMetaData() {
        ColumnMetaData userIdColumn = new ColumnMetaData("user_id", Types.VARCHAR, true, false, false);
        ColumnMetaData informationColumn = new ColumnMetaData("information", Types.VARCHAR, false, false, false);
        return new TableMetaData("t_user_info", Arrays.asList(userIdColumn, informationColumn), Collections.emptyList());
    }
    
    @Test
    public void assertSimpleSelect() {
        ShardingSphereSQLParserEngine sqlParserEngine = new ShardingSphereSQLParserEngine(
                DatabaseTypeRegistry.getTrunkDatabaseTypeName(new H2DatabaseType()), new ConfigurationProperties(new Properties()));
        SQLStatement sqlStatement = sqlParserEngine.parse(SELECT_SQL_BY_ID_ACROSS_SINGLE_AND_SHARDING_TABLES, false);
        String actual = optimizer.optimize(schemaName, sqlStatement).explain();
        String expected = "EnumerableCalc(expr#0..4=[{inputs}],expr#5=[CAST($t1):VARCHAR],expr#6=[CAST($t3):VARCHAR],expr#7=[=($t5,$t6)],proj#0..1=[{exprs}],information=[$t4],$condition=[$t7])"
                + "  EnumerableNestedLoopJoin(condition=[true],joinType=[inner])"
                + "    EnumerableTableScan(table=[[federate_jdbc,t_order_federate]])"
                + "    EnumerableTableScan(table=[[federate_jdbc,t_user_info]])";
        assertThat(actual.replaceAll("\\s*", ""), is(expected.replaceAll("\\s*", "")));
    }
}
