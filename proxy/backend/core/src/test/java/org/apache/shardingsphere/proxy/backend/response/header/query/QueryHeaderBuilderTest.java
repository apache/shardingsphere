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

package org.apache.shardingsphere.proxy.backend.response.header.query;

import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class QueryHeaderBuilderTest {
    
    @Test
    void assertAppendProtocolAttributes() throws SQLException {
        QueryHeaderBuilder builder = mock(QueryHeaderBuilder.class, CALLS_REAL_METHODS);
        QueryHeader firstHeader = mock(QueryHeader.class);
        QueryHeader secondHeader = mock(QueryHeader.class);
        ResultSet resultSet = mock(ResultSet.class);
        builder.appendProtocolAttributes(Arrays.asList(firstHeader, secondHeader), resultSet);
        verify(builder).appendProtocolAttributes(firstHeader, resultSet);
        verify(builder).appendProtocolAttributes(secondHeader, resultSet);
    }
}
