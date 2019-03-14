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

package org.apache.shardingsphere.core.parse.parser.sql.dql;

import lombok.ToString;
import org.apache.shardingsphere.core.constant.SQLType;
import org.apache.shardingsphere.core.parse.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parse.lexer.token.TokenType;
import org.apache.shardingsphere.core.parse.parser.sql.AbstractSQLStatement;

/**
 * DQL statement.
 *
 * @author zhangliang
 */
@ToString(callSuper = true)
public class DQLStatement extends AbstractSQLStatement {
    
    public DQLStatement() {
        super(SQLType.DQL);
    }
    
    /**
     * Is DQL statement.
     *
     * @param tokenType token type
     * @return is DQL or not
     */
    public static boolean isDQL(final TokenType tokenType) {
        return DefaultKeyword.SELECT == tokenType;
    }
}
