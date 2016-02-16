/**
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

package com.dangdang.ddframe.rdb.sharding.api.config;

import java.util.Collections;
import java.util.Map;

/**
 * 配置类.
 *
 * @author gaohongtao
 */
public final class ShardingConfiguration {
    
    private  Map<String,Object> props;

    public ShardingConfiguration() {
        this.props= Collections.emptyMap();
    }

    public ShardingConfiguration(Map<String, Object> props) {
        if(props == null){
            this.props=Collections.emptyMap();
        }else {
            this.props = props;
        }
    }


    /**
     * 获取字符串类型的配置.
     * 
     * @param key 配置项的键值
     * @return 配置值
     */
    public String getConfig(final ShardingConfigurationConstant key) {
        Object value=props.get(key.getKey());
        if(value==null){
            return key.getDefaultValue();
        }
        return value.toString();
    }
    
    /**
     * 获取制定类型的配置.
     * 
     * @param key 配置项的键值
     * @param type 配置值的类型
     * @return 配置值
     */
    public <T> T getConfig(final ShardingConfigurationConstant key, final Class<T> type) {
        return convert(getConfig(key), type);
    }
    
    @SuppressWarnings("unchecked")
    private <T> T convert(final String value, final Class<T> convertType) {
        if (Boolean.class == convertType || boolean.class == convertType) {
            return (T) Boolean.valueOf(value);
        }
        if (Integer.class == convertType || int.class == convertType) {
            return (T) Integer.valueOf(value);
        }
        if (Long.class == convertType || long.class == convertType) {
            return (T) Long.valueOf(value);
        }
        if (Double.class == convertType || double.class == convertType) {
            return (T) Double.valueOf(value);
        }
        if (String.class == convertType) {
            return (T) value;
        }
        throw new UnsupportedOperationException(String.format("unsupported config data type %s", convertType.getName()));
    }
}
