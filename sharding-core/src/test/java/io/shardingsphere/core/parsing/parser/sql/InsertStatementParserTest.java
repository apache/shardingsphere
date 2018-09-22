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

package io.shardingsphere.core.parsing.parser.sql;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.parsing.SQLParsingEngine;
import io.shardingsphere.core.parsing.parser.exception.SQLParsingException;
import io.shardingsphere.core.rule.ShardingRule;
import org.junit.Test;

public final class InsertStatementParserTest extends AbstractStatementParserTest {
    
    @Test
    public void parseInsertOnDuplicateKeyUpdateWithNoShardingColumn() {
        ShardingRule shardingRule = createShardingRule();
        new SQLParsingEngine(DatabaseType.MySQL, "INSERT ALL INTO TABLE_XXX (field8) VALUES (field8) ON DUPLICATE KEY UPDATE field8 = VALUES(field8)", shardingRule, null).parse(false);
    }
    
    @Test(expected = SQLParsingException.class)
    public void parseInsertOnDuplicateKeyUpdateWithShardingColumn() {
        ShardingRule shardingRule = createShardingRule();
        new SQLParsingEngine(DatabaseType.MySQL, "INSERT ALL INTO TABLE_XXX (field1) VALUES (field1) ON DUPLICATE KEY UPDATE field1 = VALUES(field1)", shardingRule, null).parse(false);
    }
}
