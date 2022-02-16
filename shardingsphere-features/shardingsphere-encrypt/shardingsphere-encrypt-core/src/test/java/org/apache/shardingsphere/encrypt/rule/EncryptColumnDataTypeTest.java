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

package org.apache.shardingsphere.encrypt.rule;

import org.apache.shardingsphere.encrypt.spi.context.EncryptColumnDataType;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.junit.Test;

import java.util.Map;
import java.util.TreeMap;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class EncryptColumnDataTypeTest {
    
    @Test
    public void assertEncryptColumnDataType() {
        Map<String, Integer> dataTypes = mockDataTypes();
        DatabaseType databaseType = new MySQLDatabaseType();
        assertThat(new EncryptColumnDataType("BIT(5)", dataTypes, databaseType).getDataType(), is(-7));
        assertThat(new EncryptColumnDataType("TINYINT(5) UNSIGNED ZEROFILL", dataTypes, databaseType).getDataType(), is(-6));
        assertThat(new EncryptColumnDataType("DATE", dataTypes, databaseType).getDataType(), is(91));
        assertThat(new EncryptColumnDataType("INTEGER(20) NOT NULL UNSIGNED REFERENCES t_order(order_id)", dataTypes, databaseType).getDataType(), is(4));
        assertThat(new EncryptColumnDataType("INT(20) NOT NULL", dataTypes, databaseType).getDataType(), is(4));
        assertThat(new EncryptColumnDataType("TINYINT NOT NULL", dataTypes, databaseType).getDataType(), is(-6));
        assertThat(new EncryptColumnDataType("SMALLINT NOT NULL", dataTypes, databaseType).getDataType(), is(5));
        assertThat(new EncryptColumnDataType("MIDDLEINT NOT NULL", dataTypes, databaseType).getDataType(), is(4));
        assertThat(new EncryptColumnDataType("MEDIUMINT NOT NULL", dataTypes, databaseType).getDataType(), is(4));
        assertThat(new EncryptColumnDataType("BIGINT", dataTypes, databaseType).getDataType(), is(-5));
        assertThat(new EncryptColumnDataType("REAL", dataTypes, databaseType).getDataType(), is(8));
        assertThat(new EncryptColumnDataType("DOUBLE PRECISION", dataTypes, databaseType).getDataType(), is(8));
        assertThat(new EncryptColumnDataType("FLOAT", dataTypes, databaseType).getDataType(), is(7));
        assertThat(new EncryptColumnDataType("DECIMAL", dataTypes, databaseType).getDataType(), is(3));
        assertThat(new EncryptColumnDataType("NUMERIC", dataTypes, databaseType).getDataType(), is(3));
        assertThat(new EncryptColumnDataType("FIXED", dataTypes, databaseType).getDataType(), is(3));
        assertThat(new EncryptColumnDataType("BOOL", dataTypes, databaseType).getDataType(), is(-6));
        assertThat(new EncryptColumnDataType("BOOLEAN", dataTypes, databaseType).getDataType(), is(-6));
        assertThat(new EncryptColumnDataType("CHAR", dataTypes, databaseType).getDataType(), is(1));
        assertThat(new EncryptColumnDataType("NCHAR NOT NULL", dataTypes, databaseType).getDataType(), is(1));
        assertThat(new EncryptColumnDataType("NATIONAL CHAR", dataTypes, databaseType).getDataType(), is(1));
        assertThat(new EncryptColumnDataType("BINARY", dataTypes, databaseType).getDataType(), is(-2));
        assertThat(new EncryptColumnDataType("CHAR VARYING", dataTypes, databaseType).getDataType(), is(1));
        assertThat(new EncryptColumnDataType("VARCHAR", dataTypes, databaseType).getDataType(), is(12));
        assertThat(new EncryptColumnDataType("NATIONAL VARCHAR", dataTypes, databaseType).getDataType(), is(12));
        assertThat(new EncryptColumnDataType("NVARCHAR", dataTypes, databaseType).getDataType(), is(12));
        assertThat(new EncryptColumnDataType("NCHAR VARCHAR", dataTypes, databaseType).getDataType(), is(12));
        assertThat(new EncryptColumnDataType("NATIONAL CHAR VARYING", dataTypes, databaseType).getDataType(), is(12));
        assertThat(new EncryptColumnDataType("NCHAR VARYING", dataTypes, databaseType).getDataType(), is(12));
        assertThat(new EncryptColumnDataType("VARBINARY", dataTypes, databaseType).getDataType(), is(-3));
        assertThat(new EncryptColumnDataType("YEAR", dataTypes, databaseType).getDataType(), is(91));
        assertThat(new EncryptColumnDataType("DATE", dataTypes, databaseType).getDataType(), is(91));
        assertThat(new EncryptColumnDataType("TIME", dataTypes, databaseType).getDataType(), is(92));
        assertThat(new EncryptColumnDataType("TIMESTAMP", dataTypes, databaseType).getDataType(), is(93));
        assertThat(new EncryptColumnDataType("DATETIME", dataTypes, databaseType).getDataType(), is(93));
        assertThat(new EncryptColumnDataType("TINYBLOB", dataTypes, databaseType).getDataType(), is(-4));
        assertThat(new EncryptColumnDataType("BLOB", dataTypes, databaseType).getDataType(), is(-4));
        assertThat(new EncryptColumnDataType("LONGBLOB", dataTypes, databaseType).getDataType(), is(-4));
        assertThat(new EncryptColumnDataType("LONG VARBINARY", dataTypes, databaseType).getDataType(), is(-4));
        assertThat(new EncryptColumnDataType("LONG CHAR VARYING", dataTypes, databaseType).getDataType(), is(-1));
        assertThat(new EncryptColumnDataType("LONG VARCHAR", dataTypes, databaseType).getDataType(), is(-1));
        assertThat(new EncryptColumnDataType("TINYTEXT", dataTypes, databaseType).getDataType(), is(-1)); 
        assertThat(new EncryptColumnDataType("TEXT", dataTypes, databaseType).getDataType(), is(-1));
        assertThat(new EncryptColumnDataType("MEDIUMTEXT", dataTypes, databaseType).getDataType(), is(-1));
        assertThat(new EncryptColumnDataType("LONGTEXT", dataTypes, databaseType).getDataType(), is(-1));
        assertThat(new EncryptColumnDataType("ENUM(spring)", dataTypes, databaseType).getDataType(), is(12));
        assertThat(new EncryptColumnDataType("SET(spring)", dataTypes, databaseType).getDataType(), is(12));
        assertThat(new EncryptColumnDataType("SERIAL", dataTypes, databaseType).getDataType(), is(-5));
        assertThat(new EncryptColumnDataType("JSON", dataTypes, databaseType).getDataType(), is(-1));
        assertThat(new EncryptColumnDataType("GEOMETRY", dataTypes, databaseType).getDataType(), is(-2));
        assertThat(new EncryptColumnDataType("GEOMETRYCOLLECTION", dataTypes, databaseType).getDataType(), is(-2));
        assertThat(new EncryptColumnDataType("POINT", dataTypes, databaseType).getDataType(), is(-2));
        assertThat(new EncryptColumnDataType("MULTIPOINT", dataTypes, databaseType).getDataType(), is(-2));
        assertThat(new EncryptColumnDataType("LINESTRING", dataTypes, databaseType).getDataType(), is(-2));
        assertThat(new EncryptColumnDataType("MULTILINESTRING", dataTypes, databaseType).getDataType(), is(-2));
        assertThat(new EncryptColumnDataType("POLYGON", dataTypes, databaseType).getDataType(), is(-2));
        assertThat(new EncryptColumnDataType("MULTIPOLYGON", dataTypes, databaseType).getDataType(), is(-2));
    }
    
    private Map<String, Integer> mockDataTypes() {
        Map<String, Integer> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        result.put("BIGINT", -5);
        result.put("BIGINT UNSIGNED", -5);
        result.put("BINARY", -2);
        result.put("BIT", -7);
        result.put("BLOB", -4);
        result.put("BOOL", -7);
        result.put("CHAR", 1);
        result.put("DATE", 91);
        result.put("DATETIME", 93);
        result.put("DECIMAL", 3);
        result.put("DOUBLE", 8);
        result.put("DOUBLE PRECISION", 8);
        result.put("ENUM", 12);
        result.put("FLOAT", 7);
        result.put("INT", 4);
        result.put("INT UNSIGNED", 4);
        result.put("INTEGER", 4);
        result.put("INTEGER UNSIGNED", 4);
        result.put("LONG VARBINARY", -4);
        result.put("LONG VARCHAR", -1);
        result.put("LONGBLOB", -4);
        result.put("LONGTEXT", -1);
        result.put("MEDIUMBLOB", -4);
        result.put("MEDIUMINT", 4);
        result.put("MEDIUMINT UNSIGNED", 4);
        result.put("MEDIUMTEXT", -1);
        result.put("NUMERIC", 2);
        result.put("REAL", 8);
        result.put("SET", 12);
        result.put("SMALLINT", 5);
        result.put("SMALLINT UNSIGNED", 5);
        result.put("TEXT", -1);
        result.put("TIME", 92);
        result.put("TIMESTAMP", 93);
        result.put("TINYBLOB", -4);
        result.put("TINYINT", -6);
        result.put("TINYINT UNSIGNED", -6);
        result.put("TINYTEXT", -1);
        result.put("VARBINARY", -3);
        result.put("VARCHAR", 12);
        return result;
    }
}
