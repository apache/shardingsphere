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

package org.apache.shardingsphere.mode.metadata.refresher.federation.type;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.refresher.federation.FederationMetaDataRefresher;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.AlterViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlterViewFederationMetaDataRefresherTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private final AlterViewFederationMetaDataRefresher refresher = (AlterViewFederationMetaDataRefresher) TypedSPILoader.getService(FederationMetaDataRefresher.class, AlterViewStatement.class);
    
    private final String schemaName = "foo_schema";
    
    @Mock
    private MetaDataManagerPersistService metaDataManagerPersistService;
    
    @Mock
    private ShardingSphereDatabase database;
    
    private AlterViewStatement sqlStatement;
    
    @BeforeEach
    void setUp() {
        sqlStatement = new AlterViewStatement(databaseType);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertRefreshWithRenameView() {
        ShardingSphereSchema schema = new ShardingSphereSchema(schemaName, databaseType, Collections.emptyList(), Collections.singleton(new ShardingSphereView("foo_view", "SELECT * FROM foo_tbl")));
        when(database.getSchema(schemaName)).thenReturn(schema);
        sqlStatement.setView(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_view"))));
        sqlStatement.setRenameView(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("bar_view"))));
        refresher.refresh(metaDataManagerPersistService, databaseType, database, schemaName, sqlStatement);
        ArgumentCaptor<Collection<ShardingSphereView>> alteredViewsCaptor = ArgumentCaptor.forClass(Collection.class);
        ArgumentCaptor<Collection<String>> droppedViewsCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(metaDataManagerPersistService).alterViews(eq(database), eq(schemaName), alteredViewsCaptor.capture());
        verify(metaDataManagerPersistService).dropViews(eq(database), eq(schemaName), droppedViewsCaptor.capture());
        Collection<ShardingSphereView> alteredViews = alteredViewsCaptor.getValue();
        assertThat(alteredViews, hasSize(1));
        ShardingSphereView actualView = alteredViews.iterator().next();
        assertThat(actualView.getName(), is("bar_view"));
        assertThat(actualView.getViewDefinition(), is("SELECT * FROM foo_tbl"));
        assertThat(droppedViewsCaptor.getValue(), contains("foo_view"));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertRefreshWithViewDefinition() {
        sqlStatement.setView(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_view"))));
        String expectedViewDefinition = "SELECT * FROM foo_tbl";
        sqlStatement.setViewDefinition(expectedViewDefinition);
        refresher.refresh(metaDataManagerPersistService, databaseType, database, schemaName, sqlStatement);
        ArgumentCaptor<Collection<ShardingSphereView>> alteredViewsCaptor = ArgumentCaptor.forClass(Collection.class);
        ArgumentCaptor<Collection<String>> droppedViewsCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(metaDataManagerPersistService).alterViews(eq(database), eq(schemaName), alteredViewsCaptor.capture());
        verify(metaDataManagerPersistService).dropViews(eq(database), eq(schemaName), droppedViewsCaptor.capture());
        Collection<ShardingSphereView> alteredViews = alteredViewsCaptor.getValue();
        assertThat(alteredViews, hasSize(1));
        ShardingSphereView actualView = alteredViews.iterator().next();
        assertThat(actualView.getName(), is("foo_view"));
        assertThat(actualView.getViewDefinition(), is(expectedViewDefinition));
        assertTrue(droppedViewsCaptor.getValue().isEmpty());
    }
}
