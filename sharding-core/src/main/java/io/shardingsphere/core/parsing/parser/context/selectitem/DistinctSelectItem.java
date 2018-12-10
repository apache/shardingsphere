/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.parsing.parser.context.selectitem;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import io.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import io.shardingsphere.core.util.SQLUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Distinct select item.
 *
 * @author panjuan
 */
@Getter
@EqualsAndHashCode
@ToString
public final class DistinctSelectItem implements SelectItem {
    
    private final List<String> distinctColumnNames = new LinkedList<>();
    
    private final Optional<String> alias;
    
    public DistinctSelectItem(final Collection<String> distinctColumnNames, final Optional<String> alias) {
        this.distinctColumnNames.addAll(distinctColumnNames);
        this.alias = alias;
    }

    @Override
    public String getExpression() {
        
        return distinctColumnNames.isEmpty() ? DefaultKeyword.DISTINCT.name() : SQLUtil.getExactlyValue(DefaultKeyword.DISTINCT + " " + Joiner.on(", ").join(distinctColumnNames));
    }
    
    /**
     * Get column label.
     *
     * @return column label
     */
    public String getColumnLabel() {
        return alias.isPresent() ? alias.get() : getExpression();
    }
    
    /**
     * Get distinct column labels.
     *
     * @return distinct column labels
     */
    public Collection<String> getDistinctColumnLabels() {
        return alias.isPresent() ? getDistinctColumnLabels(alias.get()) : distinctColumnNames;
    }
    
    private Collection<String> getDistinctColumnLabels(final String alias) {
        if (1 == distinctColumnNames.size()) {
            return Collections.singletonList(alias);
        }
        List<String> result = new LinkedList<>(distinctColumnNames);
        result.set(0, alias);
        return result;
    }
}
