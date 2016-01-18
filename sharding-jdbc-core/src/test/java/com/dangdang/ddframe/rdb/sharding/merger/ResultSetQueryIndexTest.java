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

package com.dangdang.ddframe.rdb.sharding.merger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

import org.junit.Test;

import com.dangdang.ddframe.rdb.sharding.merger.common.ResultSetQueryIndex;

public final class ResultSetQueryIndexTest {
    
    @Test
    public void assertIsQueryBySequence() {
        assertTrue(new ResultSetQueryIndex(1).isQueryBySequence());
        assertFalse(new ResultSetQueryIndex("name").isQueryBySequence());
    }
    
    @Test
    public void assertGetRawQueryIndex() {
        assertThat(new ResultSetQueryIndex(1).getRawQueryIndex(), is((Object) 1));
        assertThat(new ResultSetQueryIndex("name").getRawQueryIndex(), is((Object) "name"));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewResultSetQueryIndexFailure() {
        new ResultSetQueryIndex(1L);
    }
}
