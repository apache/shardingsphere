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

import org.apache.shardingsphere.infra.metadata.mapper.type.CreateUserStatementEventMapper;
import org.apache.shardingsphere.infra.metadata.mapper.type.DropUserStatementEventMapper;
import org.apache.shardingsphere.infra.metadata.mapper.type.GrantStatementEventMapper;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dcl.CreateUserStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dcl.DropUserStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dcl.GrantStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterFunctionStatement;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class SQLStatementEventMapperFactoryTest {

    @Test
    public void assertNewInstance() {
        assertThat(SQLStatementEventMapperFactory.newInstance(mock(GrantStatement.class)).get(), instanceOf(GrantStatementEventMapper.class));
        assertThat(SQLStatementEventMapperFactory.newInstance(mock(CreateUserStatement.class)).get(), instanceOf(CreateUserStatementEventMapper.class));
        assertThat(SQLStatementEventMapperFactory.newInstance(mock(DropUserStatement.class)).get(), instanceOf(DropUserStatementEventMapper.class));
        assertFalse(SQLStatementEventMapperFactory.newInstance(mock(AlterFunctionStatement.class)).isPresent());
    }
}
