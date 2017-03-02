/*
 * Copyright 1999-2101 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.druid.sql.dialect.mysql.ast.expr;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLExprImpl;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
public class MySqlMatchAgainstExpr extends SQLExprImpl implements SQLExpr {
    
    private final List<SQLExpr> columns = new ArrayList<>();

    private SQLExpr against;

    private SearchModifier searchModifier;
    
    @RequiredArgsConstructor
    @Getter
    public enum SearchModifier {
        
        IN_BOOLEAN_MODE("IN BOOLEAN MODE"), 
        IN_NATURAL_LANGUAGE_MODE("IN NATURAL LANGUAGE MODE"),
        IN_NATURAL_LANGUAGE_MODE_WITH_QUERY_EXPANSION("IN NATURAL LANGUAGE MODE WITH QUERY EXPANSION"),
        WITH_QUERY_EXPANSION("WITH QUERY EXPANSION"), ;
        
        private final String name;
    }
}
