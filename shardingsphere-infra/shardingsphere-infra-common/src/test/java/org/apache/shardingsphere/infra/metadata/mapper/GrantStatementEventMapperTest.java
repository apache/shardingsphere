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

package org.apache.shardingsphere.infra.metadata.mapper;

import org.apache.shardingsphere.infra.metadata.mapper.event.dcl.impl.GrantStatementEvent;
import org.apache.shardingsphere.infra.metadata.mapper.type.GrantStatementEventMapper;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dcl.MySQLCreateUserStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dcl.MySQLGrantStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.segment.UserSegment;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class GrantStatementEventMapperTest {

    @Test
    public void assertMap() {
        GrantStatementEventMapper grantStatementEventMapper = new GrantStatementEventMapper();
        MySQLGrantStatement mySQLGrantStatement = getMySQLGrantStatement();
        mySQLGrantStatement.getUsers().add(getUserSegment("test", "123456", "host"));
        GrantStatementEvent grantStatementEvent = grantStatementEventMapper.map(mySQLGrantStatement);
        assertThat(grantStatementEvent.getUsers().size(), is(1));
        mySQLGrantStatement.getUsers().add(getUserSegment("test2", "654321", "host2"));
        grantStatementEvent = grantStatementEventMapper.map(mySQLGrantStatement);
        Collection<String> userGranteeCollection = grantStatementEvent.getUsers().stream().map(each -> each.getGrantee().toString()).collect(Collectors.toSet());
        assertThat(grantStatementEvent.getUsers().size(), is(2));
        assertThat(userGranteeCollection.contains("test@host"), is(Boolean.TRUE));
        assertThat(userGranteeCollection.contains("test2@host2"), is(Boolean.TRUE));
        grantStatementEvent = grantStatementEventMapper.map(mock(MySQLCreateUserStatement.class));
        assertThat(grantStatementEvent.getUsers().size(), is(0));
        grantStatementEvent = grantStatementEventMapper.map(null);
        assertThat(grantStatementEvent.getUsers().size(), is(0));
    }

    private MySQLGrantStatement getMySQLGrantStatement() {
        MySQLGrantStatement result = mock(MySQLGrantStatement.class);
        when(result.getUsers()).thenReturn(new ArrayList<>());
        return result;
    }

    private UserSegment getUserSegment(final String user, final String auth, final String host) {
        UserSegment result = mock(UserSegment.class);
        when(result.getUser()).thenReturn(user);
        when(result.getAuth()).thenReturn(auth);
        when(result.getHost()).thenReturn(host);
        return result;
    }
}
