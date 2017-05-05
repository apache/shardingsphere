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

package com.dangdang.ddframe.rdb.sharding.parsing;

import com.dangdang.ddframe.rdb.sharding.api.fixture.ShardingRuleMockBuilder;
import com.dangdang.ddframe.rdb.sharding.constants.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingUnsupportedException;
import org.junit.Test;

import java.util.Collections;

public final class UnsupportedParseTest {
    
    @Test(expected = SQLParsingUnsupportedException.class)
    public void assertCreate() {
        new SQLParsingEngine(DatabaseType.MySQL, "CREATE TABLE `order` (id BIGINT(10))", new ShardingRuleMockBuilder().build(), Collections.emptyList()).parseStatement();
    }
    
    @Test(expected = SQLParsingUnsupportedException.class)
    public void assertDrop() {
        new SQLParsingEngine(DatabaseType.MySQL, "DROP TABLE `order`", new ShardingRuleMockBuilder().build(), Collections.emptyList()).parseStatement();
    }
    
    @Test(expected = SQLParsingUnsupportedException.class)
    public void assertTruncate() {
        new SQLParsingEngine(DatabaseType.MySQL, "TRUNCATE `order`", new ShardingRuleMockBuilder().build(), Collections.emptyList()).parseStatement();
    }
    
    @Test(expected = SQLParsingUnsupportedException.class)
    public void assertAlter() {
        new SQLParsingEngine(DatabaseType.MySQL, "ALTER TABLE `order` ADD COLUMN `other` VARCHAR(45)", new ShardingRuleMockBuilder().build(), Collections.emptyList()).parseStatement();
    }
    
    @Test(expected = SQLParsingException.class)
    public void assertNegativeLimitRowCount() {
        new SQLParsingEngine(DatabaseType.MySQL, "select * from order limit -2,-1", new ShardingRuleMockBuilder().build(), Collections.emptyList()).parseStatement();
    }
}
