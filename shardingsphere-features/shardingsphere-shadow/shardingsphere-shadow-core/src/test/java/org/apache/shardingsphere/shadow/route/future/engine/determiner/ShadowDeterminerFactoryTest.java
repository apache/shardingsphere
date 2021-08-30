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

package org.apache.shardingsphere.shadow.route.future.engine.determiner;

import org.apache.shardingsphere.shadow.algorithm.shadow.note.SimpleSQLNoteShadowAlgorithm;
import org.apache.shardingsphere.shadow.api.shadow.column.ColumnShadowAlgorithm;
import org.apache.shardingsphere.shadow.api.shadow.note.NoteShadowAlgorithm;
import org.apache.shardingsphere.shadow.route.future.engine.determiner.algorithm.ColumnShadowAlgorithmDeterminer;
import org.apache.shardingsphere.shadow.route.future.engine.determiner.algorithm.NoteShadowAlgorithmDeterminer;
import org.apache.shardingsphere.shadow.route.future.engine.determiner.table.AnyAlgorithmApplicableShadowTableDeterminer;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;
import org.junit.Test;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShadowDeterminerFactoryTest {
    
    @Test
    public void assertSuccessNewInstance() {
        Optional<ShadowTableDeterminer> shadowTableDeterminer = ShadowDeterminerFactory.getShadowTableDeterminer("t_user", createShadowRule());
        shadowTableDeterminer.ifPresent(tableDeterminer -> assertThat(tableDeterminer instanceof AnyAlgorithmApplicableShadowTableDeterminer, is(true)));
        assertThat(ShadowDeterminerFactory.getShadowAlgorithmDeterminer(mock(NoteShadowAlgorithm.class)) instanceof NoteShadowAlgorithmDeterminer, is(true));
        assertThat(ShadowDeterminerFactory.getShadowAlgorithmDeterminer(mock(ColumnShadowAlgorithm.class)) instanceof ColumnShadowAlgorithmDeterminer, is(true));
    }
    
    private ShadowRule createShadowRule() {
        ShadowRule shadowRule = mock(ShadowRule.class);
        when(shadowRule.getRelatedShadowAlgorithms("t_user")).thenReturn(createRelatedShadowAlgorithms());
        return shadowRule;
    }
    
    private Optional<Collection<ShadowAlgorithm>> createRelatedShadowAlgorithms() {
        Collection<ShadowAlgorithm> result = new LinkedList<>();
        result.add(new SimpleSQLNoteShadowAlgorithm());
        return Optional.of(result);
    }
}
