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

package org.apache.shardingsphere.encrypt.context;

import org.apache.shardingsphere.encrypt.rule.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.encrypt.spi.context.EncryptColumnDataType;
import org.apache.shardingsphere.encrypt.spi.context.EncryptContext;
import org.junit.Test;

import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class EncryptContextBuilderTest {

    @Test
    public void assertBuildWhenConfigDateType() {
        EncryptContext actual = EncryptContextBuilder.build("encrypt_db", "test", "cipher", mockEncryptRule());
        assertThat(actual.getSchemaName(), is("encrypt_db"));
        assertThat(actual.getTableName(), is("test"));
        assertThat(actual.getColumnName(), is("cipher"));
        assertTrue(actual.getLogicDataType().isPresent());
        assertThat(actual.getLogicDataType().get().getTypeName(), is("int(20) unsigned not null default 0"));
        assertThat(actual.getLogicDataType().get().getDataType(), is(Types.INTEGER));
        assertTrue(actual.getCipherDataType().isPresent());
        assertThat(actual.getCipherDataType().get().getTypeName(), is("varchar(200) not null default ''"));
        assertThat(actual.getCipherDataType().get().getDataType(), is(Types.VARCHAR));
        assertTrue(actual.getAssistedQueryDataType().isPresent());
        assertThat(actual.getAssistedQueryDataType().get().getTypeName(), is("varchar(200) not null"));
        assertThat(actual.getAssistedQueryDataType().get().getDataType(), is(Types.VARCHAR));
        assertTrue(actual.getPlainDataType().isPresent());
        assertThat(actual.getPlainDataType().get().getTypeName(), is("int(20) unsigned not null default 0"));
        assertThat(actual.getPlainDataType().get().getDataType(), is(Types.INTEGER));
    }
    
    @Test
    public void assertBuildWhenNotConfigDateType() {
        EncryptContext actual = EncryptContextBuilder.build("encrypt_db", "test", "cipher", mock(EncryptRule.class));
        assertThat(actual.getSchemaName(), is("encrypt_db"));
        assertThat(actual.getTableName(), is("test"));
        assertThat(actual.getColumnName(), is("cipher"));
        assertFalse(actual.getLogicDataType().isPresent());
        assertFalse(actual.getCipherDataType().isPresent());
        assertFalse(actual.getAssistedQueryDataType().isPresent());
        assertFalse(actual.getPlainDataType().isPresent());
    }
    
    private EncryptRule mockEncryptRule() {
        EncryptRule result = mock(EncryptRule.class);
        EncryptTable encryptTable = mockEncryptTable();
        when(result.findEncryptTable("test")).thenReturn(Optional.of(encryptTable));
        return result;
    }
    
    private EncryptTable mockEncryptTable() {
        EncryptTable result = mock(EncryptTable.class);
        when(result.findEncryptColumn("cipher")).thenReturn(Optional.of(mockEncryptColumn()));
        return result;
    }
    
    private EncryptColumn mockEncryptColumn() {
        Map<String, Integer> dataTypes = new LinkedHashMap<>();
        dataTypes.put("int", Types.INTEGER);
        dataTypes.put("varchar", Types.VARCHAR);
        EncryptColumnDataType logicDataType = new EncryptColumnDataType("int(20) unsigned not null default 0", dataTypes);
        EncryptColumnDataType cipherDataType = new EncryptColumnDataType("varchar(200) not null default ''", dataTypes);
        EncryptColumnDataType assistedQueryDataType = new EncryptColumnDataType("varchar(200) not null", dataTypes);
        EncryptColumnDataType plainDataType = new EncryptColumnDataType("int(20) unsigned not null default 0", dataTypes);
        return new EncryptColumn(logicDataType, "cipher_certificate_number", cipherDataType, "assisted_certificate_number", 
                assistedQueryDataType, "certificate_number_plain", plainDataType, "test");
    }
}
