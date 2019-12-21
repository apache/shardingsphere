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
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.sql.parser.relation.metadata.RelationMetas;
import org.apache.shardingsphere.core.rule.aware.EncryptRuleAware;
import org.apache.shardingsphere.encrypt.rewrite.aware.QueryWithCipherColumnAware;
import org.apache.shardingsphere.encrypt.rewrite.parameter.impl.EncryptAssignmentParameterRewriter;
import org.apache.shardingsphere.encrypt.rewrite.parameter.impl.EncryptInsertValueParameterRewriter;
import org.apache.shardingsphere.encrypt.rewrite.parameter.impl.EncryptPredicateParameterRewriter;
import org.apache.shardingsphere.underlying.rewrite.parameter.rewriter.ParameterRewriter;
import org.apache.shardingsphere.underlying.rewrite.parameter.rewriter.ParameterRewriterBuilder;
import org.apache.shardingsphere.underlying.rewrite.sql.token.generator.aware.RelationMetasAware;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Parameter rewriter builder for encrypt.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class EncryptParameterRewriterBuilder implements ParameterRewriterBuilder {
    
    private final EncryptRule encryptRule;
    
    private final boolean queryWithCipherColumn;
    
    @Override
    public Collection<ParameterRewriter> getParameterRewriters(final RelationMetas relationMetas) {
        Collection<ParameterRewriter> result = getParameterRewriters();
        for (ParameterRewriter each : result) {
            setUpParameterRewriters(each, relationMetas);
        }
        return result;
    }
    
    private Collection<ParameterRewriter> getParameterRewriters() {
        Collection<ParameterRewriter> result = new LinkedList<>();
        result.add(new EncryptAssignmentParameterRewriter());
        result.add(new EncryptPredicateParameterRewriter());
        result.add(new EncryptInsertValueParameterRewriter());
        return result;
    }
    
    private void setUpParameterRewriters(final ParameterRewriter parameterRewriter, final RelationMetas relationMetas) {
        if (parameterRewriter instanceof RelationMetasAware) {
            ((RelationMetasAware) parameterRewriter).setRelationMetas(relationMetas);
        }
        if (parameterRewriter instanceof EncryptRuleAware) {
            ((EncryptRuleAware) parameterRewriter).setEncryptRule(encryptRule);
        }
        if (parameterRewriter instanceof QueryWithCipherColumnAware) {
            ((QueryWithCipherColumnAware) parameterRewriter).setQueryWithCipherColumn(queryWithCipherColumn);
        }
    }
}
