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
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.optimize.statement.SQLStatementContext;
import org.apache.shardingsphere.core.rewrite.SQLRewriteDecorator;
import org.apache.shardingsphere.core.rewrite.SQLRewriteEngine;
import org.apache.shardingsphere.core.rewrite.parameter.rewriter.encrypt.EncryptParameterBuilderFactory;
import org.apache.shardingsphere.core.rewrite.sql.token.builder.EncryptTokenGenerateBuilder;
import org.apache.shardingsphere.core.rule.EncryptRule;

import java.util.List;

/**
 * Rewriter decorator for encrypt.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class EncryptRewriterDecorator implements SQLRewriteDecorator {
    
    private final EncryptRule encryptRule;
    
    private final boolean isQueryWithCipherColumn;
    
    @Override
    public void decorate(final SQLRewriteEngine sqlRewriteEngine, final TableMetas tableMetas, final SQLStatementContext sqlStatementContext, final List<Object> parameters) {
        EncryptParameterBuilderFactory.build(sqlRewriteEngine.getParameterBuilder(), encryptRule, tableMetas, sqlStatementContext, parameters, isQueryWithCipherColumn);
        sqlRewriteEngine.getSqlTokenGenerators().addAll(new EncryptTokenGenerateBuilder(encryptRule, isQueryWithCipherColumn).getSQLTokenGenerators());
    }
}
