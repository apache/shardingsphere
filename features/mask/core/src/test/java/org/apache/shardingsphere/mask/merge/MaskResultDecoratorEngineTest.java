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

package org.apache.shardingsphere.mask.merge;

import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.merge.engine.ResultProcessEngine;
import org.apache.shardingsphere.infra.merge.engine.decorator.ResultDecorator;
import org.apache.shardingsphere.infra.merge.engine.decorator.impl.TransparentResultDecorator;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.spi.type.ordered.OrderedSPIRegistry;
import org.apache.shardingsphere.mask.merge.dql.MaskDQLResultDecorator;
import org.apache.shardingsphere.mask.rule.MaskRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public final class MaskResultDecoratorEngineTest {
    
    @Mock
    private MaskRule rule;
    
    @Mock
    private ShardingSphereDatabase database;
    
    @Test
    public void assertNewInstanceWithSelectStatement() {
        MaskResultDecoratorEngine engine = (MaskResultDecoratorEngine) OrderedSPIRegistry.getRegisteredServices(ResultProcessEngine.class, Collections.singleton(rule)).get(rule);
        ResultDecorator<?> actual = engine.newInstance(database, rule, mock(ConfigurationProperties.class), mock(SelectStatementContext.class, RETURNS_DEEP_STUBS));
        assertThat(actual, instanceOf(MaskDQLResultDecorator.class));
    }
    
    @Test
    public void assertNewInstanceWithOtherStatement() {
        MaskResultDecoratorEngine engine = (MaskResultDecoratorEngine) OrderedSPIRegistry.getRegisteredServices(ResultProcessEngine.class, Collections.singleton(rule)).get(rule);
        ResultDecorator<?> actual = engine.newInstance(database, rule, mock(ConfigurationProperties.class), mock(InsertStatementContext.class));
        assertThat(actual, instanceOf(TransparentResultDecorator.class));
    }
}
