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

package org.apache.shardingsphere.infra.binder.segment.select.hint;

import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.hint.DataBaseHintSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.hint.HintSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.hint.ShardingHintSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;

/**
 * SqlHintContextEngine.
 */

public final class HintContextEngine {

    /**
     * Create hint context.
     *
     * @param selectStatement select statement
     * @return hint context
     */
    public HintContext createHintContext(final SelectStatement selectStatement) {
        if (selectStatement.getHint().isPresent()) {
            HintSegment hintSegment = selectStatement.getHint().get();
            if (hintSegment.getAbstractHintSegment() instanceof DataBaseHintSegment) {
                HintContext hintContext = new HintContext(HintType.DATABASE_HINT);
                hintContext.setDataBaseHintSegment((DataBaseHintSegment) hintSegment.getAbstractHintSegment());
                return hintContext;
            } else if (hintSegment.getAbstractHintSegment() instanceof ShardingHintSegment) {
                HintContext hintContext = new HintContext(HintType.SHARDING_VALUE_HINT);
                hintContext.setShardingHintSegment((ShardingHintSegment) hintSegment.getAbstractHintSegment());
                return hintContext;
            }
        }
        return new HintContext(HintType.NONE);
    }
}
