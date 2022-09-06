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

package org.apache.shardingsphere.integration.transaction.engine.opengauss;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.integration.transaction.engine.base.BaseTransactionITCase;
import org.apache.shardingsphere.integration.transaction.engine.constants.TransactionTestConstants;
import org.apache.shardingsphere.integration.transaction.framework.param.TransactionParameterized;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Objects;

/**
 * OpenGauss general transaction test case with proxy container, includes multiple cases.
 */
@Slf4j
@RunWith(Parameterized.class)
public final class OpenGaussProxyTransactionIT extends BaseTransactionITCase {
    
    private final TransactionParameterized parameterized;
    
    public OpenGaussProxyTransactionIT(final TransactionParameterized parameterized) throws SQLException {
        super(parameterized);
        this.parameterized = parameterized;
        log.info("Parameterized:{}", parameterized);
    }
    
    @Parameters(name = "{0}")
    public static Collection<TransactionParameterized> getParameters() {
        return getTransactionParameterizedList(OpenGaussProxyTransactionIT.class);
    }
    
    @Before
    @SneakyThrows(SQLException.class)
    public void before() {
        if (TransactionTestConstants.PROXY.equalsIgnoreCase(parameterized.getAdapter())) {
            if (Objects.equals(parameterized.getTransactionType(), TransactionType.LOCAL)) {
                alterLocalTransactionRule();
            } else if (Objects.equals(parameterized.getTransactionType(), TransactionType.XA)) {
                alterXaTransactionRule(parameterized.getProvider());
            }
        }
    }
    
    @After
    @SneakyThrows(SQLException.class)
    public void after() {
        getDataSource().close();
        getContainerComposer().close();
    }
    
    @Test
    public void assertTransaction() {
        callTestCases(parameterized);
    }
}
