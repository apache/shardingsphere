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

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.IdentifierPatternType;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingSphereIdentifierTest {
    
    @Test
    void assertConstructorWithValue() {
        assertThat(new ShardingSphereIdentifier("foo").getValue(), is("foo"));
        assertThat(new ShardingSphereIdentifier("foo").getStandardizeValue(), is("foo"));
    }
    
    @Test
    void assertConstructorWithValueAndMetaData() {
        DialectDatabaseMetaData mockMetaData = mockPostgreSQLMetaData();
        assertThat(new ShardingSphereIdentifier("foo", mockMetaData).getValue(), is("foo"));
        assertThat(new ShardingSphereIdentifier("foo", mockMetaData).getStandardizeValue(), is("foo"));
        assertThat(new ShardingSphereIdentifier("FOO", mockMetaData).getStandardizeValue(), is("foo"));
    }
    
    @Test
    void assertConstructorWithIdentifierValue() {
        DialectDatabaseMetaData mockMetaData = mockPostgreSQLMetaData();
        assertThat(new ShardingSphereIdentifier(new IdentifierValue("foo"), mockMetaData).getValue(), is("foo"));
        assertThat(new ShardingSphereIdentifier(new IdentifierValue("FOO"), mockMetaData).getStandardizeValue(), is("foo"));
        assertThat(new ShardingSphereIdentifier(new IdentifierValue("foo", QuoteCharacter.QUOTE), mockMetaData).getStandardizeValue(), is("foo"));
        assertThat(new ShardingSphereIdentifier(new IdentifierValue("FOO", QuoteCharacter.QUOTE), mockMetaData).getStandardizeValue(), is("FOO"));
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
    void assertEqualsWithNoDialectMetadata() {
        assertThat(new ShardingSphereIdentifier("foo"), is(new ShardingSphereIdentifier("foo")));
        assertThat(new ShardingSphereIdentifier("foo"), is(new ShardingSphereIdentifier("FOO")));
    }
    
    @Test
    void assertHashCodeWithNoDialectMetadata() {
        assertThat(new ShardingSphereIdentifier("foo").hashCode(), is(new ShardingSphereIdentifier("foo").hashCode()));
        assertThat(new ShardingSphereIdentifier("foo").hashCode(), is(new ShardingSphereIdentifier("FOO").hashCode()));
    }
    
    @Test
    void assertToString() {
        DialectDatabaseMetaData mockMetaData = mockPostgreSQLMetaData();
        assertThat(new ShardingSphereIdentifier("foo", mockMetaData).toString(), is("foo"));
        assertThat(new ShardingSphereIdentifier("FOO", mockMetaData).toString(), is("FOO"));
        assertThat(new ShardingSphereIdentifier(new IdentifierValue("foo"), mockMetaData).toString(), is("foo"));
    }
    
    @Test
    void assertPostgreSQLLowerCaseUnquoted() {
        DialectDatabaseMetaData postgres = mockPostgreSQLMetaData();
        Map<ShardingSphereIdentifier, String> map = new HashMap<>();
        map.put(new ShardingSphereIdentifier("mytable", postgres), "value1");
        assertThat(map.get(new ShardingSphereIdentifier("MYTABLE", postgres)), is("value1"));
        assertThat(map.get(new ShardingSphereIdentifier("mytable", postgres)), is("value1"));
    }
    
    @Test
    void assertPostgreSQLQuotedCaseSensitive() {
        DialectDatabaseMetaData postgres = mockPostgreSQLMetaData();
        Map<ShardingSphereIdentifier, String> map = new HashMap<>();
        map.put(new ShardingSphereIdentifier(new IdentifierValue("MyTable", QuoteCharacter.QUOTE), postgres), "value1");
        assertThat(map.get(new ShardingSphereIdentifier(new IdentifierValue("MyTable", QuoteCharacter.QUOTE), postgres)), is("value1"));
        assertThat(map.get(new ShardingSphereIdentifier(new IdentifierValue("MyTable", QuoteCharacter.NONE), postgres)), is(nullValue()));
        assertThat(map.get(new ShardingSphereIdentifier(new IdentifierValue("mytable", QuoteCharacter.NONE), postgres)), is(nullValue()));
    }
    
    @Test
    void assertPostgreSQLStandardizeValue() {
        DialectDatabaseMetaData postgres = mockPostgreSQLMetaData();
        ShardingSphereIdentifier unquoted = new ShardingSphereIdentifier("MyTable", postgres);
        assertThat(unquoted.getValue(), is("MyTable"));
        assertThat(unquoted.getStandardizeValue(), is("mytable"));
        ShardingSphereIdentifier quoted = new ShardingSphereIdentifier(new IdentifierValue("MyTable", QuoteCharacter.QUOTE), postgres);
        assertThat(quoted.getValue(), is("MyTable"));
        assertThat(quoted.getStandardizeValue(), is("MyTable"));
    }
    
    @Test
    void assertOracleUpperCaseUnquoted() {
        DialectDatabaseMetaData oracle = mockOracleMetaData();
        Map<ShardingSphereIdentifier, String> map = new HashMap<>();
        map.put(new ShardingSphereIdentifier("MYTABLE", oracle), "value1");
        assertThat(map.get(new ShardingSphereIdentifier("mytable", oracle)), is("value1"));
        assertThat(map.get(new ShardingSphereIdentifier("MYTABLE", oracle)), is("value1"));
    }
    
    @Test
    void assertOracleQuotedCaseSensitive() {
        DialectDatabaseMetaData oracle = mockOracleMetaData();
        Map<ShardingSphereIdentifier, String> map = new HashMap<>();
        map.put(new ShardingSphereIdentifier(new IdentifierValue("MYTABLE", QuoteCharacter.QUOTE), oracle), "value1");
        assertThat(map.get(new ShardingSphereIdentifier(new IdentifierValue("MYTABLE", QuoteCharacter.QUOTE), oracle)), is("value1"));
        assertThat(map.get(new ShardingSphereIdentifier(new IdentifierValue("mytable", QuoteCharacter.QUOTE), oracle)), is(nullValue()));
        assertThat(map.get(new ShardingSphereIdentifier(new IdentifierValue("mytable", QuoteCharacter.NONE), oracle)), is("value1"));
    }
    
    @Test
    void assertOracleStandardizeValue() {
        DialectDatabaseMetaData oracle = mockOracleMetaData();
        ShardingSphereIdentifier unquoted = new ShardingSphereIdentifier("mytable", oracle);
        assertThat(unquoted.getValue(), is("mytable"));
        assertThat(unquoted.getStandardizeValue(), is("MYTABLE"));
        ShardingSphereIdentifier quoted = new ShardingSphereIdentifier(new IdentifierValue("mytable", QuoteCharacter.QUOTE), oracle);
        assertThat(quoted.getValue(), is("mytable"));
        assertThat(quoted.getStandardizeValue(), is("mytable"));
    }
    
    @Test
    void assertClickHouseKeepOriginUnquoted() {
        DialectDatabaseMetaData clickhouse = mockClickHouseMetaData();
        Map<ShardingSphereIdentifier, String> map = new HashMap<>();
        map.put(new ShardingSphereIdentifier("MyTable", clickhouse), "value1");
        assertThat(map.get(new ShardingSphereIdentifier("MyTable", clickhouse)), is("value1"));
        assertThat(map.get(new ShardingSphereIdentifier("mytable", clickhouse)), is(nullValue()));
    }
    
    @Test
    void assertClickHouseQuotedCaseSensitive() {
        DialectDatabaseMetaData clickhouse = mockClickHouseMetaData();
        Map<ShardingSphereIdentifier, String> map = new HashMap<>();
        map.put(new ShardingSphereIdentifier(new IdentifierValue("MyTable", QuoteCharacter.QUOTE), clickhouse), "value1");
        assertThat(map.get(new ShardingSphereIdentifier(new IdentifierValue("MyTable", QuoteCharacter.QUOTE), clickhouse)), is("value1"));
        assertThat(map.get(new ShardingSphereIdentifier(new IdentifierValue("mytable", QuoteCharacter.QUOTE), clickhouse)), is(nullValue()));
    }
    
    @Test
    void assertClickHouseStandardizeValue() {
        DialectDatabaseMetaData clickhouse = mockClickHouseMetaData();
        ShardingSphereIdentifier unquoted = new ShardingSphereIdentifier("MyTable", clickhouse);
        assertThat(unquoted.getValue(), is("MyTable"));
        assertThat(unquoted.getStandardizeValue(), is("MyTable"));
        ShardingSphereIdentifier quoted = new ShardingSphereIdentifier(new IdentifierValue("MyTable", QuoteCharacter.QUOTE), clickhouse);
        assertThat(quoted.getValue(), is("MyTable"));
        assertThat(quoted.getStandardizeValue(), is("MyTable"));
    }
    
    @Test
    void assertHashCodeWithCaseSensitive() {
        DialectDatabaseMetaData postgres = mockPostgreSQLMetaData();
        ShardingSphereIdentifier quoted1 = new ShardingSphereIdentifier(new IdentifierValue("MyTable", QuoteCharacter.QUOTE), postgres);
        ShardingSphereIdentifier quoted2 = new ShardingSphereIdentifier(new IdentifierValue("MyTable", QuoteCharacter.QUOTE), postgres);
        ShardingSphereIdentifier quoted3 = new ShardingSphereIdentifier(new IdentifierValue("mytable", QuoteCharacter.QUOTE), postgres);
        assertThat(quoted1.hashCode(), is(quoted2.hashCode()));
        assertThat(quoted1.hashCode(), not(quoted3.hashCode()));
    }
    
    @Test
    void assertNullValue() {
        DialectDatabaseMetaData postgres = mockPostgreSQLMetaData();
        ShardingSphereIdentifier nullIdentifier = new ShardingSphereIdentifier((String) null);
        assertThat(nullIdentifier.getValue(), is(nullValue()));
        assertThat(nullIdentifier.getStandardizeValue(), is(nullValue()));
        ShardingSphereIdentifier nullIdentifierWithMetaData = new ShardingSphereIdentifier(new IdentifierValue((String) null), postgres);
        assertThat(nullIdentifierWithMetaData.getValue(), is(nullValue()));
        assertThat(nullIdentifierWithMetaData.getStandardizeValue(), is(nullValue()));
    }
    
    @Test
    void assertEqualsWithNullValues() {
        DialectDatabaseMetaData postgres = mockPostgreSQLMetaData();
        ShardingSphereIdentifier null1 = new ShardingSphereIdentifier((String) null);
        ShardingSphereIdentifier null2 = new ShardingSphereIdentifier((String) null);
        assertThat(null1, is(null2));
        assertThat(null1.hashCode(), is(null2.hashCode()));
        ShardingSphereIdentifier null3 = new ShardingSphereIdentifier(new IdentifierValue((String) null), postgres);
        ShardingSphereIdentifier null4 = new ShardingSphereIdentifier(new IdentifierValue((String) null), postgres);
        assertThat(null3, is(null4));
        assertThat(null3.hashCode(), is(null4.hashCode()));
    }
    
    @Test
    void assertNotEqualsWithOneNullValue() {
        DialectDatabaseMetaData postgres = mockPostgreSQLMetaData();
        ShardingSphereIdentifier nullIdentifier = new ShardingSphereIdentifier((String) null);
        ShardingSphereIdentifier nonNullIdentifier = new ShardingSphereIdentifier("foo");
        assertThat(nullIdentifier, not(nonNullIdentifier));
        ShardingSphereIdentifier nullIdentifierWithMeta = new ShardingSphereIdentifier(new IdentifierValue((String) null), postgres);
        ShardingSphereIdentifier nonNullIdentifierWithMeta = new ShardingSphereIdentifier("foo", postgres);
        assertThat(nullIdentifierWithMeta, not(nonNullIdentifierWithMeta));
    }
    
    @Test
    void assertMySQLKeepOriginUnquoted() {
        DialectDatabaseMetaData mysql = mockMySQLMetaData();
        Map<ShardingSphereIdentifier, String> map = new HashMap<>();
        map.put(new ShardingSphereIdentifier("MyTable", mysql), "value1");
        assertThat(map.get(new ShardingSphereIdentifier("MyTable", mysql)), is("value1"));
        assertThat(map.get(new ShardingSphereIdentifier("mytable", mysql)), is("value1"));
    }
    
    @Test
    void assertMySQLQuotedCaseSensitive() {
        DialectDatabaseMetaData mysql = mockMySQLMetaData();
        Map<ShardingSphereIdentifier, String> map = new HashMap<>();
        map.put(new ShardingSphereIdentifier(new IdentifierValue("MyTable", QuoteCharacter.BACK_QUOTE), mysql), "value1");
        // MySQL isCaseSensitive()=false, quoted identifiers still use CaseInsensitiveString
        assertThat(map.get(new ShardingSphereIdentifier(new IdentifierValue("MyTable", QuoteCharacter.BACK_QUOTE), mysql)), is("value1"));
        assertThat(map.get(new ShardingSphereIdentifier(new IdentifierValue("mytable", QuoteCharacter.BACK_QUOTE), mysql)), is("value1"));
    }
    
    @Test
    void assertMySQLStandardizeValue() {
        DialectDatabaseMetaData mysql = mockMySQLMetaData();
        ShardingSphereIdentifier unquoted = new ShardingSphereIdentifier("MyTable", mysql);
        assertThat(unquoted.getValue(), is("MyTable"));
        assertThat(unquoted.getStandardizeValue(), is("MyTable"));
        ShardingSphereIdentifier quoted = new ShardingSphereIdentifier(new IdentifierValue("MyTable", QuoteCharacter.BACK_QUOTE), mysql);
        assertThat(quoted.getValue(), is("MyTable"));
        assertThat(quoted.getStandardizeValue(), is("MyTable"));
    }
    
    @Test
    void assertOpenGaussLowerCaseUnquoted() {
        DialectDatabaseMetaData opengauss = mockOpenGaussMetaData();
        Map<ShardingSphereIdentifier, String> map = new HashMap<>();
        map.put(new ShardingSphereIdentifier("mytable", opengauss), "value1");
        assertThat(map.get(new ShardingSphereIdentifier("MYTABLE", opengauss)), is("value1"));
        assertThat(map.get(new ShardingSphereIdentifier("mytable", opengauss)), is("value1"));
    }
    
    @Test
    void assertOpenGaussQuotedCaseSensitive() {
        DialectDatabaseMetaData opengauss = mockOpenGaussMetaData();
        Map<ShardingSphereIdentifier, String> map = new HashMap<>();
        map.put(new ShardingSphereIdentifier(new IdentifierValue("MyTable", QuoteCharacter.QUOTE), opengauss), "value1");
        assertThat(map.get(new ShardingSphereIdentifier(new IdentifierValue("MyTable", QuoteCharacter.QUOTE), opengauss)), is("value1"));
        assertThat(map.get(new ShardingSphereIdentifier(new IdentifierValue("MyTable", QuoteCharacter.NONE), opengauss)), is(nullValue()));
        assertThat(map.get(new ShardingSphereIdentifier(new IdentifierValue("mytable", QuoteCharacter.NONE), opengauss)), is(nullValue()));
    }
    
    @Test
    void assertOpenGaussStandardizeValue() {
        DialectDatabaseMetaData opengauss = mockOpenGaussMetaData();
        ShardingSphereIdentifier unquoted = new ShardingSphereIdentifier("MyTable", opengauss);
        assertThat(unquoted.getValue(), is("MyTable"));
        assertThat(unquoted.getStandardizeValue(), is("mytable"));
        ShardingSphereIdentifier quoted = new ShardingSphereIdentifier(new IdentifierValue("MyTable", QuoteCharacter.QUOTE), opengauss);
        assertThat(quoted.getValue(), is("MyTable"));
        assertThat(quoted.getStandardizeValue(), is("MyTable"));
    }
    
    private DialectDatabaseMetaData mockPostgreSQLMetaData() {
        DialectDatabaseMetaData result = mock(DialectDatabaseMetaData.class);
        when(result.getQuoteCharacter()).thenReturn(QuoteCharacter.QUOTE);
        when(result.getIdentifierPatternType()).thenReturn(IdentifierPatternType.LOWER_CASE);
        when(result.isCaseSensitive()).thenReturn(true);
        return result;
    }
    
    private DialectDatabaseMetaData mockOracleMetaData() {
        DialectDatabaseMetaData result = mock(DialectDatabaseMetaData.class);
        when(result.getQuoteCharacter()).thenReturn(QuoteCharacter.QUOTE);
        when(result.getIdentifierPatternType()).thenReturn(IdentifierPatternType.UPPER_CASE);
        when(result.isCaseSensitive()).thenReturn(true);
        return result;
    }
    
    private DialectDatabaseMetaData mockClickHouseMetaData() {
        DialectDatabaseMetaData result = mock(DialectDatabaseMetaData.class);
        when(result.getQuoteCharacter()).thenReturn(QuoteCharacter.QUOTE);
        when(result.getIdentifierPatternType()).thenReturn(IdentifierPatternType.KEEP_ORIGIN);
        when(result.isCaseSensitive()).thenReturn(true);
        return result;
    }
    
    private DialectDatabaseMetaData mockMySQLMetaData() {
        DialectDatabaseMetaData result = mock(DialectDatabaseMetaData.class);
        when(result.getQuoteCharacter()).thenReturn(QuoteCharacter.BACK_QUOTE);
        when(result.getIdentifierPatternType()).thenReturn(IdentifierPatternType.KEEP_ORIGIN);
        when(result.isCaseSensitive()).thenReturn(false);
        return result;
    }
    
    private DialectDatabaseMetaData mockOpenGaussMetaData() {
        DialectDatabaseMetaData result = mock(DialectDatabaseMetaData.class);
        when(result.getQuoteCharacter()).thenReturn(QuoteCharacter.QUOTE);
        when(result.getIdentifierPatternType()).thenReturn(IdentifierPatternType.LOWER_CASE);
        when(result.isCaseSensitive()).thenReturn(true);
        return result;
    }
}
