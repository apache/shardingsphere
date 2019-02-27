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

package org.apache.shardingsphere.core.parsing.parser.dialect.oracle.clause;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.parsing.lexer.LexerEngine;
import org.apache.shardingsphere.core.parsing.lexer.token.Keyword;
import org.apache.shardingsphere.core.parsing.parser.clause.WhereClauseParser;
import org.apache.shardingsphere.core.parsing.parser.context.selectitem.SelectItem;

import java.util.List;

/**
 * Where clause parser for Oracle.
 *
 * @author zhangliang
 */
public final class OracleWhereClauseParser extends WhereClauseParser {
    
    public OracleWhereClauseParser(final LexerEngine lexerEngine) {
        super(DatabaseType.Oracle, lexerEngine);
    }
    
    @Override
    protected boolean isRowNumberCondition(final List<SelectItem> items, final String columnLabel) {
        Optional<String> rowNumberAlias = Optional.absent();
        for (SelectItem each : items) {
            if (each.getAlias().isPresent() && "rownum".equalsIgnoreCase(each.getExpression())) {
                rowNumberAlias = each.getAlias();
                break;
            }
        }
        return "rownum".equalsIgnoreCase(columnLabel) || columnLabel.equalsIgnoreCase(rowNumberAlias.orNull());
    }
    
    @Override
    protected Keyword[] getCustomizedOtherConditionOperators() {
        return new Keyword[0];
    }
}
