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

package org.apache.shardingsphere.database.connector.mysql.metadata.database.option;

import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.datatype.DialectDataTypeOption;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.sql.Types;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MySQLDataTypeOptionTest {
    
    private final DialectDataTypeOption dataTypeOption = new MySQLDataTypeOption();
    
    @Test
    void assertGetExtraDataTypes() {
        Map<String, Integer> extraDataTypes = dataTypeOption.getExtraDataTypes();
        assertThat(extraDataTypes.size(), is(10));
        assertThat(extraDataTypes.get("JSON"), is(Types.LONGVARCHAR));
        assertThat(extraDataTypes.get("geometry"), is(Types.BINARY));
        assertThat(extraDataTypes.get("YEAR"), is(Types.DATE));
    }
    
    @Test
    void assertFindExtraSQLTypeClass() {
        Optional<Class<?>> tinyintActual = dataTypeOption.findExtraSQLTypeClass(Types.TINYINT, false);
        assertTrue(tinyintActual.isPresent());
        assertThat(tinyintActual.get(), is(Integer.class));
        Optional<Class<?>> smallintActual = dataTypeOption.findExtraSQLTypeClass(Types.SMALLINT, false);
        assertTrue(smallintActual.isPresent());
        assertThat(smallintActual.get(), is(Integer.class));
        Optional<Class<?>> integerUnsignedActual = dataTypeOption.findExtraSQLTypeClass(Types.INTEGER, true);
        assertTrue(integerUnsignedActual.isPresent());
        assertThat(integerUnsignedActual.get(), is(Long.class));
        Optional<Class<?>> integerSignedActual = dataTypeOption.findExtraSQLTypeClass(Types.INTEGER, false);
        assertTrue(integerSignedActual.isPresent());
        assertThat(integerSignedActual.get(), is(Integer.class));
        Optional<Class<?>> bigintUnsignedActual = dataTypeOption.findExtraSQLTypeClass(Types.BIGINT, true);
        assertTrue(bigintUnsignedActual.isPresent());
        assertThat(bigintUnsignedActual.get(), is(BigInteger.class));
        Optional<Class<?>> bigintSignedActual = dataTypeOption.findExtraSQLTypeClass(Types.BIGINT, false);
        assertTrue(bigintSignedActual.isPresent());
        assertThat(bigintSignedActual.get(), is(Long.class));
        assertFalse(dataTypeOption.findExtraSQLTypeClass(Types.VARCHAR, false).isPresent());
    }
    
    @Test
    void assertIsIntegerDataType() {
        assertTrue(dataTypeOption.isIntegerDataType(Types.INTEGER));
        assertFalse(dataTypeOption.isIntegerDataType(Types.VARCHAR));
    }
    
    @Test
    void assertIsStringDataType() {
        assertTrue(dataTypeOption.isStringDataType(Types.VARCHAR));
        assertFalse(dataTypeOption.isStringDataType(Types.INTEGER));
    }
    
    @Test
    void assertIsBinaryDataType() {
        assertTrue(dataTypeOption.isBinaryDataType(Types.BINARY));
        assertFalse(dataTypeOption.isBinaryDataType(Types.INTEGER));
    }
}
