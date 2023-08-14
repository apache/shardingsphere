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

package org.apache.shardingsphere.mask.merge.dql;

import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mask.rule.MaskRule;
import org.apache.shardingsphere.mask.spi.MaskAlgorithm;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MaskAlgorithmMetaDataTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private MaskRule maskRule;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SelectStatementContext selectStatementContext;
    
    @SuppressWarnings("rawtypes")
    @Test
    void assertFindMaskAlgorithmByColumnIndex() {
        when(maskRule.findMaskAlgorithm("t_order", "order_id")).thenReturn(Optional.of(TypedSPILoader.getService(MaskAlgorithm.class, "MD5")));
        ColumnProjection columnProjection = new ColumnProjection(null, "order_id", null, mock(DatabaseType.class));
        columnProjection.setOriginalColumn(new IdentifierValue("order_id"));
        columnProjection.setOriginalTable(new IdentifierValue("t_order"));
        when(selectStatementContext.getProjectionsContext().getExpandProjections()).thenReturn(Collections.singletonList(columnProjection));
        when(selectStatementContext.getTablesContext().getTableNames()).thenReturn(Collections.singletonList("t_order"));
        Optional<MaskAlgorithm> actual = new MaskAlgorithmMetaData(database, maskRule, selectStatementContext).findMaskAlgorithmByColumnIndex(1);
        assertTrue(actual.isPresent());
        assertThat(actual.get().getType(), is("MD5"));
    }
}
