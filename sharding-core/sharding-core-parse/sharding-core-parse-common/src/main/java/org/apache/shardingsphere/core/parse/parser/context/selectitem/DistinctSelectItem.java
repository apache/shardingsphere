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

package org.apache.shardingsphere.core.parse.parser.context.selectitem;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.parse.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parse.util.SQLUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * Distinct select item.
 *
 * @author panjuan
 */
@RequiredArgsConstructor
@Getter
public final class DistinctSelectItem implements SelectItem {
    
    private final Set<String> distinctColumnNames;
    
    private final Optional<String> alias;
    
    @Override
    public String getExpression() {
        return isSingleColumnWithAlias() ? SQLUtil.getExactlyValue(DefaultKeyword.DISTINCT.name() + " " + distinctColumnNames.iterator().next() + "AS" + alias.get()) 
                : SQLUtil.getExactlyValue(DefaultKeyword.DISTINCT + " " + Joiner.on(", ").join(distinctColumnNames));
    }
    
    /**
     * Get distinct column labels.
     *
     * @return distinct column labels
     */
    public Collection<String> getDistinctColumnLabels() {
        return isSingleColumnWithAlias() ? Collections.singletonList(alias.get()) : distinctColumnNames;
    }
    
    private boolean isSingleColumnWithAlias() {
        return 1 == distinctColumnNames.size() && alias.isPresent();
    }
}
