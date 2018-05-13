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

package io.shardingjdbc.core.constant;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class ShardingPropertiesConstantTest {
    
    @Test
    public void assertFindByKey() {
        assertThat(ShardingPropertiesConstant.findByKey("sql.show"), is(ShardingPropertiesConstant.SQL_SHOW));
        assertThat(ShardingPropertiesConstant.findByKey("executor.size"), is(ShardingPropertiesConstant.EXECUTOR_SIZE));
    }
    
    @Test
    public void assertFindByKeyWhenNotFound() {
        assertNull(ShardingPropertiesConstant.findByKey("empty"));
    }
}
