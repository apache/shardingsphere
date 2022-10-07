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
import org.apache.shardingsphere.encrypt.rewrite.aware.DatabaseNameAware;
import org.apache.shardingsphere.encrypt.rewrite.aware.EncryptConditionsAware;
import org.apache.shardingsphere.encrypt.rewrite.condition.EncryptCondition;
import org.apache.shardingsphere.encrypt.rewrite.parameter.rewriter.EncryptAssignmentParameterRewriter;
import org.apache.shardingsphere.encrypt.rewrite.parameter.rewriter.EncryptInsertOnDuplicateKeyUpdateValueParameterRewriter;
import org.apache.shardingsphere.encrypt.rewrite.parameter.rewriter.EncryptInsertValueParameterRewriter;
import org.apache.shardingsphere.encrypt.rewrite.parameter.rewriter.EncryptPredicateParameterRewriter;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.aware.EncryptRuleAware;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.parameter.rewriter.ParameterRewriter;
import org.apache.shardingsphere.infra.rewrite.parameter.rewriter.ParameterRewriterBuilder;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.aware.SchemaMetaDataAware;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/**
 * Parameter rewriter builder for encrypt.
 */
@RequiredArgsConstructor
public final class EncryptParameterRewriterBuilder implements ParameterRewriterBuilder {
    
    private final EncryptRule encryptRule;
    
    private final String databaseName;
    
    private final Map<String, ShardingSphereSchema> schemas;
    
    private final SQLStatementContext<?> sqlStatementContext;
    
    private final Collection<EncryptCondition> encryptConditions;
    
    @SuppressWarnings("rawtypes")
    @Override
    public Collection<ParameterRewriter> getParameterRewriters() {
        Collection<ParameterRewriter> result = new LinkedList<>();
        addParameterRewriter(result, new EncryptAssignmentParameterRewriter());
        addParameterRewriter(result, new EncryptPredicateParameterRewriter());
        addParameterRewriter(result, new EncryptInsertValueParameterRewriter());
        addParameterRewriter(result, new EncryptInsertOnDuplicateKeyUpdateValueParameterRewriter());
        return result;
    }
    
    private void addParameterRewriter(final Collection<ParameterRewriter> parameterRewriters, final ParameterRewriter<?> toBeAddedParameterRewriter) {
        if (toBeAddedParameterRewriter.isNeedRewrite(sqlStatementContext)) {
            setUpParameterRewriter(toBeAddedParameterRewriter);
            parameterRewriters.add(toBeAddedParameterRewriter);
        }
    }
    
    @SuppressWarnings("rawtypes")
    private void setUpParameterRewriter(final ParameterRewriter toBeAddedParameterRewriter) {
        if (toBeAddedParameterRewriter instanceof SchemaMetaDataAware) {
            ((SchemaMetaDataAware) toBeAddedParameterRewriter).setSchemas(schemas);
        }
        if (toBeAddedParameterRewriter instanceof EncryptRuleAware) {
            ((EncryptRuleAware) toBeAddedParameterRewriter).setEncryptRule(encryptRule);
        }
        if (toBeAddedParameterRewriter instanceof EncryptConditionsAware) {
            ((EncryptConditionsAware) toBeAddedParameterRewriter).setEncryptConditions(encryptConditions);
        }
        if (toBeAddedParameterRewriter instanceof DatabaseNameAware) {
            ((DatabaseNameAware) toBeAddedParameterRewriter).setDatabaseName(databaseName);
        }
    }
}
