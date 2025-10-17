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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.protocol;

import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FirebirdBinaryProtocolValueFactoryTest {
    
    @Test
    void assertGetStringBinaryProtocolValueByVarying() {
        FirebirdBinaryProtocolValue actual = FirebirdBinaryProtocolValueFactory.getBinaryProtocolValue(FirebirdBinaryColumnType.VARYING);
        assertThat(actual, isA(FirebirdStringBinaryProtocolValue.class));
    }
    
    // TODO Uncomment when a specific handler is required; currently BLOB is handled by StringBinaryProtocolValue
    // @Test
    // void assertGetByteBinaryProtocolValue() {
    // FirebirdBinaryProtocolValue actual = FirebirdBinaryProtocolValueFactory.getBinaryProtocolValue(FirebirdBinaryColumnType.BLOB);
    // assertThat(actual, isA(FirebirdByteBinaryProtocolValue.class));
    // }
    
    @Test
    void assertGetInt16BinaryProtocolValue() {
        FirebirdBinaryProtocolValue actual = FirebirdBinaryProtocolValueFactory.getBinaryProtocolValue(FirebirdBinaryColumnType.INT128);
        assertThat(actual, isA(FirebirdInt16BinaryProtocolValue.class));
    }
    
    @Test
    void assertGetInt8BinaryProtocolValue() {
        FirebirdBinaryProtocolValue actual = FirebirdBinaryProtocolValueFactory.getBinaryProtocolValue(FirebirdBinaryColumnType.INT64);
        assertThat(actual, isA(FirebirdInt8BinaryProtocolValue.class));
    }
    
    @Test
    void assertGetInt4BinaryProtocolValue() {
        FirebirdBinaryProtocolValue actual = FirebirdBinaryProtocolValueFactory.getBinaryProtocolValue(FirebirdBinaryColumnType.LONG);
        assertThat(actual, isA(FirebirdInt4BinaryProtocolValue.class));
    }
    
    @Test
    void assertGetInt2BinaryProtocolValue() {
        FirebirdBinaryProtocolValue actual = FirebirdBinaryProtocolValueFactory.getBinaryProtocolValue(FirebirdBinaryColumnType.SHORT);
        assertThat(actual, isA(FirebirdInt2BinaryProtocolValue.class));
    }
    
    @Test
    void assertGetInt1BinaryProtocolValue() {
        FirebirdBinaryProtocolValue actual = FirebirdBinaryProtocolValueFactory.getBinaryProtocolValue(FirebirdBinaryColumnType.BOOLEAN);
        assertThat(actual, isA(FirebirdInt1BinaryProtocolValue.class));
    }
    
    @Test
    void assertGetDoubleBinaryProtocolValue() {
        FirebirdBinaryProtocolValue actual = FirebirdBinaryProtocolValueFactory.getBinaryProtocolValue(FirebirdBinaryColumnType.DOUBLE);
        assertThat(actual, isA(FirebirdDoubleBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetFloatBinaryProtocolValue() {
        FirebirdBinaryProtocolValue actual = FirebirdBinaryProtocolValueFactory.getBinaryProtocolValue(FirebirdBinaryColumnType.FLOAT);
        assertThat(actual, isA(FirebirdFloatBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetDateBinaryProtocolValue() {
        FirebirdBinaryProtocolValue actual = FirebirdBinaryProtocolValueFactory.getBinaryProtocolValue(FirebirdBinaryColumnType.DATE);
        assertThat(actual, isA(FirebirdDateBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetTimeBinaryProtocolValue() {
        FirebirdBinaryProtocolValue actual = FirebirdBinaryProtocolValueFactory.getBinaryProtocolValue(FirebirdBinaryColumnType.TIME);
        assertThat(actual, isA(FirebirdTimeBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetTimestampBinaryProtocolValue() {
        FirebirdBinaryProtocolValue actual = FirebirdBinaryProtocolValueFactory.getBinaryProtocolValue(FirebirdBinaryColumnType.TIMESTAMP);
        assertThat(actual, isA(FirebirdTimestampBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetTimestampTZBinaryProtocolValue() {
        FirebirdBinaryProtocolValue actual = FirebirdBinaryProtocolValueFactory.getBinaryProtocolValue(FirebirdBinaryColumnType.TIMESTAMP_TZ);
        assertThat(actual, isA(FirebirdTimestampTZBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetNullBinaryProtocolValue() {
        FirebirdBinaryProtocolValue actual = FirebirdBinaryProtocolValueFactory.getBinaryProtocolValue(FirebirdBinaryColumnType.NULL);
        assertThat(actual, isA(FirebirdNullBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueExThrown() {
        assertThrows(IllegalArgumentException.class, () -> FirebirdBinaryProtocolValueFactory.getBinaryProtocolValue(FirebirdBinaryColumnType.TIME_TZ_EX));
    }
}
