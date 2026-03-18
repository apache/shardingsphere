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

package org.apache.shardingsphere.sqlfederation.executor.context;

import org.apache.calcite.adapter.java.JavaTypeFactory;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.shardingsphere.sqlfederation.compiler.rel.converter.SQLFederationRelConverter;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExecutorBindContextTest {
    
    @Test
    void assertGetRootSchema() {
        SchemaPlus expectedSchema = mock(SchemaPlus.class);
        ExecutorBindContext context = new ExecutorBindContext(createConverter(expectedSchema, null), Collections.emptyMap());
        assertThat(context.getRootSchema(), is(expectedSchema));
    }
    
    @Test
    void assertGetTypeFactory() {
        JavaTypeFactory typeFactory = mock(JavaTypeFactory.class, RETURNS_DEEP_STUBS);
        ExecutorBindContext context = new ExecutorBindContext(createConverter(null, typeFactory), Collections.emptyMap());
        assertThat(context.getTypeFactory(), is(typeFactory));
    }
    
    @Test
    void assertGetQueryProvider() {
        ExecutorBindContext context = new ExecutorBindContext(createConverter(null, null), Collections.emptyMap());
        assertNull(context.getQueryProvider());
    }
    
    @Test
    void assertGet() {
        ExecutorBindContext context = new ExecutorBindContext(createConverter(null, null), Collections.singletonMap("foo_param", 10));
        assertThat(context.get("foo_param"), is(10));
        assertThat(context.getParameters(), is(Collections.singletonMap("foo_param", 10)));
    }
    
    private SQLFederationRelConverter createConverter(final SchemaPlus schemaPlus, final JavaTypeFactory typeFactory) {
        SQLFederationRelConverter result = mock(SQLFederationRelConverter.class);
        when(result.getSchemaPlus()).thenReturn(schemaPlus);
        RelOptCluster cluster = mock(RelOptCluster.class);
        when(result.getCluster()).thenReturn(cluster);
        when(cluster.getTypeFactory()).thenReturn(typeFactory);
        return result;
    }
}
