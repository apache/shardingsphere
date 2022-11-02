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

package org.apache.shardingsphere.data.pipeline.mysql.datasource;

import org.apache.shardingsphere.data.pipeline.spi.datasource.JdbcQueryPropertiesExtension;
import org.apache.shardingsphere.data.pipeline.spi.datasource.JdbcQueryPropertiesExtensionFactory;
import org.junit.Test;

import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public final class MySQLJdbcQueryPropertiesExtensionTest {
    
    @Test
    public void assertExtendQueryProperties() {
        Optional<JdbcQueryPropertiesExtension> extension = JdbcQueryPropertiesExtensionFactory.getInstance("MySQL");
        assertTrue(extension.isPresent());
        assertExtension(extension.get());
        assertQueryProperties(extension.get().extendQueryProperties());
    }
    
    private void assertExtension(final JdbcQueryPropertiesExtension actual) {
        assertThat(actual, instanceOf(MySQLJdbcQueryPropertiesExtension.class));
        assertThat(actual.getType(), equalTo("MySQL"));
    }
    
    private void assertQueryProperties(final Properties actual) {
        assertThat(actual.size(), equalTo(6));
        assertThat(actual.getProperty("useSSL"), equalTo(Boolean.FALSE.toString()));
        assertThat(actual.getProperty("rewriteBatchedStatements"), equalTo(Boolean.TRUE.toString()));
        assertThat(actual.getProperty("yearIsDateType"), equalTo(Boolean.FALSE.toString()));
        assertThat(actual.getProperty("zeroDateTimeBehavior"), equalTo("convertToNull"));
        assertThat(actual.getProperty("noDatetimeStringSync"), equalTo(Boolean.TRUE.toString()));
        assertThat(actual.getProperty("jdbcCompliantTruncation"), equalTo(Boolean.FALSE.toString()));
    }
}
