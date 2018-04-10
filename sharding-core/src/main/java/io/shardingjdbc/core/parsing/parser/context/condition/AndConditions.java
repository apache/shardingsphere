/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.core.parsing.parser.context.condition;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;

/**
 * And conditions.
 *
 * @author maxiaoguang
 */
@RequiredArgsConstructor
@Getter
@ToString
public final class AndConditions {
    
    private final List<Condition> conditions = new LinkedList<>();
    
    /**
     * Add condition.
     *
     * @param condition condition
     */
    public void add(final Condition condition) {
        conditions.add(condition);
    }
    
    /**
     * Find condition via column.
     *
     * @param column column
     * @return found condition
     */
    public Optional<Condition> find(final Column column) {
        Condition result = null;
        for (Condition each : conditions) {
            if (Objects.equal(each.getColumn(), column)) {
                result = each;
            }
        }
        return Optional.fromNullable(result);
    }
    
    /**
     * Get condition via index.
     *
     * @param index index of conditions
     * @return found condition
     */
    public Optional<Condition> get(final int index) {
        Condition result = null;
        if (size() > index) {
            result = conditions.get(index);
        }
        return Optional.fromNullable(result);
    }
    
    /**
     * Adjust conditions is empty or not.
     *
     * @return conditions is empty or not
     */
    public boolean isEmpty() {
        return conditions.isEmpty();
    }
    
    /**
     * Returns the number of conditions in this.
     *
     * @return the number of conditions in this
     */
    public int size() {
        return conditions.size();
    }
}
