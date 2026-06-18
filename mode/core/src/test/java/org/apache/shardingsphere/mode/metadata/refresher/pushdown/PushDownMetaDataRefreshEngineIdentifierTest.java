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

package org.apache.shardingsphere.mode.metadata.refresher.pushdown;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.type.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.DropTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

class PushDownMetaDataRefreshEngineIdentifierTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertRefreshUsesActualSchemaAndTableNames() throws SQLException {
        PushDownMetaDataManagerPersistServiceFixture persistService = new PushDownMetaDataManagerPersistServiceFixture();
        PushDownMetaDataRefreshEngine engine = new PushDownMetaDataRefreshEngine(new CommonSQLStatementContext(createDropTableStatement()));
        engine.refresh(persistService, createDatabase(), new ConfigurationProperties(new Properties()), Collections.emptyList());
        assertThat(persistService.getDroppedTableSchemaName(), is("Foo_Schema"));
        assertThat(persistService.getDroppedTableNames(), contains("Foo_Tbl"));
    }
    
    private DropTableStatement createDropTableStatement() {
        TableNameSegment tableNameSegment = new TableNameSegment(0, 0, new IdentifierValue("foo_tbl"));
        tableNameSegment.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        return new DropTableStatement(databaseType, Collections.singletonList(new SimpleTableSegment(tableNameSegment)), false, false);
    }
    
    private ShardingSphereDatabase createDatabase() {
        ShardingSphereSchema schema = new ShardingSphereSchema("Foo_Schema", databaseType,
                Collections.singletonList(new ShardingSphereTable("Foo_Tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList())),
                Collections.emptyList());
        return new ShardingSphereDatabase("foo_db", databaseType, new ResourceMetaData(Collections.emptyMap()),
                new RuleMetaData(Collections.emptyList()), Collections.singletonList(schema), new ConfigurationProperties(new Properties()));
    }
}
