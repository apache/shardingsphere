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

package com.dangdang.ddframe.rdb.sharding.keygen;

import com.dangdang.ddframe.rdb.sharding.api.rule.DataSourceRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public class UnsupportedKeyGeneratorTest {
    
    @Test(expected = IllegalArgumentException.class)
    public void testIllegalClass() {
        TableRule.builder("test").dataSourceRule(createDataSourceRule())
                .generateKeyColumn("col_1", IllegalKeyGenerator.class).build();
    }
    
    private DataSourceRule createDataSourceRule() {
        Map<String, DataSource> result = new HashMap<>(2);
        result.put("ds0", null);
        result.put("ds1", null);
        return new DataSourceRule(result);
    }
    
    private static class IllegalKeyGenerator implements KeyGenerator {
        @Override
        public Number generateKey() {
            return null;
        }
    }
}
