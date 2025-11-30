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

package org.apache.shardingsphere.agent.plugin.tracing.opentelemetry.fixture;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.agent.api.advice.TargetAdviceObject;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

@Getter
@Setter
public final class JDBCExecutorCallbackFixture extends JDBCExecutorCallback<Object> implements TargetAdviceObject {
    
    private Object attachment;
    
    public JDBCExecutorCallbackFixture(final DatabaseType protocolType, final ResourceMetaData resourceMetaData, final SQLStatement sqlStatement, final boolean isExceptionThrown) {
        super(protocolType, resourceMetaData, sqlStatement, isExceptionThrown);
    }
    
    @Override
    protected Object executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode, final DatabaseType storageType) {
        return null;
    }
    
    @Override
    protected Optional<Object> getSaneResult(final SQLStatement sqlStatement, final SQLException ex) {
        return Optional.empty();
    }
}
