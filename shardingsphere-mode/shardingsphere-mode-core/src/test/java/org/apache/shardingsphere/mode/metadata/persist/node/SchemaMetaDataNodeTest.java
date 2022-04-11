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

import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class SchemaMetaDataNodeTest {
    
    @Test
    public void assertGetRulePath() {
        assertThat(SchemaMetaDataNode.getRulePath(DefaultSchema.LOGIC_NAME, "0"), is("/metadata/logic_db/logic_db/versions/0/rules"));
    }
    
    @Test
    public void assertGetSchemaName() {
        Optional<String> actualSchemaName = SchemaMetaDataNode.getSchemaName("/metadata/logic_db/logic_schema/rules");
        assertTrue(actualSchemaName.isPresent());
        assertThat(actualSchemaName.get(), is("logic_schema"));
    }
    
    @Test
    public void assertGetSchemaNameWithLine() {
        Optional<String> actualSchemaName = SchemaMetaDataNode.getSchemaName("/metadata/logic-db-test/logic-db-schema/rules");
        assertTrue(actualSchemaName.isPresent());
        assertThat(actualSchemaName.get(), is("logic-db-schema"));
    }
    
    @Test
    public void assertGetSchemaNamePath() {
        assertThat(SchemaMetaDataNode.getSchemaNamePath("sharding_db"), is("/metadata/sharding_db"));
    }
    
    @Test
    public void assertGetMetaDataTablesPath() {
        assertThat(SchemaMetaDataNode.getMetaDataTablesPath("sharding_db", "sharding_db"), is("/metadata/sharding_db/sharding_db/tables"));
    }

    @Test
    public void assertGetDatabaseNameBySchemaPath() {
        Optional<String> actualSchemaName = SchemaMetaDataNode.getDatabaseNameBySchemaPath("/metadata/logic_db");
        assertTrue(actualSchemaName.isPresent());
        assertThat(actualSchemaName.get(), is("logic_db"));
    }
    
    @Test
    public void assertGetTableName() {
        Optional<String> actualTableName = SchemaMetaDataNode.getTableName("/metadata/logic_db/logic_schema/tables/t_order");
        assertTrue(actualTableName.isPresent());
        assertThat(actualTableName.get(), is("t_order"));
    }
    
    @Test
    public void assertGetVersionBySchemaPath() {
        Optional<String> actualVersion = SchemaMetaDataNode.getVersionByDataSourcesPath("/metadata/logic_db/logic_schema/versions/0/dataSources");
        assertTrue(actualVersion.isPresent());
        assertThat(actualVersion.get(), is("0"));
    }
    
    @Test
    public void assertGetVersionByRulesPath() {
        Optional<String> actualVersion = SchemaMetaDataNode.getVersionByRulesPath("/metadata/logic_db/logic_schema/versions/0/rules");
        assertTrue(actualVersion.isPresent());
        assertThat(actualVersion.get(), is("0"));
    }
    
    @Test
    public void assertGetSchemaVersionPath() {
        assertThat(SchemaMetaDataNode.getSchemaVersionPath("logic_db", "0"), is("/metadata/logic_db/logic_db/versions/0"));
    }

    @Test
    public void assertGetTableMetaDataPath() {
        assertThat(SchemaMetaDataNode.getTableMetaDataPath("db", "schema", "table"), is("/metadata/db/schema/tables/table"));
    }

    @Test
    public void assertGetDatabaseNamePath() {
        assertThat(SchemaMetaDataNode.getDatabaseNamePath("db"), is("/metadata/db"));
    }

    @Test
    public void assertGetMetaDataNodePath() {
        assertThat(SchemaMetaDataNode.getMetaDataNodePath(), is("/metadata"));
    }

    @Test
    public void assertGetMetaDataDataSourcePath() {
        assertThat(SchemaMetaDataNode.getMetaDataDataSourcePath(DefaultSchema.LOGIC_NAME, "0"), is("/metadata/logic_db/logic_db/versions/0/dataSources"));
    }
}
