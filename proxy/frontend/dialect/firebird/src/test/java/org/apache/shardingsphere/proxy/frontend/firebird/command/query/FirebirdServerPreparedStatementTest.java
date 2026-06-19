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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query;

import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoReturnValue;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

class FirebirdServerPreparedStatementTest {
    
    @Test
    void assertSetRecordCount() throws ReflectiveOperationException {
        FirebirdServerPreparedStatement preparedStatement = createPreparedStatement();
        preparedStatement.setRecordCount(3);
        assertThat(Plugins.getMemberAccessor().get(FirebirdServerPreparedStatement.class.getDeclaredField("recordCount"), preparedStatement), is(3));
    }
    
    @Test
    void assertGetRecordCount() {
        assertThat(createPreparedStatement().getRecordCount(), is(0));
    }
    
    @Test
    void assertGetStatementType() {
        assertThat(createPreparedStatement().getStatementType(), is(FirebirdSQLInfoReturnValue.DELETE));
    }
    
    private FirebirdServerPreparedStatement createPreparedStatement() {
        return new FirebirdServerPreparedStatement("DELETE FROM tbl", mock(SQLStatementContext.class), mock(HintValueContext.class), FirebirdSQLInfoReturnValue.DELETE);
    }
}
