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
import org.apache.shardingsphere.encrypt.rewrite.condition.EncryptCondition;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.assignment.EncryptInsertAssignmentTokenGenerator;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.assignment.EncryptUpdateAssignmentTokenGenerator;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.ddl.EncryptAlterTableTokenGenerator;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.ddl.EncryptCreateTableTokenGenerator;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.insert.EncryptInsertCipherNameTokenGenerator;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.insert.EncryptInsertDefaultColumnsTokenGenerator;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.insert.EncryptInsertDerivedColumnsTokenGenerator;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.insert.EncryptInsertOnUpdateTokenGenerator;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.insert.EncryptInsertValuesTokenGenerator;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.predicate.EncryptInsertPredicateColumnTokenGenerator;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.predicate.EncryptInsertPredicateValueTokenGenerator;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.predicate.EncryptPredicateColumnTokenGenerator;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.predicate.EncryptPredicateValueTokenGenerator;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.projection.EncryptInsertSelectProjectionTokenGenerator;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.projection.EncryptSelectProjectionTokenGenerator;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.select.EncryptGroupByItemTokenGenerator;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.select.EncryptIndexColumnTokenGenerator;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.SQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.builder.SQLTokenGeneratorBuilder;

import java.util.Collection;
import java.util.LinkedList;

/**
 * SQL token generator builder for encrypt.
 */
@RequiredArgsConstructor
public final class EncryptTokenGenerateBuilder implements SQLTokenGeneratorBuilder {
    
    private final SQLStatementContext sqlStatementContext;
    
    private final Collection<EncryptCondition> encryptConditions;
    
    private final EncryptRule rule;
    
    private final SQLRewriteContext sqlRewriteContext;
    
    @Override
    public Collection<SQLTokenGenerator> getSQLTokenGenerators() {
        Collection<SQLTokenGenerator> result = new LinkedList<>();
        ShardingSphereDatabase database = sqlRewriteContext.getDatabase();
        addSQLTokenGenerator(result, new EncryptPredicateValueTokenGenerator(rule, database));
        addSQLTokenGenerator(result, new EncryptInsertPredicateValueTokenGenerator(rule, database));
        addSQLTokenGenerator(result, new EncryptCreateTableTokenGenerator(rule));
        addSQLTokenGenerator(result, new EncryptAlterTableTokenGenerator(rule));
        addSQLTokenGenerator(result, new EncryptSelectProjectionTokenGenerator(rule));
        addSQLTokenGenerator(result, new EncryptInsertSelectProjectionTokenGenerator(rule));
        addSQLTokenGenerator(result, new EncryptInsertAssignmentTokenGenerator(rule, database));
        addSQLTokenGenerator(result, new EncryptUpdateAssignmentTokenGenerator(rule, database));
        addSQLTokenGenerator(result, new EncryptPredicateColumnTokenGenerator(rule));
        addSQLTokenGenerator(result, new EncryptInsertPredicateColumnTokenGenerator(rule));
        addSQLTokenGenerator(result, new EncryptInsertValuesTokenGenerator(rule, database));
        addSQLTokenGenerator(result, new EncryptInsertDefaultColumnsTokenGenerator(rule));
        addSQLTokenGenerator(result, new EncryptInsertCipherNameTokenGenerator(rule));
        addSQLTokenGenerator(result, new EncryptInsertDerivedColumnsTokenGenerator(rule));
        addSQLTokenGenerator(result, new EncryptInsertOnUpdateTokenGenerator(rule, database));
        addSQLTokenGenerator(result, new EncryptGroupByItemTokenGenerator(rule));
        addSQLTokenGenerator(result, new EncryptIndexColumnTokenGenerator(rule));
        return result;
    }
    
    private void addSQLTokenGenerator(final Collection<SQLTokenGenerator> sqlTokenGenerators, final SQLTokenGenerator toBeAddedSQLTokenGenerator) {
        setUpSQLTokenGenerator(toBeAddedSQLTokenGenerator);
        if (toBeAddedSQLTokenGenerator.isGenerateSQLToken(sqlStatementContext)) {
            sqlTokenGenerators.add(toBeAddedSQLTokenGenerator);
        }
    }
    
    private void setUpSQLTokenGenerator(final SQLTokenGenerator toBeAddedSQLTokenGenerator) {
        if (toBeAddedSQLTokenGenerator instanceof EncryptConditionsAware) {
            ((EncryptConditionsAware) toBeAddedSQLTokenGenerator).setEncryptConditions(encryptConditions);
        }
    }
}
