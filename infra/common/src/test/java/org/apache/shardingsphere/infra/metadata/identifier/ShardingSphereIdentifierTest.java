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

package org.apache.shardingsphere.infra.metadata.identifier;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

class ShardingSphereIdentifierTest {
    
    @Test
    void assertConstructorWithValue() {
        assertThat(new ShardingSphereIdentifier("foo").getValue(), is("foo"));
        assertThat(new ShardingSphereIdentifier("foo").getStandardizeValue(), is("foo"));
    }
    
    @Test
    void assertConstructorWithValueAndDatabaseType() {
        DatabaseType postgresql = getDatabaseType("PostgreSQL");
        assertThat(new ShardingSphereIdentifier("foo", postgresql).getValue(), is("foo"));
        assertThat(new ShardingSphereIdentifier("foo", postgresql).getStandardizeValue(), is("foo"));
        assertThat(new ShardingSphereIdentifier("FOO", postgresql).getStandardizeValue(), is("foo"));
    }
    
    @Test
    void assertConstructorWithIdentifierValueAndDatabaseType() {
        DatabaseType postgresql = getDatabaseType("PostgreSQL");
        assertThat(new ShardingSphereIdentifier(new IdentifierValue("foo"), postgresql).getValue(), is("foo"));
        assertThat(new ShardingSphereIdentifier(new IdentifierValue("FOO"), postgresql).getStandardizeValue(), is("foo"));
        assertThat(new ShardingSphereIdentifier(new IdentifierValue("\"foo\""), postgresql).getStandardizeValue(), is("foo"));
        assertThat(new ShardingSphereIdentifier(new IdentifierValue("\"FOO\""), postgresql).getStandardizeValue(), is("FOO"));
    }
    
    @Test
    void assertEqualsWithNotShardingSphereIdentifier() {
        assertThat(new ShardingSphereIdentifier("foo"), not(new Object()));
    }
    
    @Test
    void assertEqualsWithNullValue() {
        assertThat(new ShardingSphereIdentifier("foo"), not(new ShardingSphereIdentifier((String) null)));
        assertThat(new ShardingSphereIdentifier((String) null), not(new ShardingSphereIdentifier("foo")));
        assertThat(new ShardingSphereIdentifier((String) null), is(new ShardingSphereIdentifier((String) null)));
    }
    
    @Test
    void assertEqualsWithNoDatabaseType() {
        assertThat(new ShardingSphereIdentifier("foo"), is(new ShardingSphereIdentifier("foo")));
        assertThat(new ShardingSphereIdentifier("foo"), is(new ShardingSphereIdentifier("FOO")));
    }
    
    @Test
    void assertHashCodeWithNoDatabaseType() {
        assertThat(new ShardingSphereIdentifier("foo").hashCode(), is(new ShardingSphereIdentifier("foo").hashCode()));
        assertThat(new ShardingSphereIdentifier("foo").hashCode(), is(new ShardingSphereIdentifier("FOO").hashCode()));
    }
    
    @Test
    void assertToString() {
        DatabaseType postgresql = getDatabaseType("PostgreSQL");
        assertThat(new ShardingSphereIdentifier("foo", postgresql).toString(), is("foo"));
        assertThat(new ShardingSphereIdentifier("FOO", postgresql).toString(), is("FOO"));
        assertThat(new ShardingSphereIdentifier(new IdentifierValue("foo"), postgresql).toString(), is("foo"));
    }
    
    @Test
    void assertPostgresSQLLowerCaseUnquoted() {
        DatabaseType postgresql = getDatabaseType("PostgreSQL");
        Map<ShardingSphereIdentifier, String> map = new HashMap<>();
        map.put(new ShardingSphereIdentifier("mytable", postgresql), "value1");
        assertThat(map.get(new ShardingSphereIdentifier("MYTABLE", postgresql)), is("value1"));
        assertThat(map.get(new ShardingSphereIdentifier("mytable", postgresql)), is("value1"));
    }
    
    @Test
    void assertPostgresSQLQuotedCaseSensitive() {
        DatabaseType postgresql = getDatabaseType("PostgreSQL");
        Map<ShardingSphereIdentifier, String> map = new HashMap<>();
        map.put(new ShardingSphereIdentifier(new IdentifierValue("\"MyTable\""), postgresql), "value1");
        assertThat(map.get(new ShardingSphereIdentifier(new IdentifierValue("\"MyTable\""), postgresql)), is("value1"));
        assertThat(map.get(new ShardingSphereIdentifier("MyTable", postgresql)), is(nullValue()));
        assertThat(map.get(new ShardingSphereIdentifier("mytable", postgresql)), is(nullValue()));
    }
    
    @Test
    void assertPostgresSQLStandardizeValue() {
        DatabaseType postgresql = getDatabaseType("PostgreSQL");
        ShardingSphereIdentifier unquoted = new ShardingSphereIdentifier("MyTable", postgresql);
        assertThat(unquoted.getValue(), is("MyTable"));
        assertThat(unquoted.getStandardizeValue(), is("mytable"));
        ShardingSphereIdentifier quoted = new ShardingSphereIdentifier(new IdentifierValue("\"MyTable\""), postgresql);
        assertThat(quoted.getValue(), is("MyTable"));
        assertThat(quoted.getStandardizeValue(), is("MyTable"));
    }
    
    @Test
    void assertOracleUpperCaseUnquoted() {
        DatabaseType oracle = getDatabaseType("Oracle");
        Map<ShardingSphereIdentifier, String> map = new HashMap<>();
        map.put(new ShardingSphereIdentifier("MYTABLE", oracle), "value1");
        assertThat(map.get(new ShardingSphereIdentifier("mytable", oracle)), is("value1"));
        assertThat(map.get(new ShardingSphereIdentifier("MYTABLE", oracle)), is("value1"));
    }
    
    @Test
    void assertOracleQuotedCaseInsensitive() {
        DatabaseType oracle = getDatabaseType("Oracle");
        Map<ShardingSphereIdentifier, String> map = new HashMap<>();
        map.put(new ShardingSphereIdentifier(new IdentifierValue("\"MYTABLE\""), oracle), "value1");
        assertThat(map.get(new ShardingSphereIdentifier(new IdentifierValue("\"MYTABLE\""), oracle)), is("value1"));
        assertThat(map.get(new ShardingSphereIdentifier(new IdentifierValue("\"mytable\""), oracle)), is("value1"));
        assertThat(map.get(new ShardingSphereIdentifier("mytable", oracle)), is("value1"));
    }
    
    @Test
    void assertOracleStandardizeValue() {
        DatabaseType oracle = getDatabaseType("Oracle");
        ShardingSphereIdentifier unquoted = new ShardingSphereIdentifier("mytable", oracle);
        assertThat(unquoted.getValue(), is("mytable"));
        assertThat(unquoted.getStandardizeValue(), is("mytable"));
        ShardingSphereIdentifier quoted = new ShardingSphereIdentifier(new IdentifierValue("\"mytable\""), oracle);
        assertThat(quoted.getValue(), is("mytable"));
        assertThat(quoted.getStandardizeValue(), is("mytable"));
    }
    
    @Test
    void assertClickHouseKeepOriginUnquoted() {
        DatabaseType clickhouse = getDatabaseType("ClickHouse");
        Map<ShardingSphereIdentifier, String> map = new HashMap<>();
        map.put(new ShardingSphereIdentifier("MyTable", clickhouse), "value1");
        assertThat(map.get(new ShardingSphereIdentifier("MyTable", clickhouse)), is("value1"));
        assertThat(map.get(new ShardingSphereIdentifier("mytable", clickhouse)), is("value1"));
    }
    
    @Test
    void assertClickHouseQuotedCaseSensitive() {
        DatabaseType clickhouse = getDatabaseType("ClickHouse");
        Map<ShardingSphereIdentifier, String> map = new HashMap<>();
        map.put(new ShardingSphereIdentifier(new IdentifierValue("\"MyTable\""), clickhouse), "value1");
        assertThat(map.get(new ShardingSphereIdentifier(new IdentifierValue("\"MyTable\""), clickhouse)), is("value1"));
        assertThat(map.get(new ShardingSphereIdentifier(new IdentifierValue("\"mytable\""), clickhouse)), is("value1"));
    }
    
    @Test
    void assertClickHouseStandardizeValue() {
        DatabaseType clickhouse = getDatabaseType("ClickHouse");
        ShardingSphereIdentifier unquoted = new ShardingSphereIdentifier("MyTable", clickhouse);
        assertThat(unquoted.getValue(), is("MyTable"));
        assertThat(unquoted.getStandardizeValue(), is("MyTable"));
        ShardingSphereIdentifier quoted = new ShardingSphereIdentifier(new IdentifierValue("\"MyTable\""), clickhouse);
        assertThat(quoted.getValue(), is("MyTable"));
        assertThat(quoted.getStandardizeValue(), is("MyTable"));
    }
    
    @Test
    void assertHashCodeWithCaseSensitive() {
        DatabaseType postgresql = getDatabaseType("PostgreSQL");
        ShardingSphereIdentifier quoted1 = new ShardingSphereIdentifier(new IdentifierValue("\"MyTable\""), postgresql);
        ShardingSphereIdentifier quoted2 = new ShardingSphereIdentifier(new IdentifierValue("\"MyTable\""), postgresql);
        ShardingSphereIdentifier quoted3 = new ShardingSphereIdentifier(new IdentifierValue("\"mytable\""), postgresql);
        assertThat(quoted1.hashCode(), is(quoted2.hashCode()));
        assertThat(quoted1.hashCode(), not(quoted3.hashCode()));
    }
    
    @Test
    void assertNullValue() {
        DatabaseType postgresql = getDatabaseType("PostgreSQL");
        ShardingSphereIdentifier nullIdentifier = new ShardingSphereIdentifier((String) null);
        assertThat(nullIdentifier.getValue(), is(nullValue()));
        assertThat(nullIdentifier.getStandardizeValue(), is(nullValue()));
        ShardingSphereIdentifier nullIdentifierWithDatabaseType = new ShardingSphereIdentifier(new IdentifierValue((String) null), postgresql);
        assertThat(nullIdentifierWithDatabaseType.getValue(), is(nullValue()));
        assertThat(nullIdentifierWithDatabaseType.getStandardizeValue(), is(nullValue()));
    }
    
    @Test
    void assertEqualsWithNullValues() {
        DatabaseType postgresql = getDatabaseType("PostgreSQL");
        ShardingSphereIdentifier null1 = new ShardingSphereIdentifier((String) null);
        ShardingSphereIdentifier null2 = new ShardingSphereIdentifier((String) null);
        assertThat(null1, is(null2));
        assertThat(null1.hashCode(), is(null2.hashCode()));
        ShardingSphereIdentifier null3 = new ShardingSphereIdentifier(new IdentifierValue((String) null), postgresql);
        ShardingSphereIdentifier null4 = new ShardingSphereIdentifier(new IdentifierValue((String) null), postgresql);
        assertThat(null3, is(null4));
        assertThat(null3.hashCode(), is(null4.hashCode()));
    }
    
    @Test
    void assertNotEqualsWithOneNullValue() {
        DatabaseType postgresql = getDatabaseType("PostgreSQL");
        ShardingSphereIdentifier nullIdentifier = new ShardingSphereIdentifier((String) null);
        ShardingSphereIdentifier nonNullIdentifier = new ShardingSphereIdentifier("foo");
        assertThat(nullIdentifier, not(nonNullIdentifier));
        ShardingSphereIdentifier nullIdentifierWithDatabaseType = new ShardingSphereIdentifier(new IdentifierValue((String) null), postgresql);
        ShardingSphereIdentifier nonNullIdentifierWithDatabaseType = new ShardingSphereIdentifier("foo", postgresql);
        assertThat(nullIdentifierWithDatabaseType, not(nonNullIdentifierWithDatabaseType));
    }
    
    @Test
    void assertMySQLKeepOriginUnquoted() {
        DatabaseType mysql = getDatabaseType("MySQL");
        Map<ShardingSphereIdentifier, String> map = new HashMap<>();
        map.put(new ShardingSphereIdentifier("MyTable", mysql), "value1");
        assertThat(map.get(new ShardingSphereIdentifier("MyTable", mysql)), is("value1"));
        assertThat(map.get(new ShardingSphereIdentifier("mytable", mysql)), is("value1"));
    }
    
    @Test
    void assertMySQLQuotedCaseSensitive() {
        DatabaseType mysql = getDatabaseType("MySQL");
        Map<ShardingSphereIdentifier, String> map = new HashMap<>();
        map.put(new ShardingSphereIdentifier(new IdentifierValue("`MyTable`"), mysql), "value1");
        assertThat(map.get(new ShardingSphereIdentifier(new IdentifierValue("`MyTable`"), mysql)), is("value1"));
        assertThat(map.get(new ShardingSphereIdentifier(new IdentifierValue("`mytable`"), mysql)), is("value1"));
    }
    
    @Test
    void assertMySQLStandardizeValue() {
        DatabaseType mysql = getDatabaseType("MySQL");
        ShardingSphereIdentifier unquoted = new ShardingSphereIdentifier("MyTable", mysql);
        assertThat(unquoted.getValue(), is("MyTable"));
        assertThat(unquoted.getStandardizeValue(), is("MyTable"));
        ShardingSphereIdentifier quoted = new ShardingSphereIdentifier(new IdentifierValue("`MyTable`"), mysql);
        assertThat(quoted.getValue(), is("MyTable"));
        assertThat(quoted.getStandardizeValue(), is("MyTable"));
    }
    
    @Test
    void assertOpenGaussLowerCaseUnquoted() {
        DatabaseType opengauss = getDatabaseType("openGauss");
        Map<ShardingSphereIdentifier, String> map = new HashMap<>();
        map.put(new ShardingSphereIdentifier("mytable", opengauss), "value1");
        assertThat(map.get(new ShardingSphereIdentifier("MYTABLE", opengauss)), is("value1"));
        assertThat(map.get(new ShardingSphereIdentifier("mytable", opengauss)), is("value1"));
    }
    
    @Test
    void assertOpenGaussQuotedCaseSensitive() {
        DatabaseType opengauss = getDatabaseType("openGauss");
        Map<ShardingSphereIdentifier, String> map = new HashMap<>();
        map.put(new ShardingSphereIdentifier(new IdentifierValue("\"MyTable\""), opengauss), "value1");
        assertThat(map.get(new ShardingSphereIdentifier(new IdentifierValue("\"MyTable\""), opengauss)), is("value1"));
        assertThat(map.get(new ShardingSphereIdentifier("MyTable", opengauss)), is(nullValue()));
        assertThat(map.get(new ShardingSphereIdentifier("mytable", opengauss)), is(nullValue()));
    }
    
    @Test
    void assertOpenGaussStandardizeValue() {
        DatabaseType opengauss = getDatabaseType("openGauss");
        ShardingSphereIdentifier unquoted = new ShardingSphereIdentifier("MyTable", opengauss);
        assertThat(unquoted.getValue(), is("MyTable"));
        assertThat(unquoted.getStandardizeValue(), is("mytable"));
        ShardingSphereIdentifier quoted = new ShardingSphereIdentifier(new IdentifierValue("\"MyTable\""), opengauss);
        assertThat(quoted.getValue(), is("MyTable"));
        assertThat(quoted.getStandardizeValue(), is("MyTable"));
    }
    
    private DatabaseType getDatabaseType(final String databaseTypeName) {
        return TypedSPILoader.getService(DatabaseType.class, databaseTypeName);
    }
}
