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

package com.dangdang.ddframe.rdb.sharding.router;

import java.util.Arrays;

import org.junit.Test;

import com.dangdang.ddframe.rdb.sharding.exception.SQLParserException;

public final class DMLTest extends AbstractBaseRouteSqlTest {
    
    @Test
    public void assertInsert() throws SQLParserException {
        assertSingleTarget("insert into `order` (order_id, name) value (1,'test')", "ds_1", "INSERT INTO order_1 (order_id, name) VALUES (1, 'test')");
        assertSingleTarget("insert into `order` (order_id, name) value (?,?)", Arrays.<Object>asList(2, "test"), "ds_0", "INSERT INTO order_0 (order_id, name) VALUES (?, ?)");
    }
    
    @Test
    public void assertUpdate() throws SQLParserException {
        assertSingleTarget("update `order` set name = 'test' where order_id = 1", "ds_1", "UPDATE order_1 SET name = 'test' WHERE order_id = 1");
        assertSingleTarget("update `order` set name = ? where order_id = ?", Arrays.<Object>asList("test", 2), "ds_0", "UPDATE order_0 SET name = ? WHERE order_id = ?");
    }
    
    @Test
    public void assertDelete() throws SQLParserException {
        assertSingleTarget("delete from `order` where order_id = 1", "ds_1", "DELETE FROM order_1 WHERE order_id = 1");
        assertSingleTarget("delete from `order` where order_id = ?", Arrays.<Object>asList(2), "ds_0", "DELETE FROM order_0 WHERE order_id = ?");
    }
}
