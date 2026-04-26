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

package org.apache.shardingsphere.test.e2e.mcp.support.fixture;

import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithmMetaData;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.context.AlgorithmSQLContext;

import java.util.Objects;
import java.util.Properties;

/**
 * Custom encrypt algorithm fixture for MCP workflow E2E tests.
 */
public final class MCPWorkflowCustomEncryptAlgorithmFixture implements EncryptAlgorithm {
    
    private static final String PREFIX = "mcp_custom:";
    
    private static final EncryptAlgorithmMetaData META_DATA = new EncryptAlgorithmMetaData(true, false, false);
    
    @Override
    public Object encrypt(final Object plainValue, final AlgorithmSQLContext algorithmSQLContext) {
        return PREFIX + Objects.toString(plainValue, "");
    }
    
    @Override
    public Object decrypt(final Object cipherValue, final AlgorithmSQLContext algorithmSQLContext) {
        String actualCipherValue = Objects.toString(cipherValue, "");
        return actualCipherValue.startsWith(PREFIX) ? actualCipherValue.substring(PREFIX.length()) : actualCipherValue;
    }
    
    @Override
    public EncryptAlgorithmMetaData getMetaData() {
        return META_DATA;
    }
    
    @Override
    public AlgorithmConfiguration toConfiguration() {
        return new AlgorithmConfiguration(getType(), new Properties());
    }
    
    @Override
    public String getType() {
        return "MCP_CUSTOM_REVERSIBLE";
    }
}
