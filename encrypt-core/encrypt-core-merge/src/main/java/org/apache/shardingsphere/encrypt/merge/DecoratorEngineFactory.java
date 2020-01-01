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

package org.apache.shardingsphere.encrypt.merge;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.encrypt.merge.dal.DALDecoratorEngine;
import org.apache.shardingsphere.encrypt.merge.dql.DQLDecoratorEngine;
import org.apache.shardingsphere.encrypt.merge.dql.EncryptorMetaData;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.relation.statement.impl.SelectSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.DALStatement;
import org.apache.shardingsphere.underlying.merge.engine.DecoratorEngine;
import org.apache.shardingsphere.underlying.merge.engine.impl.TransparentDecoratorEngine;

/**
 * Result merge engine factory.
 *
 * @author zhangliang
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DecoratorEngineFactory {
    
    /**
     * Create decorator engine instance.
     * 
     * @param encryptRule encrypt rule
     * @param sqlStatementContext SQL statement context
     * @param encryptorMetaData encryptor meta data
     * @param queryWithCipherColumn query with cipher column
     * @return decorator engine
     */
    public static DecoratorEngine newInstance(final EncryptRule encryptRule, 
                                              final SQLStatementContext sqlStatementContext, final EncryptorMetaData encryptorMetaData, final boolean queryWithCipherColumn) {
        if (sqlStatementContext instanceof SelectSQLStatementContext) {
            return new DQLDecoratorEngine(encryptorMetaData, queryWithCipherColumn);
        } 
        if (sqlStatementContext.getSqlStatement() instanceof DALStatement) {
            return new DALDecoratorEngine(encryptRule);
        }
        return new TransparentDecoratorEngine();
    }
}
