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

package org.apache.shardingsphere.mask.merge.dql;

import org.apache.shardingsphere.mask.rule.MaskRule;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MaskDQLResultDecoratorTest {
    
    @Test
    void assertDecorateQueryResult() throws SQLException {
        QueryResult queryResult = mock(QueryResult.class);
        when(queryResult.next()).thenReturn(true);
        MaskDQLResultDecorator decorator = new MaskDQLResultDecorator(mock(MaskAlgorithmMetaData.class));
        MergedResult actual = decorator.decorate(queryResult, mock(SQLStatementContext.class), mock(MaskRule.class));
        assertTrue(actual.next());
    }
    
    @Test
    void assertDecorateMergedResult() throws SQLException {
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.next()).thenReturn(true);
        MaskDQLResultDecorator decorator = new MaskDQLResultDecorator(mock(MaskAlgorithmMetaData.class));
        MergedResult actual = decorator.decorate(mergedResult, mock(SQLStatementContext.class), mock(MaskRule.class));
        assertTrue(actual.next());
    }
}
