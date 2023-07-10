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

package org.apache.shardingsphere.metadata.persist.node;

import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseMetaDataNodeTest {
    
    @Test
    void assertGetRulePath() {
        assertThat(DatabaseMetaDataNode.getRulePath(DefaultDatabase.LOGIC_NAME, "0"), is("/metadata/logic_db/versions/0/rules"));
    }
    
    @Test
    void assertGetDatabaseName() {
        Optional<String> actualSchemaName = DatabaseMetaDataNode.getDatabaseName("/metadata/logic_db");
        assertTrue(actualSchemaName.isPresent());
        assertThat(actualSchemaName.get(), is("logic_db"));
    }
    
    @Test
    void assertGetDatabaseNameWithLine() {
        Optional<String> actualSchemaName = DatabaseMetaDataNode.getDatabaseNameByDatabasePath("/metadata/logic-db-test/schemas/logic-db-schema");
        assertTrue(actualSchemaName.isPresent());
        assertThat(actualSchemaName.get(), is("logic-db-test"));
    }
    
    @Test
    void assertGetDatabaseNamePath() {
        assertThat(DatabaseMetaDataNode.getDatabaseNamePath("sharding_db"), is("/metadata/sharding_db"));
    }
    
    @Test
    void assertGetMetaDataTablesPath() {
        assertThat(DatabaseMetaDataNode.getMetaDataTablesPath("sharding_db", "sharding_schema"), is("/metadata/sharding_db/schemas/sharding_schema/tables"));
    }
    
    @Test
    void assertGetMetaDataViewsPath() {
        assertThat(DatabaseMetaDataNode.getMetaDataViewsPath("sharding_db", "sharding_schema"), is("/metadata/sharding_db/schemas/sharding_schema/views"));
    }
    
    @Test
    void assertGetDatabaseNameByDatabasePath() {
        Optional<String> actualSchemaName = DatabaseMetaDataNode.getDatabaseNameByDatabasePath("/metadata/logic_db/schemas/logic_schema");
        assertTrue(actualSchemaName.isPresent());
        assertThat(actualSchemaName.get(), is("logic_db"));
    }
    
    @Test
    void assertGetTableName() {
        Optional<String> actualTableName = DatabaseMetaDataNode.getTableName("/metadata/logic_db/schemas/logic_schema/tables/t_order");
        assertTrue(actualTableName.isPresent());
        assertThat(actualTableName.get(), is("t_order"));
    }
    
    @Test
    void assertGetViewName() {
        Optional<String> actualViewName = DatabaseMetaDataNode.getViewName("/metadata/logic_db/schemas/logic_schema/views/foo_view");
        assertTrue(actualViewName.isPresent());
        assertThat(actualViewName.get(), is("foo_view"));
    }
    
    @Test
    void assertGetSchemaName() {
        Optional<String> actualSchemaName = DatabaseMetaDataNode.getSchemaName("/metadata/logic_db/schemas/logic_schema");
        assertTrue(actualSchemaName.isPresent());
        assertThat(actualSchemaName.get(), is("logic_schema"));
    }
    
    @Test
    void assertGetVersionByDataSourceUnitsPath() {
        Optional<String> actualVersion = DatabaseMetaDataNode.getVersionByDataSourceUnitsPath("/metadata/logic_db/versions/0/data_sources/units");
        assertTrue(actualVersion.isPresent());
        assertThat(actualVersion.get(), is("0"));
    }
    
    @Test
    void assertGetVersionByDataSourceNodesPath() {
        Optional<String> actualVersion = DatabaseMetaDataNode.getVersionByDataSourceNodesPath("/metadata/logic_db/versions/0/data_sources/nodes");
        assertTrue(actualVersion.isPresent());
        assertThat(actualVersion.get(), is("0"));
    }
    
    @Test
    void assertGetActiveVersionPath() {
        assertThat(DatabaseMetaDataNode.getActiveVersionPath("logic_db"), is("/metadata/logic_db/active_version"));
    }
    
    @Test
    void assertGetVersionByRulesPath() {
        Optional<String> actualVersion = DatabaseMetaDataNode.getVersionByRulesPath("/metadata/logic_db/versions/0/rules");
        assertTrue(actualVersion.isPresent());
        assertThat(actualVersion.get(), is("0"));
    }
    
    @Test
    void assertGetDatabaseVersionPath() {
        assertThat(DatabaseMetaDataNode.getDatabaseVersionPath("logic_db", "0"), is("/metadata/logic_db/versions/0"));
    }
    
    @Test
    void assertGetTableMetaDataPath() {
        assertThat(DatabaseMetaDataNode.getTableMetaDataPath("logic_db", "logic_schema", "order"), is("/metadata/logic_db/schemas/logic_schema/tables/order"));
    }
    
    @Test
    void assertGetViewMetaDataPath() {
        assertThat(DatabaseMetaDataNode.getViewMetaDataPath("logic_db", "logic_schema", "order_view"), is("/metadata/logic_db/schemas/logic_schema/views/order_view"));
    }
    
    @Test
    void assertGetMetaDataNodePath() {
        assertThat(DatabaseMetaDataNode.getMetaDataNodePath(), is("/metadata"));
    }
    
    @Test
    void assertGetMetaDataDataSourceNodesPath() {
        assertThat(DatabaseMetaDataNode.getMetaDataDataSourceNodesPath(DefaultDatabase.LOGIC_NAME, "0"), is("/metadata/logic_db/versions/0/data_sources/nodes"));
    }
    
    @Test
    void assertGetMetaDataDataSourceUnitsPath() {
        assertThat(DatabaseMetaDataNode.getMetaDataDataSourceUnitsPath(DefaultDatabase.LOGIC_NAME, "0"), is("/metadata/logic_db/versions/0/data_sources/units"));
    }
}
