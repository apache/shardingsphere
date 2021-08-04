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

import org.apache.shardingsphere.infra.metadata.mapper.event.dcl.impl.CreateUserStatementEvent;
import org.apache.shardingsphere.infra.metadata.mapper.type.CreateUserStatementEventMapper;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dcl.CreateUserStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dcl.MySQLCreateUserStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.segment.UserSegment;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public final class CreateUserStatementEventMapperTest {
    
    @Test
    public void assertMapToCreateUserStatementEvent() {
        CreateUserStatementEventMapper createUserStatementEventMapper = new CreateUserStatementEventMapper();
        CreateUserStatementEvent createUserStatementEvent = createUserStatementEventMapper.map(getCreateUserStatement());
        assertThat(createUserStatementEvent.getUsers().size(), is(1));
        assertThat(createUserStatementEvent.getUsers().iterator().next().getGrantee().getUsername(), is("test"));
    }
    
    private CreateUserStatement getCreateUserStatement() {
        UserSegment userSegment = new UserSegment();
        userSegment.setUser("test");
        userSegment.setAuth("123456");
        MySQLCreateUserStatement result = new MySQLCreateUserStatement();
        result.getUsers().add(userSegment);
        return result;
    }
}
