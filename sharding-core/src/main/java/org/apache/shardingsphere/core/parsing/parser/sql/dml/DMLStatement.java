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

package org.apache.shardingsphere.core.parsing.parser.sql.dml;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.shardingsphere.core.constant.SQLType;
import org.apache.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parsing.lexer.token.Keyword;
import org.apache.shardingsphere.core.parsing.lexer.token.TokenType;
import org.apache.shardingsphere.core.parsing.parser.context.condition.Column;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLExpression;
import org.apache.shardingsphere.core.parsing.parser.sql.AbstractSQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DML statement.
 *
 * @author zhangliang
 */
@ToString(callSuper = true)
@Getter
@Setter
public class DMLStatement extends AbstractSQLStatement {
    
    private static final Collection<Keyword> STATEMENT_PREFIX = Arrays.<Keyword>asList(DefaultKeyword.INSERT, DefaultKeyword.UPDATE, DefaultKeyword.DELETE);
    
    private boolean deleteStatement;
    
    private final Map<String, String> updateTables = new HashMap<>();
    
    private final Map<Column, SQLExpression> updateColumnValues = new LinkedHashMap<>();
    
    public  Map<Column, SQLExpression> getUpdateColumnValues()
    
    private int whereStartIndex;
    
    private int whereStopIndex;
    
    private int whereParameterStartIndex;
    
    private int whereParameterEndIndex;
    
    public DMLStatement() {
        super(SQLType.DML);
    }
    
    /**
     * Is DML statement.
     *
     * @param tokenType token type
     * @return is DML or not
     */
    public static boolean isDML(final TokenType tokenType) {
        return STATEMENT_PREFIX.contains(tokenType);
    }
}
