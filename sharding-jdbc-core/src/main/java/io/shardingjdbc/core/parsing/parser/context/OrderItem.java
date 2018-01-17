/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.parsing.parser.context;

import com.google.common.base.Optional;
import io.shardingjdbc.core.constant.OrderType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Order item.
 *
 * @author zhangliang
 */
@Getter
@Setter
@ToString
public final class OrderItem {
    
    private final Optional<String> owner;
    
    private final Optional<String> name;
    
    private final OrderType type;
    
    private final OrderType nullOrderType;
    
    private int index = -1;
    
    private Optional<String> alias;
    
    public OrderItem(final String name, final OrderType type, final OrderType nullOrderType, final Optional<String> alias) {
        this.owner = Optional.absent();
        this.name = Optional.of(name);
        this.type = type;
        this.nullOrderType = nullOrderType;
        this.alias = alias;
    }
    
    public OrderItem(final String owner, final String name, final OrderType type, final OrderType nullOrderType, final Optional<String> alias) {
        this.owner = Optional.of(owner);
        this.name = Optional.of(name);
        this.type = type;
        this.nullOrderType = nullOrderType;
        this.alias = alias;
    }
    
    public OrderItem(final int index, final OrderType type, final OrderType nullOrderType) {
        owner = Optional.absent();
        name = Optional.absent();
        this.index = index;
        this.type = type;
        this.nullOrderType = nullOrderType;
        alias = Optional.absent();
    }
    
    /**
     * Get column label.
     *
     * @return column label
     */
    public String getColumnLabel() {
        return alias.isPresent() ? alias.get() : name.orNull();
    }
    
    /**
     * Get qualified name.
     *
     * @return qualified name
     */
    public Optional<String> getQualifiedName() {
        if (!name.isPresent()) {
            return Optional.absent();
        }
        return owner.isPresent() ? Optional.of(owner.get() + "." + name.get()) : name;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (null == obj || !(obj instanceof OrderItem)) {
            return false;
        }
        OrderItem orderItem = (OrderItem) obj;
        return type == orderItem.getType() && (columnLabelEquals(orderItem) || qualifiedNameEquals(orderItem) || indexEquals(orderItem));
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
    
    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
