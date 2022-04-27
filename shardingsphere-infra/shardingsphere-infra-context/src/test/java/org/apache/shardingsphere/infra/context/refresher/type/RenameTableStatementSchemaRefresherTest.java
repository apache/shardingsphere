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

import com.google.common.eventbus.Subscribe;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.dialect.SQL92DatabaseType;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.FederationDatabaseMetaData;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.event.SchemaAlteredEvent;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.table.RenameTableDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.RenameTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class RenameTableStatementSchemaRefresherTest {
    
    @Test
    public void assertRefresh() throws SQLException {
        RenameTableLister listener = new RenameTableLister(2);
        ShardingSphereEventBus.getInstance().register(listener);
        new RenameTableStatementSchemaRefresher().refresh(createShardingSphereMetaData(), new FederationDatabaseMetaData("foo_database", Collections.emptyMap()),
                new HashMap<>(), Collections.singleton("foo_ds"), "foo_schema", createRenameTableStatement(), mock(ConfigurationProperties.class));
        assertThat(listener.getActualCount(), is(listener.getRenameCount()));
        ShardingSphereEventBus.getInstance().unregister(listener);
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
    
    private ShardingSphereMetaData createShardingSphereMetaData() {
        return new ShardingSphereMetaData("foo_database",
                mockShardingSphereResource(), new ShardingSphereRuleMetaData(new LinkedList<>(), new LinkedList<>()), Collections.singletonMap("foo_schema", mock(ShardingSphereSchema.class)));
    }
    
    private ShardingSphereResource mockShardingSphereResource() {
        ShardingSphereResource result = mock(ShardingSphereResource.class);
        when(result.getDataSources()).thenReturn(Collections.emptyMap());
        when(result.getDatabaseType()).thenReturn(new SQL92DatabaseType());
        return result;
    }
    
    @RequiredArgsConstructor
    @Getter
    private static final class RenameTableLister {
        
        private final int renameCount;
        
        private int actualCount = -1;
        
        @Subscribe
        public void process(final Object message) {
            if (message instanceof SchemaAlteredEvent) {
                actualCount = ((SchemaAlteredEvent) message).getAlteredTables().size();
            }
        }
    }
}
