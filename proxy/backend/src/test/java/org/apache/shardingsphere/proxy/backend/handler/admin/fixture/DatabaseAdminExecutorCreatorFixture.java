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

package org.apache.shardingsphere.proxy.backend.handler.admin.fixture;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutorCreator;
import org.apache.shardingsphere.test.fixture.database.type.MockedDatabaseType;

import java.util.Collection;
import java.util.Optional;

public final class DatabaseAdminExecutorCreatorFixture implements DatabaseAdminExecutorCreator {
    
    @Override
    public Optional<DatabaseAdminExecutor> create(final SQLStatementContext<?> sqlStatementContext) {
        return Optional.empty();
    }
    
    @Override
    public Optional<DatabaseAdminExecutor> create(final SQLStatementContext<?> sqlStatementContext, final String sql, final String databaseName) {
        return Optional.empty();
    }
    
    @Override
    public String getType() {
        return new MockedDatabaseType().getType();
    }
    
    @Override
    public Collection<String> getTypeAliases() {
        return DatabaseAdminExecutorCreator.super.getTypeAliases();
    }
}
