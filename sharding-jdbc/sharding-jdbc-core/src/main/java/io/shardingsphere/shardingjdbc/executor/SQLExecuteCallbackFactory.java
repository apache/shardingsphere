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

package io.shardingsphere.shardingjdbc.executor;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.constant.transaction.TransactionType;
import io.shardingsphere.core.executor.StatementExecuteUnit;
import io.shardingsphere.core.executor.sql.execute.SQLExecuteCallback;
import io.shardingsphere.core.transaction.TransactionTypeHolder;
import io.shardingsphere.transaction.executor.SagaSQLExecuteCallback;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * SQL execute callback factory.
 *
 * @author yangyi
 */
final class SQLExecuteCallbackFactory {
    
    /**
     * Get update callback.
     *
     * @param databaseType database type
     * @param isExceptionThrown is exception thrown
     * @return update callback
     */
    public static SQLExecuteCallback<Integer> getPreparedUpdateSQLExecuteCallback(final DatabaseType databaseType, final boolean isExceptionThrown) {
        if (isSagaTransaction(sqlType)) {
            return getSagaUpdateSQLExecuteCallback(databaseType, sqlType, isExceptionThrown);
        }
        return new SQLExecuteCallback<Integer>(databaseType, isExceptionThrown) {
            
            @Override
            protected Integer executeSQL(final StatementExecuteUnit statementExecuteUnit) throws SQLException {
                return ((PreparedStatement) statementExecuteUnit.getStatement()).executeUpdate();
            }
        };
    }
    
    /**
     * Get execute callback.
     *
     * @param databaseType database type
     * @param isExceptionThrown is exception thrown
     * @return execute callback
     */
    public static SQLExecuteCallback<Boolean> getPreparedSQLExecuteCallback(final DatabaseType databaseType, final boolean isExceptionThrown) {
        if (isSagaTransaction(sqlType)) {
            return getSagaSQLExecuteCallback(databaseType, sqlType, isExceptionThrown);
        }
        return new SQLExecuteCallback<Boolean>(databaseType, isExceptionThrown) {
            
            @Override
            protected Boolean executeSQL(final StatementExecuteUnit statementExecuteUnit) throws SQLException {
                return ((PreparedStatement) statementExecuteUnit.getStatement()).execute();
            }
        };
    }
    
    /**
     * Get batch SQLExecuteCallBack.
     *
     * @param databaseType      types of database
     * @param sqlType           types of sql
     * @param isExceptionThrown is exception thrown
     * @return batch SQLExecuteCallBack
     */
    static SQLExecuteCallback<int[]> getBatchPreparedSQLExecuteCallback(final DatabaseType databaseType, final SQLType sqlType, final boolean isExceptionThrown) {
        if (isSagaTransaction(sqlType)) {
            return getSagaBatchSQLExecuteCallback(databaseType, sqlType, isExceptionThrown);
        }
        return new SQLExecuteCallback<int[]>(databaseType, sqlType, isExceptionThrown) {
            @Override
            protected int[] executeSQL(final StatementExecuteUnit statementExecuteUnit) throws SQLException {
                return statementExecuteUnit.getStatement().executeBatch();
            }
        };
    }
    
    /**
     * Get single SQLExecuteCallBack.
     *
     * @param databaseType      types of database
     * @param sqlType           types of sql
     * @param isExceptionThrown is exception thrown
     * @param updater           Updater defined in StatementExecutor
     * @return single SQLExecuteCallback
     */
    static SQLExecuteCallback<Integer> getSQLExecuteCallback(final DatabaseType databaseType, final SQLType sqlType, final boolean isExceptionThrown, final StatementExecutor.Updater updater) {
        if (isSagaTransaction(sqlType)) {
            return getSagaUpdateSQLExecuteCallback(databaseType, sqlType, isExceptionThrown);
        }
        return new SQLExecuteCallback<Integer>(databaseType, sqlType, isExceptionThrown) {
            
            @Override
            protected Integer executeSQL(final StatementExecuteUnit statementExecuteUnit) throws SQLException {
                return updater.executeUpdate(statementExecuteUnit.getStatement(), statementExecuteUnit.getRouteUnit().getSqlUnit().getSql());
            }
        };
    }
    
    /**
     * Get single SQLExecuteCallBack.
     *
     * @param databaseType      types of database
     * @param sqlType           types of sql
     * @param isExceptionThrown is exception thrown
     * @param executor           Executor defined in StatementExecutor
     * @return single SQLExecuteCallback
     */
    static SQLExecuteCallback<Boolean> getSQLExecuteCallback(final DatabaseType databaseType, final SQLType sqlType, final boolean isExceptionThrown, final StatementExecutor.Executor executor) {
        if (isSagaTransaction(sqlType)) {
            return getSagaSQLExecuteCallback(databaseType, sqlType, isExceptionThrown);
        }
        return new SQLExecuteCallback<Boolean>(databaseType, sqlType, isExceptionThrown) {
            
            @Override
            protected Boolean executeSQL(final StatementExecuteUnit statementExecuteUnit) throws SQLException {
                return executor.execute(statementExecuteUnit.getStatement(), statementExecuteUnit.getRouteUnit().getSqlUnit().getSql());
            }
        };
    }
    
    private static SQLExecuteCallback<int[]> getSagaBatchSQLExecuteCallback(final DatabaseType databaseType, final SQLType sqlType, final boolean isExceptionThrown) {
        return new SagaSQLExecuteCallback<int[]>(databaseType, sqlType, isExceptionThrown) {
            @Override
            protected int[] executeResult() {
                return null;
            }
        };
    }
    
    private static SQLExecuteCallback<Integer> getSagaUpdateSQLExecuteCallback(final DatabaseType databaseType, final SQLType sqlType, final boolean isExceptionThrown) {
        return new SagaSQLExecuteCallback<Integer>(databaseType, sqlType, isExceptionThrown) {
            @Override
            protected Integer executeResult() {
                return 0;
            }
        };
    }
    
    private static SQLExecuteCallback<Boolean> getSagaSQLExecuteCallback(final DatabaseType databaseType, final SQLType sqlType, final boolean isExceptionThrown) {
        return new SagaSQLExecuteCallback<Boolean>(databaseType, sqlType, isExceptionThrown) {
            @Override
            protected Boolean executeResult() {
                return false;
            }
        };
    }
    
    private static boolean isSagaTransaction(final SQLType sqlType) {
        return SQLType.DML.equals(sqlType) && TransactionType.BASE.equals(TransactionTypeHolder.get());
    }
    
}
