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

import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.rewrite.BasicRewriter;
import org.apache.shardingsphere.core.rewrite.parameter.rewriter.encrypt.EncryptParameterBuilderFactory;
import org.apache.shardingsphere.core.rewrite.sql.token.SQLTokenGenerators;
import org.apache.shardingsphere.core.rewrite.sql.token.builder.EncryptTokenGenerateBuilder;
import org.apache.shardingsphere.core.rule.EncryptRule;

import java.util.List;

/**
 * Rewriter decorator for encrypt.
 * 
 * @author zhangliang
 */
public final class EncryptRewriterDecorator {
    
    /**
     * Decorate rewriter.
     * 
     * @param rewriter rewriter to be decorated
     * @param parameters SQL parameters
     * @param encryptRule encrypt rule
     * @param tableMetas table metas
     * @param isQueryWithCipherColumn is query with cipher column
     */
    public void decorate(final BasicRewriter rewriter, final List<Object> parameters, final EncryptRule encryptRule, final TableMetas tableMetas, final boolean isQueryWithCipherColumn) {
        EncryptParameterBuilderFactory.build(rewriter.getParameterBuilder(), encryptRule, tableMetas, rewriter.getSqlStatementContext(), parameters, isQueryWithCipherColumn);
        SQLTokenGenerators sqlTokenGenerators = new SQLTokenGenerators();
        sqlTokenGenerators.addAll(new EncryptTokenGenerateBuilder(encryptRule, isQueryWithCipherColumn).getSQLTokenGenerators());
        rewriter.addSQLTokens(sqlTokenGenerators.generateSQLTokens(rewriter.getSqlStatementContext(), parameters, tableMetas, rewriter.getSqlBuilder().getSqlTokens(), true));
    }
}
