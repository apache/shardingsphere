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

package org.apache.shardingsphere.sqlfederation.compiler;

import org.apache.calcite.adapter.enumerable.EnumerableInterpretable;
import org.apache.calcite.adapter.enumerable.EnumerableRel;
import org.apache.calcite.adapter.enumerable.EnumerableRel.Prefer;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.runtime.Bindable;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class SQLFederationExecutionPlanTest {
    
    @Test
    void assertCreateStandardExecutionPlan() {
        RelNode physicalPlan = mock(RelNode.class);
        RelDataType resultColumnType = mock(RelDataType.class);
        SQLFederationExecutionPlan actual = new SQLFederationExecutionPlan(physicalPlan, resultColumnType);
        assertThat(actual.getPhysicalPlan(), is(physicalPlan));
        assertThat(actual.getResultColumnType(), is(resultColumnType));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertToBindable() {
        EnumerableRel physicalPlan = mock(EnumerableRel.class);
        Bindable<Object> expected = mock(Bindable.class);
        Map<String, Object> internalParameters = Collections.emptyMap();
        try (MockedStatic<EnumerableInterpretable> mockedInterpretable = mockStatic(EnumerableInterpretable.class)) {
            mockedInterpretable.when(() -> EnumerableInterpretable.toBindable(internalParameters, null, physicalPlan, Prefer.ARRAY)).thenReturn(expected);
            assertThat(SQLFederationExecutionPlan.toBindable(physicalPlan, internalParameters, null, Prefer.ARRAY), is(expected));
        }
    }
}
