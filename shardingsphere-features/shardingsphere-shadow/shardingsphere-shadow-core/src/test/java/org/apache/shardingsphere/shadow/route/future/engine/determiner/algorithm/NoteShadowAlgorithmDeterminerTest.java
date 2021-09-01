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

package org.apache.shardingsphere.shadow.route.future.engine.determiner.algorithm;

import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.shadow.algorithm.shadow.note.SimpleSQLNoteShadowAlgorithm;
import org.apache.shardingsphere.shadow.api.shadow.note.NoteShadowAlgorithm;
import org.junit.Test;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class NoteShadowAlgorithmDeterminerTest {
    
    @Test
    public void assertIsShadow() {
        assertTrueCase();
        assertFalseCase();
    }
    
    private void assertTrueCase() {
        assertThat(new NoteShadowAlgorithmDeterminer(createAlgorithmTrueCase()).isShadow(mock(InsertStatementContext.class), createShadowTablesTrueCase(), "t_user"), is(true));
    }
    
    private Collection<String> createShadowTablesTrueCase() {
        Collection<String> result = new LinkedList<>();
        result.add("t_user");
        result.add("t_order");
        return result;
    }
    
    private NoteShadowAlgorithm createAlgorithmTrueCase() {
        SimpleSQLNoteShadowAlgorithm simpleSQLNoteShadowAlgorithm = new SimpleSQLNoteShadowAlgorithm();
        Properties properties = new Properties();
        properties.setProperty("shadow", "true");
        simpleSQLNoteShadowAlgorithm.setProps(properties);
        simpleSQLNoteShadowAlgorithm.init();
        return simpleSQLNoteShadowAlgorithm;
    }
    
    private void assertFalseCase() {
        assertThat(new NoteShadowAlgorithmDeterminer(createAlgorithmFalseCase()).isShadow(mock(InsertStatementContext.class), createShadowTablesTrueCase(), "t_user"), is(false));
        assertThat(new NoteShadowAlgorithmDeterminer(createAlgorithmTrueCase()).isShadow(mock(InsertStatementContext.class), createShadowTablesFalseCase(), "t_user"), is(false));
        assertThat(new NoteShadowAlgorithmDeterminer(createAlgorithmTrueCase()).isShadow(mock(InsertStatementContext.class), createShadowTablesTrueCase(), "t_auto"), is(false));
    }
    
    private Collection<String> createShadowTablesFalseCase() {
        Collection<String> result = new LinkedList<>();
        result.add("t_auto");
        result.add("t_order");
        return result;
    }
    
    private NoteShadowAlgorithm createAlgorithmFalseCase() {
        SimpleSQLNoteShadowAlgorithm simpleSQLNoteShadowAlgorithm = new SimpleSQLNoteShadowAlgorithm();
        Properties properties = new Properties();
        properties.setProperty("shadow", "false");
        simpleSQLNoteShadowAlgorithm.setProps(properties);
        simpleSQLNoteShadowAlgorithm.init();
        return simpleSQLNoteShadowAlgorithm;
    }
}
