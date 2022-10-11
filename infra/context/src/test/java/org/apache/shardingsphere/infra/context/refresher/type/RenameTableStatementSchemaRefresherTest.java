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

package org.apache.shardingsphere.infra.context.refresher.type;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.database.type.dialect.SQL92DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResources;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.event.MetaDataRefreshedEvent;
import org.apache.shardingsphere.infra.metadata.database.schema.event.SchemaAlteredEvent;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.table.RenameTableDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.RenameTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class RenameTableStatementSchemaRefresherTest {
    
    @Test
    public void assertRefresh() throws SQLException {
        ShardingSphereDatabase actual = createDatabaseMetaData();
        Optional<MetaDataRefreshedEvent> event = new RenameTableStatementSchemaRefresher().refresh(
                actual, Collections.singleton("foo_ds"), "foo_schema", createRenameTableStatement(), new ConfigurationProperties(new Properties()));
        assertTrue(event.isPresent());
        assertThat(((SchemaAlteredEvent) event.get()).getDatabaseName(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(((SchemaAlteredEvent) event.get()).getSchemaName(), is("foo_schema"));
        assertThat(((SchemaAlteredEvent) event.get()).getDroppedTables(), is(Arrays.asList("tbl_0", "tbl_1")));
    }
    
    private RenameTableStatement createRenameTableStatement() {
        RenameTableStatement result = mock(RenameTableStatement.class);
        when(result.getRenameTables()).thenReturn(
                Arrays.asList(createRenameTableDefinitionSegment("tbl_0", "new_tbl_0"), createRenameTableDefinitionSegment("tbl_1", "new_tbl_1")));
        return result;
    }
    
    private RenameTableDefinitionSegment createRenameTableDefinitionSegment(final String originTableName, final String newTableName) {
        RenameTableDefinitionSegment result = new RenameTableDefinitionSegment(0, 1);
        result.setTable(new SimpleTableSegment(new TableNameSegment(0, 1, new IdentifierValue(originTableName))));
        result.setRenameTable(new SimpleTableSegment(new TableNameSegment(0, 1, new IdentifierValue(newTableName))));
        return result;
    }
    
    private ShardingSphereDatabase createDatabaseMetaData() {
        return new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME, new SQL92DatabaseType(),
                mockShardingSphereResource(), new ShardingSphereRuleMetaData(new LinkedList<>()), Collections.singletonMap("foo_schema", mock(ShardingSphereSchema.class)));
    }
    
    private ShardingSphereResources mockShardingSphereResource() {
        ShardingSphereResources result = mock(ShardingSphereResources.class);
        when(result.getDataSources()).thenReturn(Collections.singletonMap(DefaultDatabase.LOGIC_NAME, new MockedDataSource()));
        when(result.getDatabaseType()).thenReturn(new SQL92DatabaseType());
        return result;
    }
}
