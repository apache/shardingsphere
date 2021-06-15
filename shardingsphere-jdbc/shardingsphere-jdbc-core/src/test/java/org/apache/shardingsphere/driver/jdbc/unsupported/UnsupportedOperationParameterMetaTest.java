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
import org.junit.Test;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

import static org.mockito.Mockito.mock;

public final class UnsupportedOperationParameterMetaTest {
    
    private final ShardingSphereParameterMetaData shardingSphereParameterMetaData = new ShardingSphereParameterMetaData(mock(SQLStatement.class));
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertIsNullable() throws SQLException {
        shardingSphereParameterMetaData.getParameterClassName(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertIsSigned() throws SQLException {
        shardingSphereParameterMetaData.isSigned(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetPrecision() throws SQLException {
        shardingSphereParameterMetaData.getPrecision(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetScale() throws SQLException {
        shardingSphereParameterMetaData.getScale(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetParameterType() throws SQLException {
        shardingSphereParameterMetaData.getParameterType(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetParameterTypeName() throws SQLException {
        shardingSphereParameterMetaData.getParameterTypeName(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetParameterClassName() throws SQLException {
        shardingSphereParameterMetaData.getParameterClassName(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetParameterMode() throws SQLException {
        shardingSphereParameterMetaData.getParameterMode(1);
    }
}
