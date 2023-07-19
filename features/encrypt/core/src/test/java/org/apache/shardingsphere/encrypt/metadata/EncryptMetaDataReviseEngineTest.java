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

import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterial;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.MetaDataReviseEngine;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.SchemaMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.TableMetaData;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EncryptMetaDataReviseEngineTest {
    
    private static final String TABLE_NAME = "t_encrypt";
    
    @Test
    void assertRevise() {
        Map<String, SchemaMetaData> schemaMetaData = Collections.singletonMap(
                DefaultDatabase.LOGIC_NAME, new SchemaMetaData(DefaultDatabase.LOGIC_NAME, Collections.singleton(createTableMetaData())));
        TableMetaData actual = new MetaDataReviseEngine(Collections.singleton(mockEncryptRule())).revise(
                schemaMetaData, mock(GenericSchemaBuilderMaterial.class)).get(DefaultDatabase.LOGIC_NAME).getTables().iterator().next();
        assertThat(actual.getColumns().size(), is(2));
        Iterator<ColumnMetaData> columns = actual.getColumns().iterator();
        assertThat(columns.next().getName(), is("id"));
        assertThat(columns.next().getName(), is("pwd"));
    }
    
    private EncryptRule mockEncryptRule() {
        EncryptRule result = mock(EncryptRule.class);
        EncryptTable encryptTable = mock(EncryptTable.class);
        when(result.findEncryptTable(TABLE_NAME)).thenReturn(Optional.of(encryptTable));
        when(encryptTable.isCipherColumn("pwd_cipher")).thenReturn(true);
        when(encryptTable.isLikeQueryColumn("pwd_like")).thenReturn(true);
        when(encryptTable.getLogicColumnByCipherColumn("pwd_cipher")).thenReturn("pwd");
        return result;
    }
    
    private TableMetaData createTableMetaData() {
        Collection<ColumnMetaData> columns = Arrays.asList(new ColumnMetaData("id", Types.INTEGER, true, true, true, true, false),
                new ColumnMetaData("pwd_cipher", Types.VARCHAR, false, false, true, true, false),
                new ColumnMetaData("pwd_like", Types.VARCHAR, false, false, true, true, false));
        return new TableMetaData(TABLE_NAME, columns, Collections.emptyList(), Collections.emptyList());
    }
}
