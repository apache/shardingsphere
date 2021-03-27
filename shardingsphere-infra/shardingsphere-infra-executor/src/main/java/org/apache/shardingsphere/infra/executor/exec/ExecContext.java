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

package org.apache.shardingsphere.infra.executor.exec;

import lombok.Getter;
import lombok.NonNull;
import org.apache.calcite.sql.SqlNode;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.StorageResourceOption;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.ExecutorJDBCManager;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Properties;

@Getter
public final class ExecContext {
    
    @NonNull
    private ShardingRule shardingRule;
    
    @NonNull
    private List<Object> parameters;
    
    @NonNull
    private DatabaseType databaseType;
    
    @NonNull
    private ExecutorJDBCManager executorJDBCManager;
    
    private OriginSql originSql;
    
    private ConfigurationProperties props;
    
    private StorageResourceOption option;
    
    private boolean holdTransaction;
    
    private ExecContext(final ShardingRule shardingRule, final List<Object> parameters, final DatabaseType databaseType, final ExecutorJDBCManager executorJDBCManager) {
        this.shardingRule = shardingRule;
        this.parameters = parameters;
        this.databaseType = databaseType;
        this.executorJDBCManager = executorJDBCManager;
    }
    
    @Getter
    public static class OriginSql {
        
        private String sql;
        
        private SqlNode sqlNode;
        
        public OriginSql(final String sql, final SqlNode sqlNode) {
            this.sql = sql;
            this.sqlNode = sqlNode;
        }
    }
    
    public static final class ExecContextBuilder {
        
        private final ShardingRule shardingRule;
        
        private final List<Object> parameters;
        
        private final DatabaseType databaseType;
        
        private final ExecutorJDBCManager executorJDBCManager;
        
        private OriginSql originSql;
        
        private ConfigurationProperties props;
        
        private StorageResourceOption storageResourceOption;
        
        private boolean holdTransaction;
        
        private ExecContextBuilder(final ShardingRule shardingRule, final List<Object> parameters, final DatabaseType databaseType, final ExecutorJDBCManager executorJDBCManager) {
            this.shardingRule = shardingRule;
            this.parameters = parameters;
            this.databaseType = databaseType;
            this.executorJDBCManager = executorJDBCManager;
        }
    
        /**
         * set original sql.
         * @param originSql original sql
         * @return this builder
         */
        public ExecContextBuilder originSql(final OriginSql originSql) {
            this.originSql = originSql;
            return this;
        }
    
        /**
         * set configuration properties.
         * @param props configuration properties
         * @return this builder
         */
        public ExecContextBuilder props(final ConfigurationProperties props) {
            this.props = props;
            return this;
        }
    
        /**
         * set storage resource option.
         * @param storageResourceOption storage resource option 
         * @return this builder
         */
        public ExecContextBuilder storageResourceOption(final StorageResourceOption storageResourceOption) {
            this.storageResourceOption = storageResourceOption;
            return this;
        }
    
        /**
         * set transaction policy of this execution.
         * @param holdTransaction wheather hole transaction 
         * @return this builder
         */
        public ExecContextBuilder holdTransaction(final boolean holdTransaction) {
            this.holdTransaction = holdTransaction;
            return this;
        }
    
        /**
         * build current <code>ExecContext</code>.
         * @return <code>ExecContext</code> instance
         */
        public ExecContext build() {
            ExecContext execContext = new ExecContext(shardingRule, parameters, databaseType, executorJDBCManager);
            execContext.originSql = this.originSql;
            execContext.holdTransaction = this.holdTransaction;
            if (this.storageResourceOption == null) {
                execContext.option = new StatementOption(false);
            } else {
                execContext.option = this.storageResourceOption;
            }
            if (this.props == null) {
                execContext.props = new ConfigurationProperties(new Properties());
            } else {
                execContext.props = this.props;
            }
            return execContext;
        }
    
        /**
         * build the <code>ExecContext</code> builder.
         * @param shardingRule sharding rule, this parameter is required.
         * @param parameters parameters of {@link PreparedStatement}, this parameter is required.
         * @param databaseType database type of storage, this parameter is required.
         * @param executorJDBCManager Executor JDBC driver manager, this parameter is required.
         * @return builder of <code>ExecContext</code>.
         */
        public static ExecContextBuilder builder(final ShardingRule shardingRule, final List<Object> parameters, final DatabaseType databaseType, final ExecutorJDBCManager executorJDBCManager) {
            return new ExecContextBuilder(shardingRule, parameters, databaseType, executorJDBCManager);
        }
    }
}
