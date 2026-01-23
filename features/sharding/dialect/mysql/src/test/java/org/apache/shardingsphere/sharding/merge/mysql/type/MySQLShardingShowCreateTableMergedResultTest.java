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

package org.apache.shardingsphere.sharding.merge.mysql.type;

import org.apache.groovy.util.Maps;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereConstraint;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MySQLShardingShowCreateTableMergedResultTest {
    
    private ShardingRule rule;
    
    private ShardingSphereSchema schema;
    
    @BeforeEach
    void setUp() {
        rule = buildShardingRule();
        schema = createSchema();
    }
    
    private ShardingRule buildShardingRule() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(new ShardingTableRuleConfiguration("foo_tbl", "ds.foo_tbl_${0..2}"));
        shardingRuleConfig.getTables().add(new ShardingTableRuleConfiguration("bar_tbl", "ds.bar_tbl_${0..2}"));
        return new ShardingRule(shardingRuleConfig, Maps.of("ds", new MockedDataSource()), mock(ComputeNodeInstanceContext.class), Collections.emptyList());
    }
    
    private ShardingSphereSchema createSchema() {
        Collection<ShardingSphereTable> tables = new LinkedList<>();
        tables.add(new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.singleton(new ShardingSphereConstraint("foo_tbl_foreign_key", "bar_tbl"))));
        tables.add(new ShardingSphereTable("bar_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
        return new ShardingSphereSchema("foo_db", tables, Collections.emptyList(), TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
    }
    
    @Test
    void assertNextForEmptyQueryResult() throws SQLException {
        assertFalse(new MySQLShardingShowCreateTableMergedResult(rule, mock(SQLStatementContext.class), schema, Collections.emptyList()).next());
    }
    
    @Test
    void assertNextWithTableRule() throws SQLException {
        assertTrue(new MySQLShardingShowCreateTableMergedResult(rule, mock(SQLStatementContext.class), schema, Collections.singletonList(mockQueryResultWithTableRule())).next());
    }
    
    @Test
    void assertGetValueWithTableRule() throws SQLException {
        MySQLShardingShowCreateTableMergedResult actual = new MySQLShardingShowCreateTableMergedResult(
                rule, mock(SQLStatementContext.class), schema, Collections.singletonList(mockQueryResultWithTableRule()));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, String.class), is("foo_tbl"));
        assertThat(actual.getValue(2, String.class), is("CREATE TABLE `foo_tbl` (\n"
                + "  `id` int(11) NOT NULL AUTO_INCREMENT,\n"
                + "  `foo_id` int(11) NOT NULL COMMENT,\n"
                + "  `bar_id` int(11) NOT NULL COMMENT,\n"
                + "  `status` tinyint(4) NOT NULL DEFAULT '1',\n"
                + "  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,\n"
                + "  PRIMARY KEY (`id`),\n"
                + "  CONSTRAINT `foo_tbl_foreign_key` FOREIGN KEY (`bar_id`) REFERENCES `bar_tbl` (`bar_id`) \n"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin"));
    }
    
    private QueryResult mockQueryResultWithTableRule() throws SQLException {
        QueryResult result = mock(QueryResult.class, RETURNS_DEEP_STUBS);
        when(result.getMetaData().getColumnCount()).thenReturn(2);
        when(result.next()).thenReturn(true, false);
        when(result.getValue(1, Object.class)).thenReturn("foo_tbl_0");
        when(result.getValue(2, Object.class)).thenReturn("CREATE TABLE `foo_tbl_0` (\n"
                + "  `id` int(11) NOT NULL AUTO_INCREMENT,\n"
                + "  `foo_id` int(11) NOT NULL COMMENT,\n"
                + "  `bar_id` int(11) NOT NULL COMMENT,\n"
                + "  `status` tinyint(4) NOT NULL DEFAULT '1',\n"
                + "  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,\n"
                + "  PRIMARY KEY (`id`),\n"
                + "  CONSTRAINT `foo_tbl_foreign_key_foo_tbl_0` FOREIGN KEY (`bar_id`) REFERENCES `bar_tbl_0` (`bar_id`) \n"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin");
        return result;
    }
    
    @Test
    void assertGetValueWithoutTableRule() throws SQLException {
        MySQLShardingShowCreateTableMergedResult actual = new MySQLShardingShowCreateTableMergedResult(
                mock(ShardingRule.class, RETURNS_DEEP_STUBS), mock(SQLStatementContext.class), schema, Collections.singletonList(mockQueryResultWithoutTableRule()));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, String.class), is("foo_tbl"));
        assertThat(actual.getValue(2, String.class), is("CREATE TABLE `foo_tbl` (\n"
                + "  `id` int(11) NOT NULL AUTO_INCREMENT,\n"
                + "  `foo_id` int(11) NOT NULL COMMENT,\n"
                + "  `bar_id` int(11) NOT NULL COMMENT,\n"
                + "  `status` tinyint(4) NOT NULL DEFAULT '1',\n"
                + "  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,\n"
                + "  PRIMARY KEY (`id`),\n"
                + "  CONSTRAINT `foo_tbl_foreign_key` FOREIGN KEY (`bar_id`) REFERENCES `bar_tbl` (`bar_id`) \n"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin"));
    }
    
    private QueryResult mockQueryResultWithoutTableRule() throws SQLException {
        QueryResult result = mock(QueryResult.class, RETURNS_DEEP_STUBS);
        when(result.getMetaData().getColumnCount()).thenReturn(2);
        when(result.next()).thenReturn(true, false);
        when(result.getValue(1, Object.class)).thenReturn("foo_tbl");
        when(result.getValue(2, Object.class)).thenReturn("CREATE TABLE `foo_tbl` (\n"
                + "  `id` int(11) NOT NULL AUTO_INCREMENT,\n"
                + "  `foo_id` int(11) NOT NULL COMMENT,\n"
                + "  `bar_id` int(11) NOT NULL COMMENT,\n"
                + "  `status` tinyint(4) NOT NULL DEFAULT '1',\n"
                + "  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,\n"
                + "  PRIMARY KEY (`id`),\n"
                + "  CONSTRAINT `foo_tbl_foreign_key` FOREIGN KEY (`bar_id`) REFERENCES `bar_tbl` (`bar_id`) \n"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin");
        return result;
    }
}
