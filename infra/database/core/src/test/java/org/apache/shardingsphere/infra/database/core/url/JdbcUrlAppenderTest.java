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

package org.apache.shardingsphere.infra.database.core.url;

import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

class JdbcUrlAppenderTest {
    
    @Test
    void assertAppendQueryPropertiesWithoutOriginalQueryProperties() {
        String actual = new JdbcUrlAppender().appendQueryProperties("jdbc:infra.fixture://192.168.0.1:3306/foo_ds",
                PropertiesBuilder.build(new Property("useSSL", Boolean.FALSE.toString()), new Property("rewriteBatchedStatements", Boolean.TRUE.toString())));
        assertThat(actual, startsWith("jdbc:infra.fixture://192.168.0.1:3306/foo_ds?"));
        assertThat(actual, containsString("rewriteBatchedStatements=true"));
        assertThat(actual, containsString("useSSL=false"));
    }
    
    @Test
    void assertAppendQueryPropertiesWithOriginalQueryProperties() {
        String actual = new JdbcUrlAppender().appendQueryProperties(
                "jdbc:infra.fixture://192.168.0.1:3306/foo_ds?serverTimezone=UTC&useSSL=false&rewriteBatchedStatements=true",
                PropertiesBuilder.build(new Property("useSSL", Boolean.FALSE.toString()), new Property("rewriteBatchedStatements", Boolean.TRUE.toString())));
        assertThat(actual, startsWith("jdbc:infra.fixture://192.168.0.1:3306/foo_ds?"));
        assertThat(actual, containsString("serverTimezone=UTC"));
        assertThat(actual, containsString("rewriteBatchedStatements=true"));
        assertThat(actual, containsString("useSSL=false"));
    }
}
