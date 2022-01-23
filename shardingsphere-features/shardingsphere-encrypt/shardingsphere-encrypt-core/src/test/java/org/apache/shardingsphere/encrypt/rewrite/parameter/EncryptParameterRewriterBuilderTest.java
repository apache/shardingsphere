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

import org.apache.shardingsphere.encrypt.rewrite.parameter.impl.EncryptAssignmentParameterRewriter;
import org.apache.shardingsphere.encrypt.rewrite.parameter.impl.EncryptInsertOnDuplicateKeyUpdateValueParameterRewriter;
import org.apache.shardingsphere.encrypt.rewrite.parameter.impl.EncryptInsertValueParameterRewriter;
import org.apache.shardingsphere.encrypt.rewrite.parameter.impl.EncryptPredicateParameterRewriter;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.aware.EncryptRuleAware;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.parameter.rewriter.ParameterRewriter;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.aware.SchemaMetaDataAware;
import org.junit.Test;

import java.util.Collection;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class EncryptParameterRewriterBuilderTest {

    @Test
    public void assertGetParameterRewriters() {
        EncryptRule encryptRule = mock(EncryptRule.class);
        when(encryptRule.isQueryWithCipherColumn()).thenReturn(true);
        ShardingSphereSchema shardingSphereSchema = mock(ShardingSphereSchema.class);
        EncryptParameterRewriterBuilder encryptParameterRewriterBuilder = new EncryptParameterRewriterBuilder(encryptRule);
        Collection<ParameterRewriter> parameterRewriters = encryptParameterRewriterBuilder.getParameterRewriters(shardingSphereSchema);
        assertThat(parameterRewriters.size(), is(4));
        Object[] parameterRewriterArray = parameterRewriters.toArray();
        assertThat(parameterRewriterArray[0], instanceOf(EncryptAssignmentParameterRewriter.class));
        assertThat(parameterRewriterArray[1], instanceOf(EncryptPredicateParameterRewriter.class));
        assertThat(parameterRewriterArray[2], instanceOf(EncryptInsertValueParameterRewriter.class));
        assertThat(parameterRewriterArray[3], instanceOf(EncryptInsertOnDuplicateKeyUpdateValueParameterRewriter.class));
        assertThat(parameterRewriterArray[0], instanceOf(EncryptRuleAware.class));
        assertThat(parameterRewriterArray[1], instanceOf(SchemaMetaDataAware.class));
        assertThat(parameterRewriterArray[2], instanceOf(EncryptRuleAware.class));
        assertThat(parameterRewriterArray[3], instanceOf(EncryptRuleAware.class));
    }
}
