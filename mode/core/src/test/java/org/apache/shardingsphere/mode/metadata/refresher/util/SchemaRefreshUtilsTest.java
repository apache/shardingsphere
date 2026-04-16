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

package org.apache.shardingsphere.mode.metadata.refresher.util;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class SchemaRefreshUtilsTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertGetSchemaNameWithSchemaFromContext() {
        assertThat(SchemaRefreshUtils.getSchemaName(createDatabase(), createSQLStatementContextWithSchema("Foo_Schema")), is("foo_schema"));
    }
    
    @Test
    void assertGetSchemaNameWithDefaultSchema() {
        assertThat(SchemaRefreshUtils.getSchemaName(createDatabase(), createSQLStatementContextWithoutSchema()), is("foo_db"));
    }
    
    @Test
    void assertGetActualSchemaNameWithSensitiveProps() {
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", databaseType, new ResourceMetaData(Collections.emptyMap()),
                new RuleMetaData(Collections.emptyList()), Collections.singletonList(new ShardingSphereSchema("Foo_Schema", databaseType)), new ConfigurationProperties(new Properties()));
        Properties props = new Properties();
        props.setProperty("metadata-identifier-case-sensitivity", "SENSITIVE");
        assertThat(SchemaRefreshUtils.getActualSchemaName(database, new IdentifierValue("Foo_Schema", QuoteCharacter.QUOTE), new ConfigurationProperties(props)), is("Foo_Schema"));
    }
    
    @Test
    void assertGetActualSchemaNameWithInsensitiveProps() {
        assertThat(SchemaRefreshUtils.getActualSchemaName(createDatabase(), new IdentifierValue("Foo_Schema"), new ConfigurationProperties(new Properties())), is("foo_schema"));
    }
    
    private ShardingSphereDatabase createDatabase() {
        return new ShardingSphereDatabase("FOO_DB", databaseType, new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), Collections.emptyList(),
                new ConfigurationProperties(new Properties()));
    }
    
    private SQLStatementContext createSQLStatementContextWithSchema(final String schemaName) {
        TableNameSegment tableNameSegment = new TableNameSegment(0, 0, new IdentifierValue("t_order"));
        tableNameSegment.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue(schemaName)));
        TablesContext tablesContext = new TablesContext(new SimpleTableSegment(tableNameSegment));
        SQLStatement sqlStatement = DeleteStatement.builder().databaseType(databaseType).build();
        return new FixtureSQLStatementContext(sqlStatement, tablesContext);
    }
    
    private SQLStatementContext createSQLStatementContextWithoutSchema() {
        TablesContext tablesContext = new TablesContext(Collections.emptyList());
        SQLStatement sqlStatement = DeleteStatement.builder().databaseType(databaseType).build();
        return new FixtureSQLStatementContext(sqlStatement, tablesContext);
    }
    
    private static final class FixtureSQLStatementContext implements SQLStatementContext {
        
        private final SQLStatement sqlStatement;
        
        private final TablesContext tablesContext;
        
        private FixtureSQLStatementContext(final SQLStatement sqlStatement, final TablesContext tablesContext) {
            this.sqlStatement = sqlStatement;
            this.tablesContext = tablesContext;
        }
        
        @Override
        public SQLStatement getSqlStatement() {
            return sqlStatement;
        }
        
        @Override
        public TablesContext getTablesContext() {
            return tablesContext;
        }
    }
}
