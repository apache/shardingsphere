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

package org.apache.shardingsphere.driver.jdbc.unsupported;

import org.apache.shardingsphere.driver.jdbc.core.statement.metadata.ShardingSphereParameterMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.jupiter.api.Test;

import java.sql.SQLFeatureNotSupportedException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class UnsupportedOperationParameterMetaTest {
    
    private final ShardingSphereParameterMetaData shardingSphereParameterMetaData = new ShardingSphereParameterMetaData(mock(SQLStatement.class));
    
    @Test
    void assertIsNullable() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereParameterMetaData.getParameterClassName(1));
    }
    
    @Test
    void assertIsSigned() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereParameterMetaData.isSigned(1));
    }
    
    @Test
    void assertGetPrecision() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereParameterMetaData.getPrecision(1));
    }
    
    @Test
    void assertGetScale() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereParameterMetaData.getScale(1));
    }
    
    @Test
    void assertGetParameterType() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereParameterMetaData.getParameterType(1));
    }
    
    @Test
    void assertGetParameterTypeName() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereParameterMetaData.getParameterTypeName(1));
    }
    
    @Test
    void assertGetParameterClassName() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereParameterMetaData.getParameterClassName(1));
    }
    
    @Test
    void assertGetParameterMode() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereParameterMetaData.getParameterMode(1));
    }
}
