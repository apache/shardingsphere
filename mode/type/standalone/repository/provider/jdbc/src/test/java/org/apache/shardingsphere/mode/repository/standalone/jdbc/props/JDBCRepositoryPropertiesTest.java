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

package org.apache.shardingsphere.mode.repository.standalone.jdbc.props;

import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class JDBCRepositoryPropertiesTest {
    
    @Test
    void assertGetValue() {
        JDBCRepositoryProperties actual = new JDBCRepositoryProperties(createProperties());
        assertThat(actual.getValue(JDBCRepositoryPropertyKey.PROVIDER), is("MySQL"));
        assertThat(actual.getValue(JDBCRepositoryPropertyKey.JDBC_URL), is("jdbc:mysql://localhost:3306/config"));
        assertThat(actual.getValue(JDBCRepositoryPropertyKey.USERNAME), is("root"));
        assertThat(actual.getValue(JDBCRepositoryPropertyKey.PASSWORD), is("secret"));
    }
    
    private Properties createProperties() {
        return PropertiesBuilder.build(
                new Property(JDBCRepositoryPropertyKey.PROVIDER.getKey(), "MySQL"),
                new Property(JDBCRepositoryPropertyKey.JDBC_URL.getKey(), "jdbc:mysql://localhost:3306/config"),
                new Property(JDBCRepositoryPropertyKey.USERNAME.getKey(), "root"),
                new Property(JDBCRepositoryPropertyKey.PASSWORD.getKey(), "secret"));
    }
    
    @Test
    void assertGetDefaultValue() {
        JDBCRepositoryProperties actual = new JDBCRepositoryProperties(new Properties());
        assertThat(actual.getValue(JDBCRepositoryPropertyKey.PROVIDER), is("H2"));
        assertThat(actual.getValue(JDBCRepositoryPropertyKey.JDBC_URL), is("jdbc:h2:mem:config;DB_CLOSE_DELAY=0;DATABASE_TO_UPPER=false;MODE=MYSQL"));
        assertThat(actual.getValue(JDBCRepositoryPropertyKey.USERNAME), is("sa"));
        assertThat(actual.getValue(JDBCRepositoryPropertyKey.PASSWORD), is(""));
    }
}
