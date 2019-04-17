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

package org.apache.shardingsphere.core.parse.antlr.sql.statement.dml;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.shardingsphere.core.constant.SQLType;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.AbstractSQLStatement;
import org.apache.shardingsphere.core.parse.old.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parse.old.lexer.token.Keyword;
import org.apache.shardingsphere.core.parse.old.lexer.token.TokenType;

import java.util.Arrays;
import java.util.Collection;

/**
 * DML statement.
 *
 * @author zhangliang
 */
@ToString(callSuper = true)
@Getter
@Setter
public abstract class DMLStatement extends AbstractSQLStatement {
    
    private static final Collection<Keyword> STATEMENT_PREFIX = Arrays.<Keyword>asList(DefaultKeyword.INSERT, DefaultKeyword.UPDATE, DefaultKeyword.DELETE);
    
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
