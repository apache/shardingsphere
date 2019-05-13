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

package org.apache.shardingsphere.core.parse.antlr.sql.statement;

import com.google.common.base.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.shardingsphere.core.constant.SQLType;
import org.apache.shardingsphere.core.exception.ShardingException;
import org.apache.shardingsphere.core.parse.antlr.sql.context.condition.Condition;
import org.apache.shardingsphere.core.parse.antlr.sql.context.condition.ParseCondition;
import org.apache.shardingsphere.core.parse.antlr.sql.context.table.Tables;
import org.apache.shardingsphere.core.parse.antlr.sql.token.EncryptColumnToken;
import org.apache.shardingsphere.core.parse.antlr.sql.token.SQLToken;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * SQL statement abstract class.
 *
 * @author zhangliang
 * @author panjuan
 */
@RequiredArgsConstructor
@Getter
@Setter
@ToString
public abstract class AbstractSQLStatement implements SQLStatement {
    
    private final SQLType type;
    
    private final Tables tables = new Tables();
    
    private final ParseCondition routeCondition = new ParseCondition();
    
    private final ParseCondition encryptCondition = new ParseCondition();
    
    @Getter(AccessLevel.NONE)
    private final List<SQLToken> sqlTokens = new LinkedList<>();
    
    private int parametersIndex;
    
    private String logicSQL;
    
    @Override
    public final void addSQLToken(final SQLToken sqlToken) {
        sqlTokens.add(sqlToken);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public final <T extends SQLToken> Optional<T> findSQLToken(final Class<T> sqlTokenType) {
        for (SQLToken each : sqlTokens) {
            if (each.getClass().equals(sqlTokenType)) {
                return Optional.of((T) each);
            }
        }
        return Optional.absent();
    }
    
    @Override
    public final List<SQLToken> getSQLTokens() {
        Collections.sort(sqlTokens);
        return sqlTokens;
    }
    
    /**
     * Get encrypt condition.
     * 
     * @param encryptColumnToken encrypt column token
     * @return encrypt condition
     */
    public Optional<Condition> getEncryptCondition(final EncryptColumnToken encryptColumnToken) {
        List<Condition> conditions = encryptCondition.findConditions(encryptColumnToken.getColumn());
        if (0 == conditions.size()) {
            return Optional.absent();
        }
        if (1 == conditions.size()) {
            return Optional.of(conditions.iterator().next());
        }
        return Optional.of(conditions.get(getEncryptConditionIndex(encryptColumnToken)));
    }
    
    private int getEncryptConditionIndex(final EncryptColumnToken encryptColumnToken) {
        int result = 0;
        for (SQLToken each : sqlTokens) {
            if (each.equals(encryptColumnToken)) {
                return result;
            } else if (each instanceof EncryptColumnToken) {
                result++;
            }
        }
        throw new ShardingException("Index Out Of Bounds For sqlTokens.");
    }
}
