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
import io.shardingsphere.core.parsing.parser.sql.dml.DMLStatement;
import io.shardingsphere.core.rule.ShardingRule;
import org.junit.Test;


import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class UpdateStatementParserTest extends AbstractStatementParserTest {

    @Test
    public void parseWithOr() {
        ShardingRule shardingRule = createShardingRule();
        DMLStatement updateStatement = (DMLStatement) new SQLParsingEngine(
                DatabaseType.Oracle, "UPDATE TABLE_XXX AS xxx SET field1=1 WHERE field1<1 AND (field1 >2 OR xxx.field2 =1)", shardingRule, null).parse(false);
        assertUpdateStatementWitOr(updateStatement);
    }
    
    private void assertUpdateStatementWitOr(final DMLStatement updateStatement) {
        assertThat(updateStatement.getTables().find("TABLE_XXX").get().getName(), is("TABLE_XXX"));
        assertThat(updateStatement.getTables().find("TABLE_XXX").get().getAlias().get(), is("xxx"));
        assertTrue(updateStatement.getConditions().getOrCondition().getAndConditions().isEmpty());

    }
}
