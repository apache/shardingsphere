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

package org.apache.shardingsphere.transaction.xa.jta.datasource.swapper.impl;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class HikariCPPropertyProviderTest {
    
    @Test
    public void assertGetDataSourceClassName() {
        assertThat(new HikariCPPropertyProvider().getDataSourceClassName(), is("com.zaxxer.hikari.HikariDataSource"));
    }
    
    @Test
    public void assertGetURLPropertyName() {
        assertThat(new HikariCPPropertyProvider().getURLPropertyName(), is("jdbcUrl"));
    }
    
    @Test
    public void assertGetUsernamePropertyName() {
        assertThat(new HikariCPPropertyProvider().getUsernamePropertyName(), is("username"));
    }
    
    @Test
    public void assertGetPasswordPropertyName() {
        assertThat(new HikariCPPropertyProvider().getPasswordPropertyName(), is("password"));
    }
}
