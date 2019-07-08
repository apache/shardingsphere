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

package org.apache.shardingsphere.core.rewrite.builder;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.Pagination;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.ShardingSelectOptimizedStatement;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.route.type.RoutingResult;
import org.hamcrest.core.AnyOf;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class BaseParameterBuilderTest {
    
    private BaseParameterBuilder baseParameterBuilder;
    
    @Before
    public void setUp() {
        baseParameterBuilder = new BaseParameterBuilder()
    }
    
    private SQLRouteResult createSQLRouteResult() {
        Pagination pagination = mock(Pagination.class);
        when(pagination.getOffsetParameterIndex()).thenReturn(Optional.of(0));
        when(pagination.getRowCountParameterIndex()).thenReturn(Optional.of(1));
        when(pagination.getRevisedRowCount(any(ShardingSelectOptimizedStatement.class))).thenReturn(6);
        ShardingSelectOptimizedStatement optimizedStatement = mock(ShardingSelectOptimizedStatement.class);
        when(optimizedStatement.getPagination()).thenReturn(pagination);
        SQLRouteResult result = new SQLRouteResult(optimizedStatement);
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertGetParameters() {
    }
    
    @Test
    public void assertGetParameters1() {
    }
    
    @Test
    public void assertGetOriginalParameters() {
    }
    
    @Test
    public void assertGetAddedIndexAndParameters() {
    }
    
    @Test
    public void assertGetReplacedIndexAndParameters() {
    }
}
