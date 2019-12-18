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

package org.apache.shardingsphere.core.rewrite.sql;

import org.apache.shardingsphere.sql.parser.core.constant.QuoteCharacter;
import org.apache.shardingsphere.core.rewrite.feature.sharding.token.pojo.TableToken;
import org.apache.shardingsphere.core.rewrite.sql.token.pojo.SQLToken;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class SQLBuilderTest {
    
    @Test
    public void assertToSQLWithoutTokens() {
        SQLBuilder sqlBuilderWithoutTokens = new SQLBuilder("SELECT * FROM t_config", Collections.<SQLToken>emptyList());
        assertThat(sqlBuilderWithoutTokens.toSQL(), is("SELECT * FROM t_config"));
    }
    
    @Test
    public void assertToSQLWithTokens() {
        SQLBuilder sqlBuilderWithTokens = new SQLBuilder("SELECT * FROM t_order WHERE order_id > 1", Collections.<SQLToken>singletonList(new TableToken(14, 20, "t_order", QuoteCharacter.NONE)));
        assertThat(sqlBuilderWithTokens.toSQL(null, Collections.singletonMap("t_order", "t_order_0")), is("SELECT * FROM t_order_0 WHERE order_id > 1"));
    }
}
