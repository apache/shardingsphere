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

package org.apache.shardingsphere.infra.rewrite.engine;

import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.infra.rewrite.engine.result.GenericSQLRewriteResult;
import org.apache.shardingsphere.infra.metadata.schema.model.physical.PhysicalSchemaMetaData;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class GenericSQLRewriteEngineTest {
    
    @Test
    public void assertRewrite() {
        GenericSQLRewriteResult actual = new GenericSQLRewriteEngine().rewrite(
                new SQLRewriteContext(mock(PhysicalSchemaMetaData.class), mock(SQLStatementContext.class), "SELECT 1", Collections.emptyList()));
        assertThat(actual.getSqlRewriteUnit().getSql(), is("SELECT 1"));
        assertThat(actual.getSqlRewriteUnit().getParameters(), is(Collections.emptyList()));
    }
}
