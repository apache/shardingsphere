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

package org.apache.shardingsphere.encrypt.rewrite.token;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.encrypt.rewrite.aware.EncryptConditionsAware;
import org.apache.shardingsphere.encrypt.rewrite.aware.QueryWithCipherColumnAware;
import org.apache.shardingsphere.encrypt.rewrite.aware.SchemaNameAware;
import org.apache.shardingsphere.encrypt.rewrite.condition.EncryptCondition;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.AssistQueryAndPlainInsertColumnsTokenGenerator;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.EncryptAlterTableTokenGenerator;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.EncryptAssignmentTokenGenerator;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.EncryptCreateTableTokenGenerator;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.EncryptForUseDefaultInsertColumnsTokenGenerator;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.EncryptInsertOnUpdateTokenGenerator;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.EncryptInsertValuesTokenGenerator;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.EncryptOrderByItemTokenGenerator;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.EncryptPredicateColumnTokenGenerator;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.EncryptPredicateRightValueTokenGenerator;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.EncryptProjectionTokenGenerator;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.InsertCipherNameTokenGenerator;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.aware.EncryptRuleAware;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.SQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.builder.SQLTokenGeneratorBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * SQL token generator builder for encrypt.
 */
@RequiredArgsConstructor
public final class EncryptTokenGenerateBuilder implements SQLTokenGeneratorBuilder {
    
    private final EncryptRule encryptRule;
    
    private final SQLStatementContext<?> sqlStatementContext;
    
    private final Collection<EncryptCondition> encryptConditions;
    
    private final boolean containsEncryptTable;
    
    private final String schemaName;
    
    @Override
    public Collection<SQLTokenGenerator> getSQLTokenGenerators() {
        if (!containsEncryptTable) {
            return Collections.emptyList();
        }
        Collection<SQLTokenGenerator> result = new LinkedList<>();
        addSQLTokenGenerator(result, new EncryptProjectionTokenGenerator());
        addSQLTokenGenerator(result, new EncryptAssignmentTokenGenerator());
        addSQLTokenGenerator(result, new EncryptPredicateColumnTokenGenerator());
        addSQLTokenGenerator(result, new EncryptPredicateRightValueTokenGenerator());
        addSQLTokenGenerator(result, new EncryptInsertValuesTokenGenerator());
        addSQLTokenGenerator(result, new EncryptForUseDefaultInsertColumnsTokenGenerator());
        addSQLTokenGenerator(result, new InsertCipherNameTokenGenerator());
        addSQLTokenGenerator(result, new AssistQueryAndPlainInsertColumnsTokenGenerator());
        addSQLTokenGenerator(result, new EncryptInsertOnUpdateTokenGenerator());
        addSQLTokenGenerator(result, new EncryptCreateTableTokenGenerator());
        addSQLTokenGenerator(result, new EncryptAlterTableTokenGenerator());
        addSQLTokenGenerator(result, new EncryptOrderByItemTokenGenerator());
        return result;
    }
    
    private void addSQLTokenGenerator(final Collection<SQLTokenGenerator> sqlTokenGenerators, final SQLTokenGenerator toBeAddedSQLTokenGenerator) {
        if (toBeAddedSQLTokenGenerator instanceof EncryptRuleAware) {
            ((EncryptRuleAware) toBeAddedSQLTokenGenerator).setEncryptRule(encryptRule);
        }
        if (toBeAddedSQLTokenGenerator instanceof QueryWithCipherColumnAware) {
            ((QueryWithCipherColumnAware) toBeAddedSQLTokenGenerator).setQueryWithCipherColumn(encryptRule.isQueryWithCipherColumn());
        }
        if (toBeAddedSQLTokenGenerator instanceof EncryptConditionsAware) {
            ((EncryptConditionsAware) toBeAddedSQLTokenGenerator).setEncryptConditions(encryptConditions);
        }
        if (toBeAddedSQLTokenGenerator instanceof SchemaNameAware) {
            ((SchemaNameAware) toBeAddedSQLTokenGenerator).setSchemaName(schemaName);
        }
        if (toBeAddedSQLTokenGenerator.isGenerateSQLToken(sqlStatementContext)) {
            sqlTokenGenerators.add(toBeAddedSQLTokenGenerator);
        }
    }
}
