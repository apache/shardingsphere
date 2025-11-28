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

package org.apache.shardingsphere.encrypt.metadata;

import org.apache.shardingsphere.database.connector.core.metadata.data.model.ColumnMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.SchemaMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterial;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.MetaDataReviseEngine;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EncryptMetaDataReviseEngineTest {
    
    @Test
    void assertRevise() {
        Map<String, SchemaMetaData> schemaMetaData = Collections.singletonMap("foo_db", new SchemaMetaData("foo_db", Collections.singleton(createTableMetaData())));
        Map<String, ShardingSphereSchema> actual = new MetaDataReviseEngine(Collections.singleton(mockEncryptRule())).revise(schemaMetaData, mock(GenericSchemaBuilderMaterial.class));
        assertThat(actual.size(), is(1));
        assertTrue(actual.containsKey("foo_db"));
        assertThat(actual.get("foo_db").getAllTables().size(), is(1));
        ShardingSphereTable table = actual.get("foo_db").getAllTables().iterator().next();
        assertThat(table.getAllColumns().size(), is(2));
        List<ShardingSphereColumn> columns = new ArrayList<>(table.getAllColumns());
        assertThat(columns.get(0).getName(), is("id"));
        assertThat(columns.get(1).getName(), is("pwd"));
    }
    
    private EncryptRule mockEncryptRule() {
        EncryptRule result = mock(EncryptRule.class, RETURNS_DEEP_STUBS);
        EncryptTable encryptTable = mock(EncryptTable.class);
        when(result.findEncryptTable("foo_tbl")).thenReturn(Optional.of(encryptTable));
        when(encryptTable.isCipherColumn("pwd_cipher")).thenReturn(true);
        when(encryptTable.isLikeQueryColumn("pwd_like")).thenReturn(true);
        when(encryptTable.getLogicColumnByCipherColumn("pwd_cipher")).thenReturn("pwd");
        return result;
    }
    
    private TableMetaData createTableMetaData() {
        Collection<ColumnMetaData> columns = Arrays.asList(new ColumnMetaData("id", Types.INTEGER, true, true,"int", true, true, false, false),
                new ColumnMetaData("pwd_cipher", Types.VARCHAR, false, false,"varchar", true, true, false, false),
                new ColumnMetaData("pwd_like", Types.VARCHAR, false, false,"varchar", true, true, false, false));
        return new TableMetaData("foo_tbl", columns, Collections.emptyList(), Collections.emptyList());
    }
}
