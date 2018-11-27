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

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import io.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import io.shardingsphere.core.util.SQLUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Distinct select item.
 *
 * @author panjuan
 */
@Getter
@EqualsAndHashCode
@ToString
@RequiredArgsConstructor
public final class DistinctSelectItem implements SelectItem {
    
    private final String distinctColumnName;
    
    private final Optional<String> alias;

    @Override
    public String getExpression() {
        return Strings.isNullOrEmpty(distinctColumnName) ? DefaultKeyword.DISTINCT.name() : SQLUtil.getExactlyValue(DefaultKeyword.DISTINCT + " " + distinctColumnName);
    }
    
    /**
     * Get column label.
     *
     * @return column label
     */
    public String getColumnLabel() {
        return alias.isPresent() ? alias.get() : getExpression();
    }
}
