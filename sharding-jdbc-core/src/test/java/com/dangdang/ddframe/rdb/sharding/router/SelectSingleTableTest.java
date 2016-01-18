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

public final class SelectSingleTableTest extends AbstractBaseRouteSqlTest {
    
    @Test
    public void assertSingleSelect() throws SQLParserException {
        assertSingleTarget("select * from order where order_id = 1", "ds_1", "SELECT * FROM order_1 WHERE order_id = 1");
        assertSingleTarget("select * from order where order_id = ?", Arrays.<Object>asList(2), "ds_0", "SELECT * FROM order_0 WHERE order_id = ?");
    }
    
    @Test
    public void assertSelectWithAlias() throws SQLParserException {
        assertSingleTarget("select * from order a where a.order_id = 2", "ds_0", "SELECT * FROM order_0 a WHERE a.order_id = 2");
        assertSingleTarget("select * from order A where a.order_id = 2", "ds_0", "SELECT * FROM order_0 A WHERE a.order_id = 2");
        assertSingleTarget("select * from order a where A.order_id = 2", "ds_0", "SELECT * FROM order_0 a WHERE A.order_id = 2");
    }
    
    @Test
    public void assertSelectWithTableNameAsAlias() throws SQLParserException {
        assertSingleTarget("select * from order where order.order_id = 10", "ds_0", "SELECT * FROM order_0 WHERE order_0.order_id = 10");
    }
    
    @Test
    public void assertSelectWithIn() throws SQLParserException {
        assertMultipleTargets("select * from order where order_id in (?,?,?)", Arrays.<Object>asList(1, 2, 100), 4, 
                Arrays.asList("ds_0", "ds_1"), Arrays.asList("SELECT * FROM order_0 WHERE order_id IN (?, ?, ?)", "SELECT * FROM order_1 WHERE order_id IN (?, ?, ?)"));
    }
    
    @Test
    public void assertSelectWithBetween() throws SQLParserException {
        assertMultipleTargets("select * from order where order_id between ? and ?", Arrays.<Object>asList(1, 100), 4, 
                Arrays.asList("ds_0", "ds_1"), Arrays.asList("SELECT * FROM order_0 WHERE order_id BETWEEN ? AND ?", "SELECT * FROM order_1 WHERE order_id BETWEEN ? AND ?"));
    }
}
