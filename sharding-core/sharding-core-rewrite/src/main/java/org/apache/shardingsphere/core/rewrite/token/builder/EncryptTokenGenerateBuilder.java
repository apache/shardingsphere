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

package org.apache.shardingsphere.core.rewrite.token.builder;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.rewrite.token.generator.EncryptRuleAware;
import org.apache.shardingsphere.core.rewrite.token.generator.QueryWithCipherColumnAware;
import org.apache.shardingsphere.core.rewrite.token.generator.SQLTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.generator.collection.impl.InsertCipherNameTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.generator.collection.impl.InsertSetCipherColumnTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.generator.collection.impl.SelectEncryptItemTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.generator.collection.impl.EncryptAssignmentTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.generator.collection.impl.EncryptPredicateTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.generator.optional.impl.InsertQueryAndPlainNamesTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.generator.optional.impl.InsertEncryptColumnFromMetadataTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.generator.optional.impl.InsertSetQueryAndPlainColumnsTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.generator.optional.impl.InsertValuesTokenGenerator;
import org.apache.shardingsphere.core.rule.EncryptRule;

import java.util.Collection;
import java.util.LinkedList;

/**
 * SQL token generator builder for encrypt.
 *
 * @author panjuan
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class EncryptTokenGenerateBuilder implements SQLTokenGeneratorBuilder {
    
    private final EncryptRule encryptRule;
    
    private final boolean queryWithCipherColumn;
    
    @Override
    public Collection<SQLTokenGenerator> getSQLTokenGenerators() {
        Collection<SQLTokenGenerator> result = buildSQLTokenGenerators();
        for (SQLTokenGenerator each : result) {
            if (each instanceof EncryptRuleAware) {
                ((EncryptRuleAware) each).setEncryptRule(encryptRule);
            }
            if (each instanceof QueryWithCipherColumnAware) {
                ((QueryWithCipherColumnAware) each).setQueryWithCipherColumn(queryWithCipherColumn);
            }
        }
        return result;
    }
    
    private Collection<SQLTokenGenerator> buildSQLTokenGenerators() {
        Collection<SQLTokenGenerator> result = new LinkedList<>();
        result.add(new InsertEncryptColumnFromMetadataTokenGenerator());
        result.add(new SelectEncryptItemTokenGenerator());
        result.add(new EncryptAssignmentTokenGenerator());
        result.add(new EncryptPredicateTokenGenerator());
        result.add(new InsertCipherNameTokenGenerator());
        result.add(new InsertQueryAndPlainNamesTokenGenerator());
        result.add(new InsertSetCipherColumnTokenGenerator());
        result.add(new InsertSetQueryAndPlainColumnsTokenGenerator());
        result.add(new InsertValuesTokenGenerator());
        return result;
    }
}
