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

package org.apache.shardingsphere.core.parse.parser.sql;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.shardingsphere.core.constant.SQLType;
import org.apache.shardingsphere.core.parse.parser.context.condition.Conditions;
import org.apache.shardingsphere.core.parse.parser.context.table.Tables;
import org.apache.shardingsphere.core.parse.parser.token.SQLToken;

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
    
    private final Conditions routeConditions = new Conditions();
    
    private final Conditions encryptConditions = new Conditions();
    
    @Getter(AccessLevel.NONE)
    private final List<SQLToken> sqlTokens = new LinkedList<>();
    
    private int parametersIndex;
    
    private String logicSQL;
    
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
