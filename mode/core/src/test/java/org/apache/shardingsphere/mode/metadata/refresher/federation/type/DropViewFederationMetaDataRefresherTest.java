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
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.refresher.federation.FederationMetaDataRefresher;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.DropViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DropViewFederationMetaDataRefresherTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private final DropViewFederationMetaDataRefresher refresher = (DropViewFederationMetaDataRefresher) TypedSPILoader.getService(FederationMetaDataRefresher.class, DropViewStatement.class);
    
    @Mock
    private MetaDataManagerPersistService metaDataManagerPersistService;
    
    @Mock
    private ShardingSphereDatabase database;
    
    private DropViewStatement sqlStatement;
    
    @BeforeEach
    void setUp() {
        sqlStatement = new DropViewStatement(databaseType);
    }
    
    @Test
    void assertRefresh() {
        String schemaName = "foo_schema";
        sqlStatement.getViews().add(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_view"))));
        sqlStatement.getViews().add(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("bar_view"))));
        refresher.refresh(metaDataManagerPersistService, databaseType, database, schemaName, sqlStatement);
        verify(metaDataManagerPersistService).dropViews(database, schemaName, Arrays.asList("foo_view", "bar_view"));
    }
}
