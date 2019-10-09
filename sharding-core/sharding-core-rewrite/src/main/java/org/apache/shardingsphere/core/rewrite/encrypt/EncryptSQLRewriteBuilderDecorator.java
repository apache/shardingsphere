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

package org.apache.shardingsphere.core.rewrite.encrypt;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.rewrite.SQLRewriteBuilder;
import org.apache.shardingsphere.core.rewrite.SQLRewriteBuilderDecorator;
import org.apache.shardingsphere.core.rewrite.encrypt.parameter.EncryptParameterRewriterBuilder;
import org.apache.shardingsphere.core.rewrite.parameter.ParameterRewriter;
import org.apache.shardingsphere.core.rewrite.sql.token.builder.EncryptTokenGenerateBuilder;
import org.apache.shardingsphere.core.rule.EncryptRule;

/**
 * SQL rewrite builder decorator for encrypt.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class EncryptSQLRewriteBuilderDecorator implements SQLRewriteBuilderDecorator {
    
    private final EncryptRule encryptRule;
    
    private final boolean isQueryWithCipherColumn;
    
    @Override
    public void decorate(final SQLRewriteBuilder sqlRewriteBuilder) {
        for (ParameterRewriter each : new EncryptParameterRewriterBuilder(encryptRule, isQueryWithCipherColumn).getParameterRewriters(
                sqlRewriteBuilder.getParameterBuilder(), sqlRewriteBuilder.getTableMetas(), sqlRewriteBuilder.getSqlStatementContext(), sqlRewriteBuilder.getParameters())) {
            each.rewrite(sqlRewriteBuilder.getParameterBuilder(), sqlRewriteBuilder.getSqlStatementContext(), sqlRewriteBuilder.getParameters());
        }
        sqlRewriteBuilder.addSQLTokenGenerators(new EncryptTokenGenerateBuilder(encryptRule, isQueryWithCipherColumn).getSQLTokenGenerators());
    }
}
