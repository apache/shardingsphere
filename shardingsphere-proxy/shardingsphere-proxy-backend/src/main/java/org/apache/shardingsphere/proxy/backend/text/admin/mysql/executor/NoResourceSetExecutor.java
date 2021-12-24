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

package org.apache.shardingsphere.proxy.backend.text.admin.mysql.executor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.data.DatabaseBackendHandler;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.EmptyStatement;

import java.sql.SQLException;
import java.util.Collection;

/**
 * No Resource set executor.
 */
@Getter
@RequiredArgsConstructor
public final class NoResourceSetExecutor implements DatabaseBackendHandler {
    
    private final SetStatement sqlStatement;
    
    @Override
    public ResponseHeader execute() throws SQLException {
        return new UpdateResponseHeader(new EmptyStatement());
    }
    
    @Override
    public boolean next() throws SQLException {
        return DatabaseBackendHandler.super.next();
    }
    
    @Override
    public Collection<Object> getRowData() throws SQLException {
        return DatabaseBackendHandler.super.getRowData();
    }
    
    @Override
    public void close() throws SQLException {
        DatabaseBackendHandler.super.close();
    }
}
