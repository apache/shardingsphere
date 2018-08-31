/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.executor;

import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.constant.transaction.TransactionType;
import io.shardingsphere.core.executor.sql.SQLExecuteCallback;
import io.shardingsphere.core.executor.sql.StatementExecuteUnit;
import io.shardingsphere.core.routing.SQLExecutionUnit;
import io.shardingsphere.core.routing.SQLUnit;
import io.shardingsphere.transaction.TransactionTypeHolder;
import io.shardingsphere.transaction.manager.base.executor.SagaSQLExeucteCallback;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SQLExeucteCallbackFactoryTest {
    
    @Mock
    private PreparedStatement preparedStatement;
    
    private final String dsName = "ds";
    
    private final String sql = "SELECT now()";
    
    private final StatementExecuteUnit unit = new StatementExecuteUnit() {
        @Override
        public SQLExecutionUnit getSqlExecutionUnit() {
            return new SQLExecutionUnit(dsName, new SQLUnit(sql, new ArrayList<List<Object>>()));
        }
    
        @Override
        public Statement getStatement() {
            return preparedStatement;
        }
    };
    
    @Test
    public void assertGetSagaSQLExecuteCallback() {
        TransactionTypeHolder.set(TransactionType.BASE);
        SQLExecuteCallback sqlExecuteCallback = SQLExecuteCallbackFactory.getPreparedUpdateSQLExecuteCallback(SQLType.DML, false, null);
        assertThat(sqlExecuteCallback instanceof SagaSQLExeucteCallback, is(true));
        sqlExecuteCallback = SQLExecuteCallbackFactory.getPreparedSQLExecuteCallback(SQLType.DML, false, null);
        assertThat(sqlExecuteCallback instanceof SagaSQLExeucteCallback, is(true));
    }
    
    @Test
    public void assertGetPreparedUpdateSQLExecuteCallback() throws SQLException {
        SQLExecuteCallback sqlExecuteCallback = SQLExecuteCallbackFactory.getPreparedUpdateSQLExecuteCallback(SQLType.DML, true, null);
        sqlExecuteCallback.execute(unit);
        verify(preparedStatement).executeUpdate();
    }
    
    @Test
    public void assertGetPreparedQuerySQLExecuteCallback() throws SQLException {
        SQLExecuteCallback sqlExecuteCallback = SQLExecuteCallbackFactory.getPreparedQuerySQLExecuteCallback(SQLType.DQL, true, null);
        sqlExecuteCallback.execute(unit);
        verify(preparedStatement).executeQuery();
    }
    
    @Test
    public void assertGetPreparedSQLExecuteCallback() throws SQLException {
        SQLExecuteCallback sqlExecuteCallback = SQLExecuteCallbackFactory.getPreparedSQLExecuteCallback(SQLType.DQL, true, null);
        sqlExecuteCallback.execute(unit);
        verify(preparedStatement).execute();
    }
}
