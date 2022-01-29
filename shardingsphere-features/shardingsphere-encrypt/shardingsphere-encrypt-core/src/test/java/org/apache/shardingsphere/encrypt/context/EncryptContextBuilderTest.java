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
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class EncryptContextBuilderTest {

    @Test
    public void assertBuildSuccessfully() {
        EncryptColumn encryptColumn = mockEncryptColumn();
        EncryptTable encryptTable = mock(EncryptTable.class);
        when(encryptTable.findEncryptColumn("cipher")).thenReturn(Optional.of(encryptColumn));
        EncryptRule encryptRule = mock(EncryptRule.class);
        when(encryptRule.findEncryptTable("test")).thenReturn(Optional.of(encryptTable));
        EncryptContext encryptContext = EncryptContextBuilder.build("test", "test", "cipher", encryptRule);
        assertThat(encryptContext.getLogicDataType().get().getTypeName(), is("int(20) unsigned not null default 0"));
        assertThat(encryptContext.getCipherDataType().get().getTypeName(), is("varchar(200) not null default ''"));
        assertThat(encryptContext.getAssistedQueryDataType().get().getTypeName(), is("varchar(200) not null"));
        assertThat(encryptContext.getPlainDataType().get().getTypeName(), is("int(20) unsigned not null default 0"));
    }

    private EncryptColumn mockEncryptColumn() {
        Map<String, Integer> dataTypes = new LinkedHashMap<>();
        dataTypes.put("int", Types.INTEGER);
        dataTypes.put("varchar", Types.VARCHAR);
        EncryptColumnDataType logicDataType = new EncryptColumnDataType("int(20) unsigned not null default 0", dataTypes);
        EncryptColumnDataType cipherDataType = new EncryptColumnDataType("varchar(200) not null default ''", dataTypes);
        EncryptColumnDataType assistedQueryDataType = new EncryptColumnDataType("varchar(200) not null", dataTypes);
        EncryptColumnDataType plainDataType = new EncryptColumnDataType("int(20) unsigned not null default 0", dataTypes);
        return new EncryptColumn(logicDataType, "cipher_certificate_number", cipherDataType,
                "assisted_certificate_number", assistedQueryDataType, "certificate_number_plain", plainDataType, "test");
    }
}
