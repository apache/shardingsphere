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

// TODO Rename DatabaseMetaDataNodeTest when metadata structure adjustment completed. #25485
class NewDatabaseMetaDataNodeTest {
    
    @Test
    void assertIsDataSourcesNode() {
        assertTrue(NewDatabaseMetaDataNode.isDataSourcesNode("/metadata/logic_db/data_sources/foo_ds"));
    }
    
    @Test
    void assertGetDataSourceNameByDataSourceNode() {
        Optional<String> actual = NewDatabaseMetaDataNode.getDataSourceNameByDataSourceNode("/metadata/logic_db/data_sources/foo_ds/versions/0");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_ds"));
    }
    
    @Test
    void assertGetVersionByDataSourceNode() {
        Optional<String> actual = NewDatabaseMetaDataNode.getVersionByDataSourceNode("/metadata/logic_db/data_sources/foo_ds/versions/0");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("0"));
    }
    
    @Test
    void asserGetTableNode() {
        assertThat(NewDatabaseMetaDataNode.getTableNode("foo_db", "foo_schema", "foo_table"), is("/metadata/foo_db/schemas/foo_schema/tables/foo_table"));
    }
    
    @Test
    void assertGetDatabaseName() {
        Optional<String> actual = NewDatabaseMetaDataNode.getDatabaseName("/metadata/foo_db");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_db"));
    }
    
    @Test
    void assertGetDatabaseNameBySchemaPath() {
        Optional<String> actual = NewDatabaseMetaDataNode.getDatabaseNameBySchemaPath("/metadata/foo_db/schemas/foo_schema");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_db"));
    }
    
    @Test
    void assertGetSchemaName() {
        Optional<String> actual = NewDatabaseMetaDataNode.getSchemaName("/metadata/foo_db/schemas/foo_schema");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_schema"));
    }
    
    @Test
    void assertGetSchemaNameByTablePath() {
        Optional<String> actual = NewDatabaseMetaDataNode.getSchemaNameByTablePath("/metadata/foo_db/schemas/foo_schema/tables");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_schema"));
    }
    
    @Test
    void assertGetTableName() {
        Optional<String> actual = NewDatabaseMetaDataNode.getTableName("/metadata/foo_db/schemas/foo_schema/tables/foo_table");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_table"));
    }
    
    @Test
    void assertGetViewName() {
        Optional<String> actual = NewDatabaseMetaDataNode.getViewName("/metadata/foo_db/schemas/foo_schema/views/foo_view");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_view"));
    }
    
    @Test
    void assertGetMetaDataDataSourcesNode() {
        assertThat(NewDatabaseMetaDataNode.getDataSourcesNode("foo_db"), is("/metadata/foo_db/data_sources"));
    }
    
    @Test
    void assertGetMetaDataDataSourceNode() {
        assertThat(NewDatabaseMetaDataNode.getDataSourceNode("foo_db", "foo_ds", "0"), is("/metadata/foo_db/data_sources/foo_ds/versions/0"));
    }
    
    @Test
    void assertGetDataSourceVersionsNode() {
        assertThat(NewDatabaseMetaDataNode.getDataSourceVersionsNode("foo_db", "foo_ds"), is("/metadata/foo_db/data_sources/foo_ds/versions"));
    }
    
    @Test
    void assertGetDataSourceActiveVersionNode() {
        assertThat(NewDatabaseMetaDataNode.getDataSourceActiveVersionNode("foo_db", "foo_ds"), is("/metadata/foo_db/data_sources/foo_ds/active_version"));
    }
    
    @Test
    void assertGetDatabaseRuleActiveVersionNode() {
        assertThat(NewDatabaseMetaDataNode.getDatabaseRuleActiveVersionNode("foo_db", "foo_rule", "foo_tables"), is("/metadata/foo_db/rules/foo_rule/foo_tables/active_version"));
    }
    
    @Test
    void assertGetDatabaseRuleVersionsNode() {
        assertThat(NewDatabaseMetaDataNode.getDatabaseRuleVersionsNode("foo_db", "sharding", "foo_key"), is("/metadata/foo_db/rules/sharding/foo_key/versions"));
    }
    
    @Test
    void assertGetDatabaseRuleVersionNode() {
        assertThat(NewDatabaseMetaDataNode.getDatabaseRuleVersionNode("foo_db", "foo_rule", "foo_tables", "1"), is("/metadata/foo_db/rules/foo_rule/foo_tables/versions/1"));
    }
}
