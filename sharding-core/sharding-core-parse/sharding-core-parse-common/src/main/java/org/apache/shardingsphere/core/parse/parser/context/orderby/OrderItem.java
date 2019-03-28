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

package org.apache.shardingsphere.core.parse.parser.context.orderby;

import com.google.common.base.Optional;
import lombok.*;
import org.apache.shardingsphere.core.constant.OrderDirection;

/**
 * Order item.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
@Setter
@ToString
public final class OrderItem {
    
    private final String owner;
    
    private final String name;
    
    private final OrderDirection orderDirection;
    
    private final OrderDirection nullOrderDirection;
    
    private int index = -1;
    
    private String expression;
    
    private String alias;
    
    public OrderItem(final String name, final OrderDirection orderDirection, final OrderDirection nullOrderDirection) {
        this(null, name, orderDirection, nullOrderDirection);
    }
    
    public OrderItem(final int index, final OrderDirection orderDirection, final OrderDirection nullOrderDirection) {
        this(null, null, orderDirection, nullOrderDirection);
        this.index = index;
    }
    
    /**
     * Get owner.
     *
     * @return owner
     */
    public Optional<String> getOwner() {
        return Optional.fromNullable(owner);
    }
    
    /**
     * Get name.
     *
     * @return name
     */
    public Optional<String> getName() {
        return Optional.fromNullable(name);
    }
    
    /**
     * Get alias.
     *
     * @return alias
     */
    public Optional<String> getAlias() {
        return Optional.fromNullable(alias);
    }
    
    /**
     * Get column label.
     *
     * @return column label
     */
    public String getColumnLabel() {
        return null == alias ? name : alias;
    }
    
    /**
     * Get qualified name.
     *
     * @return qualified name
     */
    public Optional<String> getQualifiedName() {
        if (null == name) {
            return Optional.absent();
        }
        return null == owner ? Optional.of(name) : Optional.of(owner + "." + name);
    }
    
    /**
     * Judge order item is index or not.
     * 
     * @return order item is index or not
     */
    public boolean isIndex() {
        return -1 != index;
    }

    @Override
    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Object owner = getOwner();
        result = result * PRIME + (owner == null ? 0 : owner.hashCode());
        Object name = getName();
        result = result * PRIME + (name == null ? 0 : name.hashCode());
        Object orderDirection = getOrderDirection();
        result = result * PRIME + (orderDirection == null ? 0 : orderDirection.hashCode());
        Object nullOrderDirection = getNullOrderDirection();
        result = result * PRIME + (nullOrderDirection == null ? 0 : nullOrderDirection.hashCode());
        result = result * PRIME + getIndex();
        Object expression = getExpression();
        result = result * PRIME + (expression == null ? 0 : expression.hashCode());
        Object alias = getAlias();
        result = result * PRIME + (alias == null ? 0 : alias.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof OrderItem)) {
            return false;
        }
        OrderItem orderItem = (OrderItem) obj;
        return orderDirection == orderItem.getOrderDirection() && (columnLabelEquals(orderItem) || qualifiedNameEquals(orderItem) || indexEquals(orderItem));
    }

    private boolean columnLabelEquals(final OrderItem orderItem) {
        String columnLabel = getColumnLabel();
        return null != columnLabel && columnLabel.equalsIgnoreCase(orderItem.getColumnLabel());
    }
    
    private boolean qualifiedNameEquals(final OrderItem orderItem) {
        Optional<String> thisQualifiedName = getQualifiedName();
        Optional<String> thatQualifiedName = orderItem.getQualifiedName();
        return thisQualifiedName.isPresent() && thatQualifiedName.isPresent() && thisQualifiedName.get().equalsIgnoreCase(thatQualifiedName.get());
    }
    
    private boolean indexEquals(final OrderItem orderItem) {
        return -1 != index && index == orderItem.getIndex();
    }
}
