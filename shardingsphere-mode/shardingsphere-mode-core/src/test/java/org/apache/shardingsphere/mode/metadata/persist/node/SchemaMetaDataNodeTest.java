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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SchemaMetaDataNodeTest {
    
    @Test
    public void assertGetRulePath() {
        assertThat(SchemaMetaDataNode.getRulePath(DefaultSchema.LOGIC_NAME), is("/metadata/logic_db/rules"));
    }
    
    @Test
    public void assertGetSchemaName() {
        assertThat(SchemaMetaDataNode.getSchemaName("/metadata/logic_db/rules"), is(DefaultSchema.LOGIC_NAME));
    }
    
    @Test
    public void assertGetSchemaNameWithLine() {
        assertThat(SchemaMetaDataNode.getSchemaName("/metadata/logic-db-test/rules"), is("logic-db-test"));
    }
    
    @Test
    public void assertGetSchemaNamePath() {
        assertThat(SchemaMetaDataNode.getSchemaNamePath("sharding_db"), is("/metadata/sharding_db"));
    }
    
    @Test
    public void assertGetMetaDataTablesPath() {
        assertThat(SchemaMetaDataNode.getMetaDataTablesPath("sharding_db"), is("/metadata/sharding_db/tables"));
    }

    @Test
    public void assertGetSchemaNameBySchemaPath() {
        assertThat(SchemaMetaDataNode.getSchemaNameBySchemaPath("/metadata/logic_db"), is(DefaultSchema.LOGIC_NAME));
    }
    
    @Test
    public void assertGetTableName() {
        assertThat(SchemaMetaDataNode.getTableName("logic_db", "/metadata/logic_db/tables/t_order"), is("t_order"));
    }
}
