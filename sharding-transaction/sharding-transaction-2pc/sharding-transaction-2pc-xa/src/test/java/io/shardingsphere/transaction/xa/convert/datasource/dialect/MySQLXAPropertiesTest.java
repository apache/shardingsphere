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

package io.shardingsphere.transaction.xa.convert.datasource.dialect;

import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class MySQLXAPropertiesTest extends BaseXAPropertiesTest {
    
    @Test
    public void assertBuild() {
        getDataSourceParameter().setUrl("jdbc:mysql://127.0.0.1:3306/demo");
        Properties actual = new MySQLXAProperties().build(getDataSourceParameter());
        assertThat(actual.getProperty("user"), is("root"));
        assertThat(actual.getProperty("password"), is("root"));
        assertThat(actual.getProperty("URL"), is("jdbc:mysql://127.0.0.1:3306/demo"));
        assertThat(actual.getProperty("pinGlobalTxToPhysicalConnection"), is(Boolean.TRUE.toString()));
        assertThat(actual.getProperty("autoReconnect"), is(Boolean.TRUE.toString()));
        assertThat(actual.getProperty("useServerPrepStmts"), is(Boolean.TRUE.toString()));
        assertThat(actual.getProperty("cachePrepStmts"), is(Boolean.TRUE.toString()));
        assertThat(actual.getProperty("prepStmtCacheSize"), is("250"));
        assertThat(actual.getProperty("prepStmtCacheSqlLimit"), is("2048"));
        assertThat(actual.getProperty("useLocalSessionState"), is(Boolean.TRUE.toString()));
        assertThat(actual.getProperty("rewriteBatchedStatements"), is(Boolean.TRUE.toString()));
        assertThat(actual.getProperty("cacheResultSetMetadata"), is(Boolean.TRUE.toString()));
        assertThat(actual.getProperty("cacheServerConfiguration"), is(Boolean.TRUE.toString()));
        assertThat(actual.getProperty("elideSetAutoCommits"), is(Boolean.TRUE.toString()));
        assertThat(actual.getProperty("maintainTimeStats"), is(Boolean.FALSE.toString()));
        assertThat(actual.getProperty("netTimeoutForStreamingResults"), is("0"));
    }
}
