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

package org.apache.shardingsphere.database.connector.opengauss.metadata.database.option;

import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.datatype.DialectDataTypeOption;
import org.junit.jupiter.api.Test;

import java.sql.Types;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenGaussDataTypeOptionTest {
    
    private final DialectDataTypeOption dataTypeOption = new OpenGaussDataTypeOption();
    
    @Test
    void assertGetExtraDataTypes() {
        assertThat(dataTypeOption.getExtraDataTypes().get("SMALLINT"), is(Types.SMALLINT));
        assertThat(dataTypeOption.getExtraDataTypes().get("INT"), is(Types.INTEGER));
        assertThat(dataTypeOption.getExtraDataTypes().get("INTEGER"), is(Types.INTEGER));
        assertThat(dataTypeOption.getExtraDataTypes().get("BIGINT"), is(Types.BIGINT));
        assertThat(dataTypeOption.getExtraDataTypes().get("DECIMAL"), is(Types.DECIMAL));
        assertThat(dataTypeOption.getExtraDataTypes().get("NUMERIC"), is(Types.NUMERIC));
        assertThat(dataTypeOption.getExtraDataTypes().get("REAL"), is(Types.REAL));
        assertThat(dataTypeOption.getExtraDataTypes().get("BOOL"), is(Types.BOOLEAN));
        assertThat(dataTypeOption.getExtraDataTypes().get("CHARACTER VARYING"), is(Types.VARCHAR));
    }
    
    @Test
    void assertFindExtraSQLTypeClass() {
        assertFalse(dataTypeOption.findExtraSQLTypeClass(Types.SMALLINT, false).isPresent());
    }
    
    @Test
    void assertIsIntegerDataType() {
        assertTrue(dataTypeOption.isIntegerDataType(Types.INTEGER));
    }
    
    @Test
    void assertIsStringDataType() {
        assertTrue(dataTypeOption.isStringDataType(Types.VARCHAR));
    }
    
    @Test
    void assertIsBinaryDataType() {
        assertTrue(dataTypeOption.isBinaryDataType(Types.BINARY));
    }
}
