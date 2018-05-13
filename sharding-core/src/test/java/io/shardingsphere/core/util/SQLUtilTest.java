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

package io.shardingsphere.core.util;

import io.shardingsphere.core.constant.DatabaseType;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SQLUtilTest {
    
    @Test
    public void assertGetExactlyValue() {
        assertThat(SQLUtil.getExactlyValue("`xxx`"), is("xxx"));
        assertThat(SQLUtil.getExactlyValue("[xxx]"), is("xxx"));
        assertThat(SQLUtil.getExactlyValue("\"xxx\""), is("xxx"));
        assertThat(SQLUtil.getExactlyValue("'xxx'"), is("xxx"));
    }
    
    @Test
    public void assertGetOriginalValueForOtherDatabase() {
        assertThat(SQLUtil.getOriginalValue("select", DatabaseType.H2), is("select"));
        assertThat(SQLUtil.getOriginalValue("select", DatabaseType.Oracle), is("select"));
        assertThat(SQLUtil.getOriginalValue("select", DatabaseType.SQLServer), is("select"));
        assertThat(SQLUtil.getOriginalValue("select", DatabaseType.PostgreSQL), is("select"));
    }
    
    @Test
    public void assertGetOriginalValueForMySQLWithoutKeyword() {
        assertThat(SQLUtil.getOriginalValue("test", DatabaseType.MySQL), is("test"));
    }
    
    @Test
    public void assertGetOriginalValueForMySQLWithDefaultKeyword() {
        assertThat(SQLUtil.getOriginalValue("select", DatabaseType.MySQL), is("`select`"));
    }
    
    @Test
    public void assertGetOriginalValueForMySQLWithMySQLKeyword() {
        assertThat(SQLUtil.getOriginalValue("show", DatabaseType.MySQL), is("`show`"));
    }
}
