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

package org.apache.shardingsphere.test.e2e.transaction.engine.base;

import lombok.Getter;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.env.container.atomic.enums.AdapterType;
import org.apache.shardingsphere.test.e2e.transaction.env.TransactionE2EEnvironment;
import org.apache.shardingsphere.test.e2e.transaction.env.enums.TransactionE2EEnvTypeEnum;
import org.apache.shardingsphere.test.e2e.transaction.framework.container.compose.BaseContainerComposer;
import org.apache.shardingsphere.test.e2e.transaction.framework.container.compose.DockerContainerComposer;
import org.apache.shardingsphere.test.e2e.transaction.framework.container.compose.NativeContainerComposer;
import org.apache.shardingsphere.test.e2e.transaction.framework.param.TransactionTestParameter;

import javax.sql.DataSource;

/**
 * Transaction container composer.
 */
@Getter
public final class TransactionContainerComposer implements AutoCloseable {
    
    private static final TransactionE2EEnvironment ENV = TransactionE2EEnvironment.getInstance();
    
    private final DatabaseType databaseType;
    
    private final BaseContainerComposer containerComposer;
    
    private final DataSource dataSource;
    
    public TransactionContainerComposer(final TransactionTestParameter testParam) {
        databaseType = testParam.getDatabaseType();
        containerComposer = initContainerComposer(testParam);
        dataSource = isProxyAdapter(testParam) ? createProxyDataSource() : createJdbcDataSource();
    }
    
    private BaseContainerComposer initContainerComposer(final TransactionTestParameter testParam) {
        BaseContainerComposer result = ENV.getItEnvType() == TransactionE2EEnvTypeEnum.DOCKER ? new DockerContainerComposer(testParam) : new NativeContainerComposer();
        result.start();
        return result;
    }
    
    private boolean isProxyAdapter(final TransactionTestParameter testParam) {
        return AdapterType.PROXY.getValue().equalsIgnoreCase(testParam.getAdapter());
    }
    
    private DataSource createProxyDataSource() {
        DockerContainerComposer dockerContainerComposer = (DockerContainerComposer) containerComposer;
        return dockerContainerComposer.getProxyContainer().getTargetDataSource(null);
    }
    
    private DataSource createJdbcDataSource() {
        DockerContainerComposer dockerContainerComposer = (DockerContainerComposer) containerComposer;
        return dockerContainerComposer.getJdbcContainer().getTargetDataSource();
    }
    
    @Override
    public void close() {
        containerComposer.stop();
    }
}
