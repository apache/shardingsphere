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

package org.apache.shardingsphere.infra.rewrite.parameterized.engine;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.rewrite.engine.result.SQLRewriteUnit;
import org.apache.shardingsphere.infra.rewrite.parameterized.engine.parameter.SQLRewriteEngineTestParameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
@Getter
public abstract class AbstractSQLRewriterParameterizedTest {
    
    private final SQLRewriteEngineTestParameters testParameters;
    
    @SuppressWarnings("JUnitTestMethodInProductSource")
    @Test
    public final void assertRewrite() throws IOException {
        Collection<SQLRewriteUnit> actual = createSQLRewriteUnits();
        assertThat(actual.size(), is(testParameters.getOutputSQLs().size()));
        int count = 0;
        for (SQLRewriteUnit each : actual) {
            assertThat(each.getSql(), is(testParameters.getOutputSQLs().get(count)));
            assertThat(each.getParameters().size(), is(testParameters.getOutputGroupedParameters().get(count).size()));
            for (int i = 0; i < each.getParameters().size(); i++) {
                assertThat(each.getParameters().get(i).toString(), is(testParameters.getOutputGroupedParameters().get(count).get(i)));
            }
            count++;
        }
    }
    
    protected abstract Collection<SQLRewriteUnit> createSQLRewriteUnits() throws IOException;
}
