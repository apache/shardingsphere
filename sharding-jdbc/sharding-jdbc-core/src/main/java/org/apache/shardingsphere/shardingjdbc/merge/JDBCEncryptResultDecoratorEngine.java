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

package org.apache.shardingsphere.shardingjdbc.merge;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.encrypt.merge.EncryptResultDecoratorEngine;
import org.apache.shardingsphere.encrypt.merge.dql.EncryptorMetaData;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;

import java.sql.ResultSetMetaData;

/**
 * JDBC result decorator engine for encrypt.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class JDBCEncryptResultDecoratorEngine extends EncryptResultDecoratorEngine {
    
    private final ResultSetMetaData resultSetMetaData;
    
    @Override
    protected EncryptorMetaData createEncryptorMetaData(final EncryptRule encryptRule, final SQLStatementContext sqlStatementContext) {
        return new ResultSetEncryptorMetaData(encryptRule, resultSetMetaData, sqlStatementContext);
    }
}
