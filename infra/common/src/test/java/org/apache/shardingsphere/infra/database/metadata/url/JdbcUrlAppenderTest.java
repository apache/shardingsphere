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

package org.apache.shardingsphere.infra.database.metadata.url;

import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

public final class JdbcUrlAppenderTest {
    
    @Test
    public void assertAppendQueryPropertiesWithoutOriginalQueryProperties() {
        String actual = new JdbcUrlAppender().appendQueryProperties("jdbc:mysql://192.168.0.1:3306/foo_ds", createQueryProperties());
        assertThat(actual, startsWith("jdbc:mysql://192.168.0.1:3306/foo_ds?"));
        assertThat(actual, containsString("rewriteBatchedStatements=true"));
        assertThat(actual, containsString("useSSL=false"));
    }
    
    @Test
    public void assertAppendQueryPropertiesWithOriginalQueryProperties() {
        String actual = new JdbcUrlAppender().appendQueryProperties(
                "jdbc:mysql://192.168.0.1:3306/foo_ds?serverTimezone=UTC&useSSL=false&rewriteBatchedStatements=true", createQueryProperties());
        assertThat(actual, startsWith("jdbc:mysql://192.168.0.1:3306/foo_ds?"));
        assertThat(actual, containsString("serverTimezone=UTC"));
        assertThat(actual, containsString("rewriteBatchedStatements=true"));
        assertThat(actual, containsString("useSSL=false"));
    }
    
    private Properties createQueryProperties() {
        Properties result = new Properties();
        result.put("useSSL", Boolean.FALSE.toString());
        result.put("rewriteBatchedStatements", Boolean.TRUE.toString());
        return result;
    }
}
