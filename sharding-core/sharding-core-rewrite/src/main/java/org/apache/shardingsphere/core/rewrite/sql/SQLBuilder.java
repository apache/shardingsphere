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

package org.apache.shardingsphere.core.rewrite.sql;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.rewrite.sql.token.pojo.Alterable;
import org.apache.shardingsphere.core.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.core.rewrite.sql.token.pojo.Substitutable;
import org.apache.shardingsphere.core.route.type.RoutingUnit;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * SQL builder.
 *
 * @author gaohongtao
 * @author zhangliang
 * @author maxiaoguang
 * @author panjuan
 */
@RequiredArgsConstructor
public final class SQLBuilder {
    
    private final String logicSQL;
    
    private final List<SQLToken> sqlTokens;
    
    /**
     * Build SQL.
     *
     * @return SQL
     */
    public String toSQL() {
        return toSQL(null, Collections.<String, String>emptyMap());
    }
    
    /**
     * Build SQL.
     *
     * @param routingUnit routing unit
     * @param logicAndActualTables logic and actual tables map
     * @return SQL
     */
    public String toSQL(final RoutingUnit routingUnit, final Map<String, String> logicAndActualTables) {
        if (sqlTokens.isEmpty()) {
            return logicSQL;
        }
        Collections.sort(sqlTokens);
        StringBuilder result = new StringBuilder();
        result.append(logicSQL.substring(0, sqlTokens.get(0).getStartIndex()));
        for (SQLToken each : sqlTokens) {
            result.append(getSQLTokenText(each, routingUnit, logicAndActualTables));
            result.append(getConjunctionText(each));
        }
        return result.toString();
    }
    
    private String getSQLTokenText(final SQLToken sqlToken, final RoutingUnit routingUnit, final Map<String, String> logicAndActualTables) {
        return sqlToken instanceof Alterable ? ((Alterable) sqlToken).toString(routingUnit, logicAndActualTables) : sqlToken.toString();
    }
    
    private String getConjunctionText(final SQLToken sqlToken) {
        return logicSQL.substring(getStartIndex(sqlToken), getStopIndex(sqlToken));
    }
    
    private int getStartIndex(final SQLToken sqlToken) {
        int startIndex = sqlToken instanceof Substitutable ? ((Substitutable) sqlToken).getStopIndex() + 1 : sqlToken.getStartIndex();
        return startIndex > logicSQL.length() ? logicSQL.length() : startIndex;
    }
    
    private int getStopIndex(final SQLToken sqlToken) {
        int currentSQLTokenIndex = sqlTokens.indexOf(sqlToken);
        return sqlTokens.size() - 1 == currentSQLTokenIndex ? logicSQL.length() : sqlTokens.get(currentSQLTokenIndex + 1).getStartIndex();
    }
}
