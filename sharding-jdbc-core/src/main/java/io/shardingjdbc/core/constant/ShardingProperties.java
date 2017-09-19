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

package io.shardingjdbc.core.constant;

import io.shardingjdbc.core.util.StringUtil;
import com.google.common.base.Joiner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;

/**
 * The properties for Sharding-JDBC configuration.
 *
 * @author gaohongtao
 * @author zhangliang
 */
public final class ShardingProperties {
    
    private final Properties props;
    
    public ShardingProperties(final Properties props) {
        this.props = props;
        validate();
    }
    
    private void validate() {
        Set<String> propertyNames = props.stringPropertyNames();
        Collection<String> errorMessages = new ArrayList<>(propertyNames.size());
        for (String each : propertyNames) {
            ShardingPropertiesConstant shardingPropertiesConstant = ShardingPropertiesConstant.findByKey(each);
            if (null == shardingPropertiesConstant) {
                continue;
            }
            Class<?> type = shardingPropertiesConstant.getType();
            String value = props.getProperty(each);
            if (type == boolean.class && !StringUtil.isBooleanValue(value)) {
                errorMessages.add(getErrorMessage(shardingPropertiesConstant, value));
                continue;
            }
            if (type == int.class && !StringUtil.isIntValue(value)) {
                errorMessages.add(getErrorMessage(shardingPropertiesConstant, value));
                continue;
            }
            if (type == long.class && !StringUtil.isLongValue(value)) {
                errorMessages.add(getErrorMessage(shardingPropertiesConstant, value));
            }
        }
        if (!errorMessages.isEmpty()) {
            throw new IllegalArgumentException(Joiner.on(" ").join(errorMessages));
        }
    }
    
    private String getErrorMessage(final ShardingPropertiesConstant shardingPropertiesConstant, final String invalidValue) {
        return String.format("Value '%s' of '%s' cannot convert to type '%s'.", invalidValue, shardingPropertiesConstant.getKey(), shardingPropertiesConstant.getType().getName());
    }
    
    /**
     * Get property value.
     * 
     * @param shardingPropertiesConstant sharding properties constant
     * @param <T> class type of return value
     * @return property value
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue(final ShardingPropertiesConstant shardingPropertiesConstant) {
        String result = props.getProperty(shardingPropertiesConstant.getKey(), shardingPropertiesConstant.getDefaultValue());
        if (boolean.class == shardingPropertiesConstant.getType()) {
            return (T) Boolean.valueOf(result);
        }
        if (int.class == shardingPropertiesConstant.getType()) {
            return (T) Integer.valueOf(result);
        }
        if (long.class == shardingPropertiesConstant.getType()) {
            return (T) Long.valueOf(result);
        }
        return (T) result;
    }
}
