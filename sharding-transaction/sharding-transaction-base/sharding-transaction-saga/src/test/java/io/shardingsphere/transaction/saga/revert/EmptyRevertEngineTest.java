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

package io.shardingsphere.transaction.saga.revert;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import io.shardingsphere.transaction.saga.revert.EmptyRevertEngine;
import io.shardingsphere.transaction.saga.revert.RevertResult;

public class EmptyRevertEngineTest {
    
    private final EmptyRevertEngine revertEngine = new EmptyRevertEngine();
    
    @Test
    public void assertEmptyResult() {
        RevertResult result = revertEngine.revert("", "", Collections.<List<Object>>emptyList());
        assertThat(result.getRevertSQL(), is(""));
        assertThat(result.getRevertSQLParams().size(), is(0));
        result = revertEngine.revert("", "", new Object[]{});
        assertThat(result.getRevertSQL(), is(""));
        assertThat(result.getRevertSQLParams().size(), is(0));
    }
    
}
