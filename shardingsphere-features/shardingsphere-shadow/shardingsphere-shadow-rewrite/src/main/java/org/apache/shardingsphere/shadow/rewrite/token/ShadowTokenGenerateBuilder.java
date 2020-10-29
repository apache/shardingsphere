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

package org.apache.shardingsphere.shadow.rewrite.token;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.rewrite.aware.ShadowRuleAware;
import org.apache.shardingsphere.shadow.rewrite.token.generator.impl.RemoveShadowColumnTokenGenerator;
import org.apache.shardingsphere.shadow.rewrite.token.generator.impl.ShadowInsertValuesTokenGenerator;
import org.apache.shardingsphere.shadow.rewrite.token.generator.impl.ShadowPredicateColumnTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.SQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.builder.SQLTokenGeneratorBuilder;

import java.util.Collection;
import java.util.LinkedList;

/**
 * SQL token generator builder for shadow.
 */
@RequiredArgsConstructor
public final class ShadowTokenGenerateBuilder implements SQLTokenGeneratorBuilder {

    private final ShadowRule shadowRule;

    @Override
    public Collection<SQLTokenGenerator> getSQLTokenGenerators() {
        Collection<SQLTokenGenerator> result = buildSQLTokenGenerators();
        for (SQLTokenGenerator each : result) {
            ((ShadowRuleAware) each).setShadowRule(shadowRule);
        }
        return result;
    }

    private Collection<SQLTokenGenerator> buildSQLTokenGenerators() {
        Collection<SQLTokenGenerator> result = new LinkedList<>();
        result.add(new ShadowInsertValuesTokenGenerator());
        result.add(new RemoveShadowColumnTokenGenerator());
        result.add(new ShadowPredicateColumnTokenGenerator());
        return result;
    }
}
