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

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;

/**
 * Or conditions.
 *
 * @author maxiaoguang
 */
@RequiredArgsConstructor
@Getter
@ToString
public final class OrConditions {
    
    private final List<AndConditions> andConditions = new LinkedList<>();
    
    /**
     * Add condition.
     *
     * @param condition condition
     */
    public void add(final Condition condition) {
        AndConditions firstAndConditions;
        if (isEmpty()) {
            firstAndConditions = new AndConditions();
            andConditions.add(firstAndConditions);
        } else {
            firstAndConditions = andConditions.get(0);
        }
        firstAndConditions.add(condition);
    }
    
    /**
     * Find condition via column.
     *
     * @param column column
     * @param index index of and conditions
     * @return found condition
     */
    public Optional<Condition> find(final Column column, final int index) {
        Optional<AndConditions> andConditions = get(index);
        return andConditions.isPresent() ? andConditions.get().find(column) : Optional.<Condition>absent();
    }
    
    /**
     * Get and conditions via index.
     *
     * @param index index of and conditions
     * @return found and conditions
     */
    public Optional<AndConditions> get(final int index) {
        AndConditions result = null;
        if (size() > index) {
            result = andConditions.get(index);
        }
        return Optional.fromNullable(result);
    }
    
    /**
     * Adjust and conditions is empty or not.
     *
     * @return and conditions is empty or not
     */
    public boolean isEmpty() {
        return andConditions.isEmpty();
    }
    
    /**
     * Returns the number of and conditions in this.
     *
     * @return the number of and conditions in this
     */
    public int size() {
        return andConditions.size();
    }
}
