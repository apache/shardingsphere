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

package org.apache.shardingsphere.mode.metadata.persist.node;

import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public class DatabaseMetadataNodeTest {
    
    @Test
    public void assertGetRulePath() {
        assertThat(DatabaseMetadataNode.getRulePath(DefaultDatabase.LOGIC_NAME, "0"), is("/metadata/logic_db/versions/0/rules"));
    }
    
    @Test
    public void assertGetDatabaseName() {
        Optional<String> actualSchemaName = DatabaseMetadataNode.getDatabaseName("/metadata/logic_db");
        assertTrue(actualSchemaName.isPresent());
        assertThat(actualSchemaName.get(), is("logic_db"));
    }
    
    @Test
    public void assertGetDatabaseNameWithLine() {
        Optional<String> actualSchemaName = DatabaseMetadataNode.getDatabaseNameByDatabasePath("/metadata/logic-db-test/schemas/logic-db-schema");
        assertTrue(actualSchemaName.isPresent());
        assertThat(actualSchemaName.get(), is("logic-db-test"));
    }
    
    @Test
    public void assertGetDatabaseNamePath() {
        assertThat(DatabaseMetadataNode.getDatabaseNamePath("sharding_db"), is("/metadata/sharding_db"));
    }
    
    @Test
    public void assertGetMetadataTablesPath() {
        assertThat(DatabaseMetadataNode.getMetadataTablesPath("sharding_db", "sharding_schema"), is("/metadata/sharding_db/schemas/sharding_schema/tables"));
    }
    
    @Test
    public void assertGetMetadataViewsPath() {
        assertThat(DatabaseMetadataNode.getMetadataViewsPath("sharding_db", "sharding_schema"), is("/metadata/sharding_db/schemas/sharding_schema/views"));
    }
    
    @Test
    public void assertGetDatabaseNameByDatabasePath() {
        Optional<String> actualSchemaName = DatabaseMetadataNode.getDatabaseNameByDatabasePath("/metadata/logic_db/schemas/logic_schema");
        assertTrue(actualSchemaName.isPresent());
        assertThat(actualSchemaName.get(), is("logic_db"));
    }
    
    @Test
    public void assertGetTableName() {
        Optional<String> actualTableName = DatabaseMetadataNode.getTableName("/metadata/logic_db/schemas/logic_schema/tables/t_order");
        assertTrue(actualTableName.isPresent());
        assertThat(actualTableName.get(), is("t_order"));
    }
    
    @Test
    public void assertGetViewName() {
        Optional<String> actualViewName = DatabaseMetadataNode.getViewName("/metadata/logic_db/schemas/logic_schema/views/foo_view");
        assertTrue(actualViewName.isPresent());
        assertThat(actualViewName.get(), is("foo_view"));
    }
    
    @Test
    public void assertGetSchemaName() {
        Optional<String> actualSchemaName = DatabaseMetadataNode.getSchemaName("/metadata/logic_db/schemas/logic_schema");
        assertTrue(actualSchemaName.isPresent());
        assertThat(actualSchemaName.get(), is("logic_schema"));
    }
    
    @Test
    public void assertGetVersionByDatabasePath() {
        Optional<String> actualVersion = DatabaseMetadataNode.getVersionByDataSourcesPath("/metadata/logic_db/versions/0/data_sources");
        assertTrue(actualVersion.isPresent());
        assertThat(actualVersion.get(), is("0"));
    }
    
    @Test
    public void assertGetActiveVersionPath() {
        assertThat(DatabaseMetadataNode.getActiveVersionPath("logic_db"), is("/metadata/logic_db/active_version"));
    }
    
    @Test
    public void assertGetVersionByRulesPath() {
        Optional<String> actualVersion = DatabaseMetadataNode.getVersionByRulesPath("/metadata/logic_db/versions/0/rules");
        assertTrue(actualVersion.isPresent());
        assertThat(actualVersion.get(), is("0"));
    }
    
    @Test
    public void assertGetDatabaseVersionPath() {
        assertThat(DatabaseMetadataNode.getDatabaseVersionPath("logic_db", "0"), is("/metadata/logic_db/versions/0"));
    }
    
    @Test
    public void assertGetTableMetadataPath() {
        assertThat(DatabaseMetadataNode.getTableMetadataPath("logic_db", "logic_schema", "order"), is("/metadata/logic_db/schemas/logic_schema/tables/order"));
    }
    
    @Test
    public void assertGetViewMetadataPath() {
        assertThat(DatabaseMetadataNode.getViewMetadataPath("logic_db", "logic_schema", "order_view"), is("/metadata/logic_db/schemas/logic_schema/views/order_view"));
    }
    
    @Test
    public void assertGetMetadataNodePath() {
        assertThat(DatabaseMetadataNode.getMetadataNodePath(), is("/metadata"));
    }
    
    @Test
    public void assertGetMetadataDataSourcePath() {
        assertThat(DatabaseMetadataNode.getMetadataDataSourcePath(DefaultDatabase.LOGIC_NAME, "0"), is("/metadata/logic_db/versions/0/data_sources"));
    }
}
