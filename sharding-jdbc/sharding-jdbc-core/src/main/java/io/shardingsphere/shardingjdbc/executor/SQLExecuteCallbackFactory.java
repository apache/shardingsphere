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
import io.shardingsphere.transaction.core.internal.executor.SagaSQLExecuteCallback;
import io.shardingsphere.core.transaction.TransactionTypeHolder;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * SQL execute callback factory.
 *
 * @author yangyi
 */
final class SQLExecuteCallbackFactory {
    
    /**
     * Get prepared update callback.
     *
     * @param databaseType database type
     * @param sqlType sql type
     * @param isExceptionThrown is exception thrown
     * @return update callback
     */
    public static SQLExecuteCallback<Integer> getPreparedUpdateSQLExecuteCallback(final DatabaseType databaseType, final SQLType sqlType, final boolean isExceptionThrown) {
        if (isSagaTransaction(sqlType)) {
            return new SagaSQLExecuteCallback<Integer>(databaseType, isExceptionThrown) {
        
                @Override
                protected Integer executeResult(final StatementExecuteUnit executeUnit) throws SQLException {
                    return ((PreparedStatement) executeUnit.getStatement()).executeUpdate();
                }
            };
        }
        return new SQLExecuteCallback<Integer>(databaseType, isExceptionThrown) {
            
            @Override
            protected Integer executeSQL(final StatementExecuteUnit statementExecuteUnit) throws SQLException {
                return ((PreparedStatement) statementExecuteUnit.getStatement()).executeUpdate();
            }
        };
    }
    
    /**
     * Get prepared execute callback.
     *
     * @param databaseType database type
     * @param sqlType sql type
     * @param isExceptionThrown is exception thrown
     * @return execute callback
     */
    public static SQLExecuteCallback<Boolean> getPreparedSQLExecuteCallback(final DatabaseType databaseType, final SQLType sqlType, final boolean isExceptionThrown) {
        if (isSagaTransaction(sqlType)) {
            return new SagaSQLExecuteCallback<Boolean>(databaseType, isExceptionThrown) {
                
                @Override
                protected Boolean executeResult(final StatementExecuteUnit executeUnit) throws SQLException {
                    return ((PreparedStatement) executeUnit.getStatement()).execute();
                }
            };
        }
        return new SQLExecuteCallback<Boolean>(databaseType, isExceptionThrown) {
            
            @Override
            protected Boolean executeSQL(final StatementExecuteUnit statementExecuteUnit) throws SQLException {
                return ((PreparedStatement) statementExecuteUnit.getStatement()).execute();
            }
        };
    }
    
    /**
     * Get batch execute callback.
     *
     * @param databaseType database type
     * @param sqlType sql type
     * @param isExceptionThrown is exception thrown
     * @return batch execute callback
     */
    static SQLExecuteCallback<int[]> getBatchPreparedSQLExecuteCallback(final DatabaseType databaseType, final SQLType sqlType, final boolean isExceptionThrown) {
        if (isSagaTransaction(sqlType)) {
            return new SagaSQLExecuteCallback<int[]>(databaseType, isExceptionThrown) {
                
                @Override
                protected int[] executeResult(final StatementExecuteUnit executeUnit) throws SQLException {
                    return executeUnit.getStatement().executeBatch();
                }
            };
        }
        return new SQLExecuteCallback<int[]>(databaseType, isExceptionThrown) {
            
            @Override
            protected int[] executeSQL(final StatementExecuteUnit statementExecuteUnit) throws SQLException {
                return statementExecuteUnit.getStatement().executeBatch();
            }
        };
    }
    
    /**
     * Get execute callback.
     *
     * @param databaseType database type
     * @param sqlType sql type
     * @param isExceptionThrown is exception thrown
     * @param updater updater defined in {@code StatementExecutor}
     * @return execute callback.
     */
    static SQLExecuteCallback<Integer> getSQLExecuteCallback(final DatabaseType databaseType, final SQLType sqlType, final boolean isExceptionThrown, final StatementExecutor.Updater updater) {
        if (isSagaTransaction(sqlType)) {
            return new SagaSQLExecuteCallback<Integer>(databaseType, isExceptionThrown) {
                
                @Override
                protected Integer executeResult(final StatementExecuteUnit executeUnit) throws SQLException {
                    return updater.executeUpdate(executeUnit.getStatement(), executeUnit.getRouteUnit().getSqlUnit().getSql());
                }
            };
        }
        return new SQLExecuteCallback<Integer>(databaseType, isExceptionThrown) {
            
            @Override
            protected Integer executeSQL(final StatementExecuteUnit statementExecuteUnit) throws SQLException {
                return updater.executeUpdate(statementExecuteUnit.getStatement(), statementExecuteUnit.getRouteUnit().getSqlUnit().getSql());
            }
        };
    }
    
    /**
     * Get execute callback.
     *
     * @param databaseType database type
     * @param sqlType sql type
     * @param isExceptionThrown is exception thrown
     * @param executor executor defined in {@code StatementExecutor}
     * @return single SQLExecuteCallback
     */
    static SQLExecuteCallback<Boolean> getSQLExecuteCallback(final DatabaseType databaseType, final SQLType sqlType, final boolean isExceptionThrown, final StatementExecutor.Executor executor) {
        if (isSagaTransaction(sqlType)) {
            return new SagaSQLExecuteCallback<Boolean>(databaseType, isExceptionThrown) {
                
                @Override
                protected Boolean executeResult(final StatementExecuteUnit executeUnit) throws SQLException {
                    return executor.execute(executeUnit.getStatement(), executeUnit.getRouteUnit().getSqlUnit().getSql());
                }
            };
        }
        return new SQLExecuteCallback<Boolean>(databaseType, isExceptionThrown) {
            
            @Override
            protected Boolean executeSQL(final StatementExecuteUnit statementExecuteUnit) throws SQLException {
                return executor.execute(statementExecuteUnit.getStatement(), statementExecuteUnit.getRouteUnit().getSqlUnit().getSql());
            }
        };
    }
    
    private static boolean isSagaTransaction(final SQLType sqlType) {
        return SQLType.DML.equals(sqlType) && TransactionType.BASE.equals(TransactionTypeHolder.get());
    }
    
}
