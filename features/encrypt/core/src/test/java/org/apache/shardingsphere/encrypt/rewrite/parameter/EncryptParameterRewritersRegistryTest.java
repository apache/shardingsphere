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

package org.apache.shardingsphere.encrypt.rewrite.parameter;

import org.apache.shardingsphere.encrypt.rewrite.parameter.rewriter.EncryptAssignmentParameterRewriter;
import org.apache.shardingsphere.encrypt.rewrite.parameter.rewriter.EncryptInsertOnDuplicateKeyUpdateValueParameterRewriter;
import org.apache.shardingsphere.encrypt.rewrite.parameter.rewriter.EncryptInsertPredicateParameterRewriter;
import org.apache.shardingsphere.encrypt.rewrite.parameter.rewriter.EncryptInsertValueParameterRewriter;
import org.apache.shardingsphere.encrypt.rewrite.parameter.rewriter.EncryptPredicateParameterRewriter;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.infra.rewrite.parameter.rewriter.ParameterRewriter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EncryptParameterRewritersRegistryTest {
    
    @Test
    void assertGetParameterRewriters() {
        SQLRewriteContext sqlRewriteContext = mock(SQLRewriteContext.class, RETURNS_DEEP_STUBS);
        when(sqlRewriteContext.getDatabase().getName()).thenReturn("foo_db");
        List<ParameterRewriter> actual = new ArrayList<>(new EncryptParameterRewritersRegistry(mock(EncryptRule.class), sqlRewriteContext, Collections.emptyList()).getParameterRewriters());
        assertThat(actual.size(), is(5));
        assertThat(actual.get(0), isA(EncryptAssignmentParameterRewriter.class));
        assertThat(actual.get(1), isA(EncryptPredicateParameterRewriter.class));
        assertThat(actual.get(2), isA(EncryptInsertPredicateParameterRewriter.class));
        assertThat(actual.get(3), isA(EncryptInsertValueParameterRewriter.class));
        assertThat(actual.get(4), isA(EncryptInsertOnDuplicateKeyUpdateValueParameterRewriter.class));
    }
}
