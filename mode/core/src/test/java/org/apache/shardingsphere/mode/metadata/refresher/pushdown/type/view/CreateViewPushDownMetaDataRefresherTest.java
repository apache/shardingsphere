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

package org.apache.shardingsphere.mode.metadata.refresher.pushdown.type.view;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.refresher.pushdown.PushDownMetaDataManagerPersistServiceFixture;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class CreateViewPushDownMetaDataRefresherTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertRefreshCreateViewUsesLoadedActualViewTable() throws SQLException {
        AtomicReference<String> loadedViewName = new AtomicReference<>();
        PushDownMetaDataManagerPersistServiceFixture persistService = new PushDownMetaDataManagerPersistServiceFixture();
        CreateViewPushDownMetaDataRefresher refresher = new CreateViewPushDownMetaDataRefresher((database, logicDataSourceName, schemaName, viewName, props) -> {
            loadedViewName.set(viewName);
            return new ShardingSphereTable("Foo_View", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        });
        CreateViewStatement sqlStatement = new CreateViewStatement(databaseType);
        sqlStatement.setView(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_view"))));
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", databaseType,
                new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), Collections.emptyList());
        refresher.refresh(persistService, database, "logic_ds", "Foo_Schema", databaseType, sqlStatement, new ConfigurationProperties(new Properties()));
        assertThat(loadedViewName.get(), is("foo_view"));
        assertThat(persistService.getAlteredTableSchemaName(), is("Foo_Schema"));
        assertThat(persistService.getAlteredTables().iterator().next().getName(), is("Foo_View"));
    }
}
