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

package com.alibaba.druid.sql.dialect.oracle.parser;

import com.alibaba.druid.sql.dialect.oracle.lexer.OracleKeyword;
import com.alibaba.druid.sql.lexer.DataType;
import com.alibaba.druid.sql.lexer.DefaultKeyword;
import com.alibaba.druid.sql.parser.AbstractDeleteParser;
import com.alibaba.druid.sql.parser.SQLExprParser;

public class OracleDeleteParser extends AbstractDeleteParser {
    
    public OracleDeleteParser(final SQLExprParser exprParser) {
        super(exprParser);
    }
    
    @Override
    protected void skipBetweenDeleteAndTable() {
        getExprParser().getLexer().skipIfEqual(DataType.HINT);
        getExprParser().getLexer().skipIfEqual(DefaultKeyword.FROM);
        getExprParser().getLexer().skipIfEqual(DataType.COMMENT);
        getExprParser().getLexer().skipIfEqual(OracleKeyword.ONLY);
    }
}
