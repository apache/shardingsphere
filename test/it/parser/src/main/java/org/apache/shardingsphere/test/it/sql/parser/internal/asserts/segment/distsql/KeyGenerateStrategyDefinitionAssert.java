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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.distsql;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sharding.distsql.segment.strategy.AbstractKeyGenerateStrategyDefinitionSegment;
import org.apache.shardingsphere.sharding.distsql.segment.strategy.ColumnKeyGenerateStrategyDefinitionSegment;
import org.apache.shardingsphere.sharding.distsql.segment.strategy.SequenceKeyGenerateStrategyDefinitionSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.distsql.ExpectedKeyGenerateStrategyDefinition;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Key generate strategy definition assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KeyGenerateStrategyDefinitionAssert {
    
    /**
     * Assert key generate strategy definition is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual key generate strategy definition
     * @param expected expected key generate strategy definition
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final AbstractKeyGenerateStrategyDefinitionSegment actual,
                                final ExpectedKeyGenerateStrategyDefinition expected) {
        if ("COLUMN".equals(expected.getType())) {
            assertThat(assertContext.getText("Key generate strategy type assertion error: "), actual, isA(ColumnKeyGenerateStrategyDefinitionSegment.class));
            ColumnKeyGenerateStrategyDefinitionSegment actualColumn = (ColumnKeyGenerateStrategyDefinitionSegment) actual;
            assertThat(assertContext.getText("Key generate strategy table name assertion error: "), actualColumn.getTableName(), is(expected.getTableName()));
            assertThat(assertContext.getText("Key generate strategy column name assertion error: "), actualColumn.getColumnName(), is(expected.getColumnName()));
        } else {
            assertThat(assertContext.getText("Key generate strategy type assertion error: "), expected.getType(), is("SEQUENCE"));
            assertThat(assertContext.getText("Key generate strategy type assertion error: "), actual, isA(SequenceKeyGenerateStrategyDefinitionSegment.class));
            SequenceKeyGenerateStrategyDefinitionSegment actualSequence = (SequenceKeyGenerateStrategyDefinitionSegment) actual;
            assertThat(assertContext.getText("Key generate strategy sequence name assertion error: "), actualSequence.getSequenceName(), is(expected.getSequenceName()));
        }
        if (null == expected.getKeyGeneratorName()) {
            assertFalse(actual.getKeyGeneratorName().isPresent(), assertContext.getText("Actual key generator should not exist."));
        } else {
            assertThat(assertContext.getText("Key generator name assertion error: "), actual.getKeyGeneratorName().orElse(null), is(expected.getKeyGeneratorName()));
        }
        AlgorithmAssert.assertIs(assertContext, actual.getAlgorithmSegment().orElse(null), expected.getAlgorithm());
    }
}
