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

import com.dangdang.ddframe.rdb.sharding.parser.sql.parser.ParserException;
import com.dangdang.ddframe.rdb.sharding.parser.sql.parser.ParserUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.api.fixture.ShardingRuleMockBuilder;
import com.dangdang.ddframe.rdb.sharding.constants.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.exception.SQLParserException;
import org.junit.Test;

import java.util.Collections;

public final class UnsupportedParseTest {
    
    @Test(expected = ParserUnsupportedException.class)
    public void assertCreate() throws SQLParserException {
        SQLParserFactory.create(DatabaseType.MySQL, "CREATE TABLE `order` (id BIGINT(10))", Collections.emptyList(), new ShardingRuleMockBuilder().build());
    }
    
    @Test(expected = ParserUnsupportedException.class)
    public void assertDrop() throws SQLParserException {
        SQLParserFactory.create(DatabaseType.MySQL, "DROP TABLE `order`", Collections.emptyList(), new ShardingRuleMockBuilder().build());
    }
    
    @Test(expected = ParserUnsupportedException.class)
    public void assertTruncate() throws SQLParserException {
        SQLParserFactory.create(DatabaseType.MySQL, "TRUNCATE `order`", Collections.emptyList(), new ShardingRuleMockBuilder().build());
    }
    
    @Test(expected = ParserUnsupportedException.class)
    public void assertAlter() throws SQLParserException {
        SQLParserFactory.create(DatabaseType.MySQL, "ALTER TABLE `order` ADD COLUMN `other` VARCHAR(45)", Collections.emptyList(), new ShardingRuleMockBuilder().build());
    }
    
    @Test(expected = ParserException.class)
    public void assertNegativeLimitRowCount() throws SQLParserException {
        SQLParserFactory.create(DatabaseType.MySQL, "select * from order limit -2,-1", Collections.emptyList(), new ShardingRuleMockBuilder().build()).parse();
    }
}
