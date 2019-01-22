/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.parsing.parser.sql;

import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.parsing.parser.context.condition.Conditions;
import io.shardingsphere.core.parsing.parser.context.table.Tables;
import io.shardingsphere.core.parsing.parser.token.SQLToken;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * SQL statement abstract class.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
@Setter
@ToString
public abstract class AbstractSQLStatement implements SQLStatement {
    
    private final SQLType type;
    
    private final Tables tables = new Tables();
    
    private final Conditions conditions = new Conditions();
    
    @Getter(AccessLevel.NONE)
    private final List<SQLToken> sqlTokens = new LinkedList<>();
    
    private int parametersIndex;
    
    @Override
    public final void addSQLToken(final SQLToken sqlToken) {
        sqlTokens.add(sqlToken);
    }
    
    @Override
    public final List<SQLToken> getSQLTokens() {
        Collections.sort(sqlTokens);
        return sqlTokens;
    }
    
    @Override
    public final void increaseParametersIndex() {
        ++parametersIndex;
    }
}
