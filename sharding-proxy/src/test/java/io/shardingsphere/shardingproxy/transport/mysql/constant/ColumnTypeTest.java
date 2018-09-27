/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingproxy.transport.mysql.constant;

import org.junit.Test;

import java.sql.Types;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ColumnTypeTest {
    
    @Test
    public void assertValueOfJDBC() {
        assertThat(ColumnType.valueOfJDBCType(Types.BIT), is(ColumnType.MYSQL_TYPE_BIT));
        assertThat(ColumnType.valueOfJDBCType(Types.TINYINT), is(ColumnType.MYSQL_TYPE_TINY));
        assertThat(ColumnType.valueOfJDBCType(Types.SMALLINT), is(ColumnType.MYSQL_TYPE_SHORT));
        assertThat(ColumnType.valueOfJDBCType(Types.INTEGER), is(ColumnType.MYSQL_TYPE_LONG));
        assertThat(ColumnType.valueOfJDBCType(Types.BIGINT), is(ColumnType.MYSQL_TYPE_LONGLONG));
        assertThat(ColumnType.valueOfJDBCType(Types.FLOAT), is(ColumnType.MYSQL_TYPE_FLOAT));
        assertThat(ColumnType.valueOfJDBCType(Types.REAL), is(ColumnType.MYSQL_TYPE_FLOAT));
        assertThat(ColumnType.valueOfJDBCType(Types.DOUBLE), is(ColumnType.MYSQL_TYPE_DOUBLE));
        assertThat(ColumnType.valueOfJDBCType(Types.NUMERIC), is(ColumnType.MYSQL_TYPE_NEWDECIMAL));
        assertThat(ColumnType.valueOfJDBCType(Types.DECIMAL), is(ColumnType.MYSQL_TYPE_NEWDECIMAL));
        assertThat(ColumnType.valueOfJDBCType(Types.CHAR), is(ColumnType.MYSQL_TYPE_VARCHAR));
        assertThat(ColumnType.valueOfJDBCType(Types.VARCHAR), is(ColumnType.MYSQL_TYPE_VARCHAR));
        assertThat(ColumnType.valueOfJDBCType(Types.LONGVARCHAR), is(ColumnType.MYSQL_TYPE_VARCHAR));
        assertThat(ColumnType.valueOfJDBCType(Types.DATE), is(ColumnType.MYSQL_TYPE_DATE));
        assertThat(ColumnType.valueOfJDBCType(Types.TIME), is(ColumnType.MYSQL_TYPE_TIME));
        assertThat(ColumnType.valueOfJDBCType(Types.TIMESTAMP), is(ColumnType.MYSQL_TYPE_TIMESTAMP));
        assertThat(ColumnType.valueOfJDBCType(Types.BINARY), is(ColumnType.MYSQL_TYPE_BLOB));
        assertThat(ColumnType.valueOfJDBCType(Types.VARBINARY), is(ColumnType.MYSQL_TYPE_MEDIUM_BLOB));
        assertThat(ColumnType.valueOfJDBCType(Types.LONGVARBINARY), is(ColumnType.MYSQL_TYPE_LONG_BLOB));
        assertThat(ColumnType.valueOfJDBCType(Types.NULL), is(ColumnType.MYSQL_TYPE_NULL));
        assertThat(ColumnType.valueOfJDBCType(Types.BLOB), is(ColumnType.MYSQL_TYPE_BLOB));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertValueOfJDBCIllegalArgument() {
        ColumnType.valueOfJDBCType(9999);
    }
    
    @Test
    public void assertValueOf() {
        assertThat(ColumnType.valueOf(ColumnType.MYSQL_TYPE_DECIMAL.getValue()), is(ColumnType.MYSQL_TYPE_DECIMAL));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertValueOfWithIllegalArgument() {
        ColumnType.valueOf(-1);
    }
}
