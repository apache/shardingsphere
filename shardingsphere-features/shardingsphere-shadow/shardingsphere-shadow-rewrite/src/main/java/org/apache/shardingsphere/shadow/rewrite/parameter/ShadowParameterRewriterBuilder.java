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

package org.apache.shardingsphere.shadow.rewrite.parameter;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.rewrite.aware.ShadowRuleAware;
import org.apache.shardingsphere.shadow.rewrite.parameter.impl.ShadowInsertValueParameterRewriter;
import org.apache.shardingsphere.shadow.rewrite.parameter.impl.ShadowPredicateParameterRewriter;
import org.apache.shardingsphere.infra.schema.model.schema.physical.model.schema.PhysicalSchemaMetaData;
import org.apache.shardingsphere.infra.rewrite.parameter.rewriter.ParameterRewriter;
import org.apache.shardingsphere.infra.rewrite.parameter.rewriter.ParameterRewriterBuilder;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Parameter rewriter builder for shadow.
 */
@RequiredArgsConstructor
public final class ShadowParameterRewriterBuilder implements ParameterRewriterBuilder {
    
    private final ShadowRule shadowRule;
    
    @Override
    public Collection<ParameterRewriter> getParameterRewriters(final PhysicalSchemaMetaData schemaMetaData) {
        Collection<ParameterRewriter> result = getParameterRewriters();
        for (ParameterRewriter each : result) {
            ((ShadowRuleAware) each).setShadowRule(shadowRule);
        }
        return result;
    }
    
    private Collection<ParameterRewriter> getParameterRewriters() {
        Collection<ParameterRewriter> result = new LinkedList<>();
        result.add(new ShadowPredicateParameterRewriter());
        result.add(new ShadowInsertValueParameterRewriter());
        return result;
    }
}
