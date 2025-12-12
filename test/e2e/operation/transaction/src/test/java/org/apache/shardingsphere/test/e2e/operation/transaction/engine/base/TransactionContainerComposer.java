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

package org.apache.shardingsphere.test.e2e.operation.transaction.engine.base;

import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.env.runtime.type.ArtifactEnvironment.Adapter;
import org.apache.shardingsphere.test.e2e.operation.transaction.framework.container.compose.TransactionBaseContainerComposer;
import org.apache.shardingsphere.test.e2e.operation.transaction.framework.container.compose.TransactionDockerContainerComposer;
import org.apache.shardingsphere.test.e2e.operation.transaction.framework.param.TransactionTestParameter;

import javax.sql.DataSource;

/**
 * Transaction container composer.
 */
@Getter
public final class TransactionContainerComposer implements AutoCloseable {
    
    private final DatabaseType databaseType;
    
    private final TransactionBaseContainerComposer containerComposer;
    
    private final DataSource dataSource;
    
    public TransactionContainerComposer(final TransactionTestParameter testParam) {
        databaseType = testParam.getDatabaseType();
        containerComposer = initContainerComposer(testParam);
        dataSource = isProxyAdapter(testParam) ? createProxyDataSource() : createJdbcDataSource();
    }
    
    private TransactionBaseContainerComposer initContainerComposer(final TransactionTestParameter testParam) {
        TransactionDockerContainerComposer result = new TransactionDockerContainerComposer(testParam);
        result.start();
        return result;
    }
    
    private boolean isProxyAdapter(final TransactionTestParameter testParam) {
        return Adapter.PROXY.getValue().equalsIgnoreCase(testParam.getAdapter());
    }
    
    private DataSource createProxyDataSource() {
        TransactionDockerContainerComposer dockerContainerComposer = (TransactionDockerContainerComposer) containerComposer;
        return dockerContainerComposer.getProxyContainer().getTargetDataSource(null);
    }
    
    private DataSource createJdbcDataSource() {
        TransactionDockerContainerComposer dockerContainerComposer = (TransactionDockerContainerComposer) containerComposer;
        return dockerContainerComposer.getJdbcContainer().getTargetDataSource(null);
    }
    
    @Override
    public void close() {
        containerComposer.stop();
    }
}
