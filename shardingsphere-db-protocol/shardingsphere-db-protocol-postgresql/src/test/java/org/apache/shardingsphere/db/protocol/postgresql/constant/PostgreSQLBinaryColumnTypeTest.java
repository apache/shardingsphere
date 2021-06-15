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

package org.apache.shardingsphere.db.protocol.postgresql.constant;

import org.junit.Test;

import java.sql.Types;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class PostgreSQLBinaryColumnTypeTest {
    
    @Test
    public void assertValueOfJDBCType() {
        PostgreSQLBinaryColumnType sqlColumnType = PostgreSQLBinaryColumnType.valueOfJDBCType(Types.BIGINT);
        assertThat(sqlColumnType, is(PostgreSQLBinaryColumnType.POSTGRESQL_TYPE_INT8));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertValueOfJDBCTypeExThrown() {
        PostgreSQLBinaryColumnType.valueOfJDBCType(Types.REF_CURSOR);
    }
    
    @Test
    public void assertValueOf() {
        PostgreSQLBinaryColumnType sqlColumnType = PostgreSQLBinaryColumnType.valueOf(PostgreSQLBinaryColumnType.POSTGRESQL_TYPE_INT8.getValue());
        assertThat(sqlColumnType, is(PostgreSQLBinaryColumnType.POSTGRESQL_TYPE_INT8));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertValueOfExThrown() {
        PostgreSQLBinaryColumnType.valueOf(9999);
    }
    
    @Test
    public void assertGetValue() {
        assertThat(PostgreSQLBinaryColumnType.POSTGRESQL_TYPE_INT8.getValue(), is(20));
    }
}
