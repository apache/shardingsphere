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

package org.apache.shardingsphere.infra.binder.engine.segment.expression.type;

import org.apache.shardingsphere.infra.binder.engine.segment.SegmentType;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.test.fixture.database.MockedDatabaseType;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class FunctionExpressionSegmentBinderTest {
    
    @Test
    void assertBindFunctionExpressionSegment() {
        FunctionSegment functionSegment = new FunctionSegment(0, 0, "CONCAT", "('%','abc','%')");
        SQLStatementBinderContext binderContext = new SQLStatementBinderContext(new ShardingSphereMetaData(), DefaultDatabase.LOGIC_NAME, new MockedDatabaseType(), Collections.emptyList());
        FunctionSegment actual = FunctionExpressionSegmentBinder.bind(functionSegment, SegmentType.PROJECTION, binderContext, Collections.emptyMap(), Collections.emptyMap());
        assertThat(actual.getStartIndex(), is(functionSegment.getStartIndex()));
        assertThat(actual.getStopIndex(), is(functionSegment.getStopIndex()));
        assertThat(actual.getFunctionName(), is("CONCAT"));
        assertThat(actual.getText(), is("('%','abc','%')"));
    }
}
