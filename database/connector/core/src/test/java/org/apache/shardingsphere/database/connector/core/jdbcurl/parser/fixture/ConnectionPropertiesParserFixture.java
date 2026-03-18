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

package org.apache.shardingsphere.database.connector.core.jdbcurl.parser.fixture;

import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionProperties;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionPropertiesParser;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.StandardJdbcUrlParser;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ConnectionPropertiesParserFixture implements ConnectionPropertiesParser {
    
    @Override
    public ConnectionProperties parse(final String url, final String username, final String catalog) {
        ConnectionProperties result = mock(ConnectionProperties.class, RETURNS_DEEP_STUBS);
        when(result.getQueryProperties()).thenReturn(new StandardJdbcUrlParser().parse(url, -1).getQueryProperties());
        return result;
    }
    
    @Override
    public String getDatabaseType() {
        return "TRUNK";
    }
}
