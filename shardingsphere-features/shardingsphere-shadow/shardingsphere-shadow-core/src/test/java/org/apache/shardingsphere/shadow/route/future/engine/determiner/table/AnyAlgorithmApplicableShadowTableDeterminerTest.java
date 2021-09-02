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

package org.apache.shardingsphere.shadow.route.future.engine.determiner.table;

import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.shadow.algorithm.shadow.note.SimpleSQLNoteShadowAlgorithm;
import org.apache.shardingsphere.shadow.api.shadow.note.NoteShadowAlgorithm;
import org.apache.shardingsphere.shadow.route.future.engine.determiner.ShadowAlgorithmDeterminer;
import org.apache.shardingsphere.shadow.route.future.engine.determiner.ShadowDeterminerFactory;
import org.apache.shardingsphere.shadow.route.future.engine.determiner.ShadowTableDeterminer;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;
import org.junit.Test;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class AnyAlgorithmApplicableShadowTableDeterminerTest {
    
    @Test
    public void assertIsShadow() {
        assertTrueCase();
        assertFalseCase();
    }
    
    private void assertTrueCase() {
        ShadowTableDeterminer shadowTableDeterminer = new AnyAlgorithmApplicableShadowTableDeterminer(createShadowAlgorithmDeterminersTrueCase());
        assertThat(shadowTableDeterminer.isShadow(mock(InsertStatementContext.class), createShadowRule(), "t_user"), is(true));
    }
    
    private Collection<ShadowAlgorithmDeterminer> createShadowAlgorithmDeterminersTrueCase() {
        Collection<ShadowAlgorithmDeterminer> result = new LinkedList<>();
        result.add(ShadowDeterminerFactory.getShadowAlgorithmDeterminer(createShadowAlgorithmsTrueCase()));
        return result;
    }
    
    private ShadowAlgorithm createShadowAlgorithmsTrueCase() {
        NoteShadowAlgorithm<String> result = new SimpleSQLNoteShadowAlgorithm();
        Properties properties = new Properties();
        properties.setProperty("shadow", "true");
        result.setProps(properties);
        result.init();
        return result;
    }
    
    private ShadowRule createShadowRule() {
        ShadowRule result = mock(ShadowRule.class);
        Collection<String> tables = new LinkedList<>();
        tables.add("t_user");
        tables.add("t_order");
        when(result.getAllShadowTableNames()).thenReturn(tables);
        return result;
    }
    
    private void assertFalseCase() {
        ShadowTableDeterminer shadowTableDeterminer = new AnyAlgorithmApplicableShadowTableDeterminer(createShadowAlgorithmDeterminersFalseCase());
        assertThat(shadowTableDeterminer.isShadow(mock(InsertStatementContext.class), createShadowRule(), "t_user"), is(false));
    }
    
    private Collection<ShadowAlgorithmDeterminer> createShadowAlgorithmDeterminersFalseCase() {
        Collection<ShadowAlgorithmDeterminer> result = new LinkedList<>();
        result.add(ShadowDeterminerFactory.getShadowAlgorithmDeterminer(createShadowAlgorithmsFalseCase()));
        return result;
    }
    
    private ShadowAlgorithm createShadowAlgorithmsFalseCase() {
        NoteShadowAlgorithm<String> result = new SimpleSQLNoteShadowAlgorithm();
        Properties properties = new Properties();
        properties.setProperty("shadow", "false");
        result.setProps(properties);
        result.init();
        return result;
    }
}
