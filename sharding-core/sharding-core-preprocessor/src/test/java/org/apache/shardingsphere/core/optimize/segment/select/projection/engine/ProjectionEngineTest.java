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

package org.apache.shardingsphere.core.optimize.segment.select.projection.engine;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.optimize.segment.select.projection.Projection;
import org.apache.shardingsphere.core.optimize.segment.select.projection.impl.ShorthandProjection;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.ShorthandSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.generic.TableSegment;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ProjectionEngineTest {
    private ProjectionEngine projectionEngine;
    
    @Before
    public void setUp() {
        projectionEngine = new ProjectionEngine();
    }
    
    @Test
    public void assertProjectionCreatedWhenSelectItemSegmentNotMatched() {
        Optional<Projection> projectionOptional = projectionEngine.createProjection(null, null);
        assertFalse(projectionOptional.isPresent());
    }
    
    @Test
    public void assertProjectionCreatedWhenSelectItemSegmentInstanceOfShorthandSelectItemSegment() {
        ShorthandSelectItemSegment shorthandSelectItemSegment = mock(ShorthandSelectItemSegment.class);
        TableSegment tableSegment = mock(TableSegment.class);
        when(shorthandSelectItemSegment.getOwner()).thenReturn(Optional.of(tableSegment));
        Optional<Projection> projectionOptional = projectionEngine.createProjection(null, shorthandSelectItemSegment);
        assertTrue(projectionOptional.isPresent());
        Projection projection = projectionOptional.get();
        assertTrue(projection instanceof ShorthandProjection);
    }
}
