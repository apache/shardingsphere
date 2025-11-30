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

package org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.datatype;

import org.junit.jupiter.api.Test;

import java.sql.Types;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultDataTypeOptionTest {
    
    private final DialectDataTypeOption dataTypeOption = new DefaultDataTypeOption();
    
    @Test
    void assertIsIntegerDataType() {
        assertTrue(dataTypeOption.isIntegerDataType(Types.INTEGER));
        assertTrue(dataTypeOption.isIntegerDataType(Types.BIGINT));
        assertTrue(dataTypeOption.isIntegerDataType(Types.SMALLINT));
        assertTrue(dataTypeOption.isIntegerDataType(Types.TINYINT));
        assertFalse(dataTypeOption.isIntegerDataType(Types.VARCHAR));
    }
    
    @Test
    void assertIsStringDataType() {
        assertTrue(dataTypeOption.isStringDataType(Types.CHAR));
        assertTrue(dataTypeOption.isStringDataType(Types.VARCHAR));
        assertTrue(dataTypeOption.isStringDataType(Types.LONGVARCHAR));
        assertTrue(dataTypeOption.isStringDataType(Types.NCHAR));
        assertTrue(dataTypeOption.isStringDataType(Types.NVARCHAR));
        assertTrue(dataTypeOption.isStringDataType(Types.LONGNVARCHAR));
        assertFalse(dataTypeOption.isStringDataType(Types.INTEGER));
    }
    
    @Test
    void assertIsBinaryDataType() {
        assertTrue(dataTypeOption.isBinaryDataType(Types.BINARY));
        assertTrue(dataTypeOption.isBinaryDataType(Types.VARBINARY));
        assertTrue(dataTypeOption.isBinaryDataType(Types.LONGVARBINARY));
        assertFalse(dataTypeOption.isBinaryDataType(Types.VARCHAR));
    }
}
