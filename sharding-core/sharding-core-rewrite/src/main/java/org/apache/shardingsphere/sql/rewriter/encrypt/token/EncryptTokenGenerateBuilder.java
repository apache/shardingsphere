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

package org.apache.shardingsphere.sql.rewriter.encrypt.token;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.sql.rewriter.encrypt.aware.EncryptRuleAware;
import org.apache.shardingsphere.sql.rewriter.encrypt.aware.QueryWithCipherColumnAware;
import org.apache.shardingsphere.sql.rewriter.encrypt.token.generator.impl.AssistQueryAndPlainInsertColumnsTokenGenerator;
import org.apache.shardingsphere.sql.rewriter.encrypt.token.generator.impl.EncryptAssignmentTokenGenerator;
import org.apache.shardingsphere.sql.rewriter.encrypt.token.generator.impl.EncryptForUseDefaultInsertColumnsTokenGenerator;
import org.apache.shardingsphere.sql.rewriter.encrypt.token.generator.impl.EncryptInsertValuesTokenGenerator;
import org.apache.shardingsphere.sql.rewriter.encrypt.token.generator.impl.EncryptPredicateColumnTokenGenerator;
import org.apache.shardingsphere.sql.rewriter.encrypt.token.generator.impl.EncryptPredicateRightValueTokenGenerator;
import org.apache.shardingsphere.sql.rewriter.encrypt.token.generator.impl.EncryptProjectionTokenGenerator;
import org.apache.shardingsphere.sql.rewriter.encrypt.token.generator.impl.InsertCipherNameTokenGenerator;
import org.apache.shardingsphere.sql.rewriter.sql.token.generator.SQLTokenGenerator;
import org.apache.shardingsphere.sql.rewriter.sql.token.generator.builder.SQLTokenGeneratorBuilder;

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
        result.add(new EncryptProjectionTokenGenerator());
        result.add(new EncryptAssignmentTokenGenerator());
        result.add(new EncryptPredicateColumnTokenGenerator());
        result.add(new EncryptPredicateRightValueTokenGenerator());
        result.add(new EncryptInsertValuesTokenGenerator());
        result.add(new EncryptForUseDefaultInsertColumnsTokenGenerator());
        result.add(new InsertCipherNameTokenGenerator());
        result.add(new AssistQueryAndPlainInsertColumnsTokenGenerator());
        return result;
    }
}
