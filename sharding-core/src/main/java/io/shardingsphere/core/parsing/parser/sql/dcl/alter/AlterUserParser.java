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

package io.shardingsphere.core.parsing.parser.sql.dcl.alter;

import io.shardingsphere.core.parsing.lexer.LexerEngine;
import io.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import io.shardingsphere.core.parsing.parser.exception.SQLParsingException;
import io.shardingsphere.core.parsing.parser.sql.SQLParser;
import io.shardingsphere.core.parsing.parser.sql.dcl.DCLStatement;
import lombok.RequiredArgsConstructor;

/**
 * Alter user parser.
 *
 * @author panjuan
 */
@RequiredArgsConstructor
public class AlterUserParser implements SQLParser {
    
    private final LexerEngine lexerEngine;
    
    @Override
    public final DCLStatement parse() {
        if (lexerEngine.skipIfEqual(DefaultKeyword.USER) || lexerEngine.skipIfEqual(DefaultKeyword.ROLE) || lexerEngine.skipIfEqual(DefaultKeyword.LOGIN)) {
            return new DCLStatement();
        } else {
            throw new SQLParsingException("Can't support other ALTER grammar unless ALTER USER, ALTER ROLE, ALTER LOGIN.");
        }
    }
}
