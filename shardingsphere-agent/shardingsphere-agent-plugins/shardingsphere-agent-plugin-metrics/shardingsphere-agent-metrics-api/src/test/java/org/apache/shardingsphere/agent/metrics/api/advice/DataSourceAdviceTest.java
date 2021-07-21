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

package org.apache.shardingsphere.agent.metrics.api.advice;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.agent.api.result.MethodInvocationResult;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Method;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DataSourceAdviceTest {
    
    @Mock
    private MethodInvocationResult result;
    
    @Mock
    private Method decorate;
    
    private final DataSourceAdvice dataSourceAdvice = new DataSourceAdvice();
    
    @Test
    public void assertNotNull() {
        HikariDataSource hikariDataSource = new HikariDataSource();
        when(result.getResult()).thenReturn(hikariDataSource);
        dataSourceAdvice.afterMethod(String.class, decorate, new Object[]{}, result);
        Assert.assertNotNull(hikariDataSource.getMetricsTrackerFactory());
    }
}
