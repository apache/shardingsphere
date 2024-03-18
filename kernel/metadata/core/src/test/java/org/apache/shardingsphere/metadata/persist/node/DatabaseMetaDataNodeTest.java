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

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseMetaDataNodeTest {
    
    @Test
    void assertIsDataSourcesNode() {
        assertTrue(DatabaseMetaDataNode.isDataSourcesNode("/metadata/logic_db/data_sources/foo_ds"));
    }
    
    @Test
    void assertGetDataSourceNameByDataSourceUnitNode() {
        Optional<String> actual = DatabaseMetaDataNode.getDataSourceNameByDataSourceUnitNode("/metadata/logic_db/data_sources/units/foo_ds/versions/0");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_ds"));
    }
    
    @Test
    void assertIsDataSourceUnitActiveVersionNode() {
        assertTrue(DatabaseMetaDataNode.isDataSourceUnitActiveVersionNode("/metadata/logic_db/data_sources/units/foo_ds/active_version"));
    }
    
    @Test
    void assertIsDataSourceNodeActiveVersionNode() {
        assertTrue(DatabaseMetaDataNode.isDataSourceNodeActiveVersionNode("/metadata/logic_db/data_sources/nodes/foo_ds/active_version"));
    }
    
    @Test
    void assertGetDatabaseName() {
        Optional<String> actual = DatabaseMetaDataNode.getDatabaseName("/metadata/foo_db");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_db"));
    }
    
    @Test
    void assertGetDatabaseNameBySchemaNode() {
        Optional<String> actual = DatabaseMetaDataNode.getDatabaseNameBySchemaNode("/metadata/foo_db/schemas/foo_schema");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_db"));
    }
    
    @Test
    void assertGetSchemaName() {
        Optional<String> actual = DatabaseMetaDataNode.getSchemaName("/metadata/foo_db/schemas/foo_schema");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_schema"));
    }
    
    @Test
    void assertGetSchemaNameByTableNode() {
        Optional<String> actual = DatabaseMetaDataNode.getSchemaNameByTableNode("/metadata/foo_db/schemas/foo_schema/tables");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_schema"));
    }
    
    @Test
    void assertGetTableName() {
        Optional<String> actual = DatabaseMetaDataNode.getTableName("/metadata/foo_db/schemas/foo_schema/tables/foo_table/active_version");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_table"));
    }
    
    @Test
    void assertIsTableActiveVersionNode() {
        assertTrue(DatabaseMetaDataNode.isTableActiveVersionNode("/metadata/foo_db/schemas/foo_schema/tables/foo_table/active_version"));
    }
    
    @Test
    void assertGetViewName() {
        Optional<String> actual = DatabaseMetaDataNode.getViewName("/metadata/foo_db/schemas/foo_schema/views/foo_view");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_view"));
    }
    
    @Test
    void assertIsViewActiveVersionNode() {
        assertTrue(DatabaseMetaDataNode.isViewActiveVersionNode("/metadata/foo_db/schemas/foo_schema/views/foo_view/active_version"));
    }
    
    @Test
    void assertGetVersionNodeByActiveVersionPath() {
        assertThat(DatabaseMetaDataNode.getVersionNodeByActiveVersionPath("/metadata/foo_db/schemas/foo_schema/views/foo_view/active_version", "0"),
                is("/metadata/foo_db/schemas/foo_schema/views/foo_view/versions/0"));
    }
    
    @Test
    void assertGetMetaDataDataSourcesNode() {
        assertThat(DatabaseMetaDataNode.getDataSourceUnitsNode("foo_db"), is("/metadata/foo_db/data_sources/units"));
    }
    
    @Test
    void assertGetMetaDataDataSourceNode() {
        assertThat(DatabaseMetaDataNode.getDataSourceUnitNode("foo_db", "foo_ds"), is("/metadata/foo_db/data_sources/units/foo_ds"));
    }
    
    @Test
    void assertGetDataSourceUnitNodeWithVersion() {
        assertThat(DatabaseMetaDataNode.getDataSourceUnitNodeWithVersion("foo_db", "foo_ds", "0"), is("/metadata/foo_db/data_sources/units/foo_ds/versions/0"));
    }
    
    @Test
    void assertGetDataSourceUnitVersionsNode() {
        assertThat(DatabaseMetaDataNode.getDataSourceUnitVersionsNode("foo_db", "foo_ds"), is("/metadata/foo_db/data_sources/units/foo_ds/versions"));
    }
    
    @Test
    void assertGetDataSourceUnitActiveVersionNode() {
        assertThat(DatabaseMetaDataNode.getDataSourceUnitActiveVersionNode("foo_db", "foo_ds"), is("/metadata/foo_db/data_sources/units/foo_ds/active_version"));
    }
    
    @Test
    void assertGetDatabaseRuleActiveVersionNode() {
        assertThat(DatabaseMetaDataNode.getDatabaseRuleActiveVersionNode("foo_db", "foo_rule", "foo_tables"), is("/metadata/foo_db/rules/foo_rule/foo_tables/active_version"));
    }
    
    @Test
    void assertGetDatabaseRuleVersionsNode() {
        assertThat(DatabaseMetaDataNode.getDatabaseRuleVersionsNode("foo_db", "sharding", "foo_key"), is("/metadata/foo_db/rules/sharding/foo_key/versions"));
    }
    
    @Test
    void assertGetDatabaseRuleVersionNode() {
        assertThat(DatabaseMetaDataNode.getDatabaseRuleVersionNode("foo_db", "foo_rule", "foo_tables", "1"), is("/metadata/foo_db/rules/foo_rule/foo_tables/versions/1"));
    }
    
    @Test
    void assertGetTableActiveVersionNode() {
        assertThat(DatabaseMetaDataNode.getTableActiveVersionNode("foo_db", "foo_schema", "foo_table"), is("/metadata/foo_db/schemas/foo_schema/tables/foo_table/active_version"));
    }
    
    @Test
    void assertGetTableVersionsNode() {
        assertThat(DatabaseMetaDataNode.getTableVersionsNode("foo_db", "foo_schema", "foo_table"), is("/metadata/foo_db/schemas/foo_schema/tables/foo_table/versions"));
    }
    
    @Test
    void assertGetTableVersionNode() {
        assertThat(DatabaseMetaDataNode.getTableVersionNode("foo_db", "foo_schema", "foo_table", "0"), is("/metadata/foo_db/schemas/foo_schema/tables/foo_table/versions/0"));
    }
    
    @Test
    void assertGetTableNode() {
        assertThat(DatabaseMetaDataNode.getTableNode("foo_db", "foo_schema", "foo_table"), is("/metadata/foo_db/schemas/foo_schema/tables/foo_table"));
    }
    
    @Test
    void assertGetViewActiveVersionNode() {
        assertThat(DatabaseMetaDataNode.getViewActiveVersionNode("foo_db", "foo_schema", "foo_view"), is("/metadata/foo_db/schemas/foo_schema/views/foo_view/active_version"));
    }
    
    @Test
    void assertGetViewVersionsNode() {
        assertThat(DatabaseMetaDataNode.getViewVersionsNode("foo_db", "foo_schema", "foo_view"), is("/metadata/foo_db/schemas/foo_schema/views/foo_view/versions"));
    }
    
    @Test
    void assertGetViewVersionNode() {
        assertThat(DatabaseMetaDataNode.getViewVersionNode("foo_db", "foo_schema", "foo_view", "0"), is("/metadata/foo_db/schemas/foo_schema/views/foo_view/versions/0"));
    }
    
    @Test
    void assertGetViewNode() {
        assertThat(DatabaseMetaDataNode.getViewNode("foo_db", "foo_schema", "foo_view"), is("/metadata/foo_db/schemas/foo_schema/views/foo_view"));
    }
    
    @Test
    void assertGetDataSourceNodeVersionsNode() {
        assertThat(DatabaseMetaDataNode.getDataSourceNodeVersionsNode("foo_db", "foo_ds"), is("/metadata/foo_db/data_sources/nodes/foo_ds/versions"));
    }
}
