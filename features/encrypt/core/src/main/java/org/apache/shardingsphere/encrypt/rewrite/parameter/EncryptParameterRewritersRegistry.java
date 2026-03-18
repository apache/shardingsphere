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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.encrypt.rewrite.condition.EncryptCondition;
import org.apache.shardingsphere.encrypt.rewrite.parameter.rewriter.EncryptAssignmentParameterRewriter;
import org.apache.shardingsphere.encrypt.rewrite.parameter.rewriter.EncryptInsertOnDuplicateKeyUpdateValueParameterRewriter;
import org.apache.shardingsphere.encrypt.rewrite.parameter.rewriter.EncryptInsertPredicateParameterRewriter;
import org.apache.shardingsphere.encrypt.rewrite.parameter.rewriter.EncryptInsertValueParameterRewriter;
import org.apache.shardingsphere.encrypt.rewrite.parameter.rewriter.EncryptPredicateParameterRewriter;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.infra.rewrite.parameter.rewriter.ParameterRewriter;
import org.apache.shardingsphere.infra.rewrite.parameter.rewriter.ParameterRewritersRegistry;

import java.util.Arrays;
import java.util.Collection;

/**
 * Parameter rewriter registry for encrypt.
 */
@RequiredArgsConstructor
public final class EncryptParameterRewritersRegistry implements ParameterRewritersRegistry {
    
    private final EncryptRule rule;
    
    private final SQLRewriteContext sqlRewriteContext;
    
    private final Collection<EncryptCondition> encryptConditions;
    
    @Override
    public Collection<ParameterRewriter> getParameterRewriters() {
        String databaseName = sqlRewriteContext.getDatabase().getName();
        return Arrays.asList(
                new EncryptAssignmentParameterRewriter(rule, databaseName),
                new EncryptPredicateParameterRewriter(rule, databaseName, encryptConditions),
                new EncryptInsertPredicateParameterRewriter(rule, databaseName, encryptConditions),
                new EncryptInsertValueParameterRewriter(rule, databaseName),
                new EncryptInsertOnDuplicateKeyUpdateValueParameterRewriter(rule, databaseName));
    }
}
