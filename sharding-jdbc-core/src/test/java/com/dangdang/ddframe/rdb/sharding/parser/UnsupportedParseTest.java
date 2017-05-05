/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
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
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.parser;

import com.dangdang.ddframe.rdb.sharding.api.fixture.ShardingRuleMockBuilder;
import com.dangdang.ddframe.rdb.sharding.constants.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.exception.SQLParserException;
import com.dangdang.ddframe.rdb.sharding.parser.parser.ParserUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parser.parser.SQLParserEngine;
import org.junit.Test;

import java.util.Collections;

public final class UnsupportedParseTest {
    
    @Test(expected = ParserUnsupportedException.class)
    public void assertCreate() throws SQLParserException {
        new SQLParserEngine(DatabaseType.MySQL, "CREATE TABLE `order` (id BIGINT(10))", new ShardingRuleMockBuilder().build(), Collections.emptyList()).parseStatement();
    }
    
    @Test(expected = ParserUnsupportedException.class)
    public void assertDrop() throws SQLParserException {
        new SQLParserEngine(DatabaseType.MySQL, "DROP TABLE `order`", new ShardingRuleMockBuilder().build(), Collections.emptyList()).parseStatement();
    }
    
    @Test(expected = ParserUnsupportedException.class)
    public void assertTruncate() throws SQLParserException {
        new SQLParserEngine(DatabaseType.MySQL, "TRUNCATE `order`", new ShardingRuleMockBuilder().build(), Collections.emptyList()).parseStatement();
    }
    
    @Test(expected = ParserUnsupportedException.class)
    public void assertAlter() throws SQLParserException {
        new SQLParserEngine(DatabaseType.MySQL, "ALTER TABLE `order` ADD COLUMN `other` VARCHAR(45)", new ShardingRuleMockBuilder().build(), Collections.emptyList()).parseStatement();
    }
    
    @Test(expected = SQLParserException.class)
    public void assertNegativeLimitRowCount() throws SQLParserException {
        new SQLParserEngine(DatabaseType.MySQL, "select * from order limit -2,-1", new ShardingRuleMockBuilder().build(), Collections.emptyList()).parseStatement();
    }
}
