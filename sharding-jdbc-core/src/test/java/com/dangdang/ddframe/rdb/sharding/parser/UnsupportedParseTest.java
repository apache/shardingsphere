/**
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

import java.util.Arrays;
import java.util.Collections;

import com.dangdang.ddframe.rdb.sharding.api.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.exception.SQLParserException;
import com.google.common.collect.Lists;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public final class UnsupportedParseTest {
    
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    
    @Test(expected = SQLParserException.class)
    public void assertCreate() throws SQLParserException {
        SQLParserFactory.create(DatabaseType.MySQL, "CREATE TABLE `order` (id BIGINT(10))", Collections.emptyList(), Collections.<String>emptyList());
    }
    
    @Test(expected = SQLParserException.class)
    public void assertDrop() throws SQLParserException {
        SQLParserFactory.create(DatabaseType.MySQL, "DROP TABLE `order`", Collections.emptyList(), Collections.<String>emptyList());
    }
    
    @Test(expected = SQLParserException.class)
    public void assertTruncate() throws SQLParserException {
        SQLParserFactory.create(DatabaseType.MySQL, "TRUNCATE `order`", Collections.emptyList(), Collections.<String>emptyList());
    }
    
    @Test(expected = SQLParserException.class)
    public void assertAlter() throws SQLParserException {
        SQLParserFactory.create(DatabaseType.MySQL, "ALTER TABLE `order` ADD COLUMN `other` VARCHAR(45)", Collections.emptyList(), Collections.<String>emptyList());
    }
    
    @Test
    public void testWithoutColumnNames() {
        expectedException.expect(SQLParserException.class);
        expectedException.expectMessage("Insert statement DOES NOT contains column name.The syntax is : INSERT INTO tbl_name (col_name,...) VALUES (expr,...)");
        SQLParserFactory.create(DatabaseType.MySQL, "insert into `t_order` values(1,2,'INSERT')", Lists.newArrayList(), Arrays.asList("order_id", "user_id")).parse();
    }
    
    @Test
    public void testWithoutMissShardingKey() {
        expectedException.expect(SQLParserException.class);
        expectedException.expectMessage("Sharding columns DO NOT exist in insert column names");
        SQLParserFactory.create(DatabaseType.MySQL, "insert into `t_order`(ORDER_ID, USER_ID) values(1,2,'INSERT')", Lists.newArrayList(), Arrays.asList("order_id", "user_id")).parse();
    }
}
