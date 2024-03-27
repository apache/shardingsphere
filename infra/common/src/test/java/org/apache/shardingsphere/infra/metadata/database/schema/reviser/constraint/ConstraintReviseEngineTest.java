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

package org.apache.shardingsphere.infra.metadata.database.schema.reviser.constraint;

import org.apache.shardingsphere.infra.database.core.metadata.data.model.ConstraintMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.MetaDataReviseEntry;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConstraintReviseEngineTest<T extends ShardingSphereRule> {
    
    @Mock
    private T mockRule;
    
    @Mock
    private MetaDataReviseEntry<T> mockMetaDataReviseEntry;
    
    @InjectMocks
    private ConstraintReviseEngine<T> engine;
    
    @Test
    void assertNotPresentReviserReturnsOriginalConstraints() {
        String tableName = "tableName";
        doReturn(Optional.empty()).when(mockMetaDataReviseEntry).getConstraintReviser(mockRule, tableName);
        Collection<ConstraintMetaData> expectedConstraints = new LinkedHashSet<>();
        expectedConstraints.add(new ConstraintMetaData("constraint1", tableName));
        expectedConstraints.add(new ConstraintMetaData("constraint2", tableName));
        
        Collection<ConstraintMetaData> actualConstraints = engine.revise(tableName, expectedConstraints);
        assertThat(actualConstraints, is(expectedConstraints));
    }
    
    @Test
    void assertReviserReturnsRevisedConstraints() {
        String tableName = "tableName";
        ConstraintReviser<T> reviser = mock(ConstraintReviser.class);
        doReturn(Optional.of(reviser)).when(mockMetaDataReviseEntry).getConstraintReviser(mockRule, tableName);
        
        Collection<ConstraintMetaData> originalConstraints = new LinkedHashSet<>();
        ConstraintMetaData constraint1 = new ConstraintMetaData("constraint1", tableName);
        ConstraintMetaData constraint2 = new ConstraintMetaData("constraint2", tableName);
        originalConstraints.add(constraint1);
        originalConstraints.add(constraint2);
        
        ConstraintMetaData constraint3 = new ConstraintMetaData("constraint3", tableName);
        when(reviser.revise(tableName, constraint1, mockRule)).thenReturn(Optional.of(constraint3));
        when(reviser.revise(tableName, constraint2, mockRule)).thenReturn(Optional.empty());
        Collection<ConstraintMetaData> expectedConstraints = new LinkedHashSet<>();
        expectedConstraints.add(constraint3);
        
        Collection<ConstraintMetaData> actualConstraints = engine.revise(tableName, originalConstraints);
        assertThat(actualConstraints, is(expectedConstraints));
    }
    
}
