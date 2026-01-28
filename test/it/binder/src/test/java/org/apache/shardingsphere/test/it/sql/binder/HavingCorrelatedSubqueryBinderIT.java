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

package org.apache.shardingsphere.test.it.sql.binder;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sql.parser.engine.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.engine.api.SQLStatementVisitorEngine;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

class HavingCorrelatedSubqueryBinderIT {
    
    @Test
    void assertBindHavingWithCorrelatedSubqueryReferencingOuterColumnPostgreSQL() {
        String sql = "SELECT ref_0.user_id AS c_0 FROM t_order AS ref_0 GROUP BY ref_0.user_id "
                + "HAVING 'a' = (SELECT ref_2.status AS c_0 FROM t_order_item AS ref_2 WHERE ref_0.user_id = ref_2.user_id LIMIT 1)";
        assertDoesNotThrow(() -> bindSQL("PostgreSQL", sql));
    }
    
    @Test
    void assertBindHavingWithCorrelatedSubqueryUsingBetweenPostgreSQL() {
        String sql = "SELECT ref_0.user_id AS c_0 FROM t_order AS ref_0 GROUP BY ref_0.user_id "
                + "HAVING 'a' = (SELECT ref_2.status AS c_0 FROM t_order_item AS ref_2 WHERE ref_0.user_id BETWEEN ref_2.user_id AND ref_2.user_id LIMIT 1)";
        assertDoesNotThrow(() -> bindSQL("PostgreSQL", sql));
    }
    
    @Test
    void assertBindHavingWithCorrelatedSubqueryReferencingOuterColumnMySQL() {
        String sql = "SELECT ref_0.user_id AS c_0 FROM t_order AS ref_0 GROUP BY ref_0.user_id "
                + "HAVING 'a' = (SELECT ref_2.status AS c_0 FROM t_order_item AS ref_2 WHERE ref_0.user_id = ref_2.user_id LIMIT 1)";
        assertDoesNotThrow(() -> bindSQL("MySQL", sql));
    }
    
    @Test
    void assertBindHavingWithCorrelatedSubqueryReferencingOuterColumnOpenGauss() {
        String sql = "SELECT ref_0.user_id AS c_0 FROM t_order AS ref_0 GROUP BY ref_0.user_id "
                + "HAVING 'a' = (SELECT ref_2.status AS c_0 FROM t_order_item AS ref_2 WHERE ref_0.user_id = ref_2.user_id LIMIT 1)";
        assertDoesNotThrow(() -> bindSQL("openGauss", sql));
    }
    
    private SQLStatementContext bindSQL(final String databaseTypeName, final String sql) {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, databaseTypeName);
        SQLStatement sqlStatement = new SQLStatementVisitorEngine(databaseTypeName).visit(new SQLParserEngine(databaseTypeName, new CacheOption(128, 1024L)).parse(sql, false));
        SQLStatementContext result = new SQLBindEngine(mockMetaData(databaseType), "foo_db", new HintValueContext()).bind(sqlStatement);
        assertThat(result, notNullValue());
        return result;
    }
    
    private ShardingSphereMetaData mockMetaData(final DatabaseType databaseType) {
        Collection<ShardingSphereDatabase> databases = new LinkedList<>();
        databases.add(new ShardingSphereDatabase("foo_db", databaseType, mock(ResourceMetaData.class), mock(RuleMetaData.class), mockSchemas(databaseType)));
        return new ShardingSphereMetaData(databases, mock(ResourceMetaData.class), mock(RuleMetaData.class), new ConfigurationProperties(new Properties()));
    }
    
    private Collection<ShardingSphereSchema> mockSchemas(final DatabaseType databaseType) {
        Collection<ShardingSphereSchema> result = new LinkedList<>();
        String defaultSchemaName = new DatabaseTypeRegistry(databaseType).getDefaultSchemaName("foo_db");
        result.add(new ShardingSphereSchema(defaultSchemaName, mockTables(), Collections.emptyList(), databaseType));
        return result;
    }
    
    private Collection<ShardingSphereTable> mockTables() {
        Collection<ShardingSphereTable> result = new LinkedList<>();
        result.add(new ShardingSphereTable("t_order", Arrays.asList(
                new ShardingSphereColumn("order_id", Types.BIGINT, true, false, false, true, false, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("status", Types.VARCHAR, false, false, false, true, false, false)),
                Collections.emptyList(), Collections.emptyList()));
        result.add(new ShardingSphereTable("t_order_item", Arrays.asList(
                new ShardingSphereColumn("item_id", Types.BIGINT, true, false, false, true, false, false),
                new ShardingSphereColumn("order_id", Types.BIGINT, false, false, false, true, false, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("status", Types.VARCHAR, false, false, false, true, false, false)),
                Collections.emptyList(), Collections.emptyList()));
        return result;
    }
}
