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

package com.dangdang.ddframe.rdb.sharding.json;

import com.dangdang.ddframe.rdb.sharding.api.config.strategy.ComplexShardingStrategyConfiguration;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.HintShardingStrategyConfiguration;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.InlineShardingStrategyConfiguration;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.NoneShardingStrategyConfiguration;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.ShardingStrategyConfiguration;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.StandardShardingStrategyConfiguration;
import com.dangdang.ddframe.rdb.sharding.exception.ShardingJdbcException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * Sharding strategy configuration gson type adapter.
 *
 * @author zhangliang
 */
public final class ShardingStrategyConfigurationGsonTypeAdapter extends TypeAdapter<ShardingStrategyConfiguration> {
    
    @Override
    public ShardingStrategyConfiguration read(final JsonReader in) throws IOException {
        String type = "";
        String shardingColumn = "";
        String shardingColumns = "";
        String algorithmClassName = "";
        String preciseAlgorithmClassName = "";
        String rangeAlgorithmClassName = "";
        String algorithmInlineExpression = "";
        in.beginObject();
        while (in.hasNext()) {
            String jsonName = in.nextName();
            switch (jsonName) {
                case "type":
                    type = in.nextString();
                    break;
                case "shardingColumn":
                    shardingColumn = in.nextString();
                    break;
                case "shardingColumns":
                    shardingColumns = in.nextString();
                    break;
                case "algorithmClassName":
                    algorithmClassName = in.nextString();
                    break;
                case "preciseAlgorithmClassName":
                    preciseAlgorithmClassName = in.nextString();
                    break;
                case "rangeAlgorithmClassName":
                    rangeAlgorithmClassName = in.nextString();
                    break;
                case "algorithmInlineExpression":
                    algorithmInlineExpression = in.nextString();
                    break;
                default:
                    throw new ShardingJdbcException("Cannot convert json for property: %s", jsonName);
            }
        }
        in.endObject();
        return createStrategy(type, shardingColumn, shardingColumns, algorithmClassName, preciseAlgorithmClassName, rangeAlgorithmClassName, algorithmInlineExpression); 
    }
    
    private ShardingStrategyConfiguration createStrategy(final String type, final String shardingColumn, final String shardingColumns,
                                                         final String algorithmClassName, final String preciseAlgorithmClassName, final String rangeAlgorithmClassName,
                                                         final String algorithmInlineExpression) {
        if (type.equals(ShardingStrategyType.STANDARD.name())) {
            return createStandardStrategy(shardingColumn, preciseAlgorithmClassName, rangeAlgorithmClassName);
        }
        if (type.equals(ShardingStrategyType.COMPLEX.name())) {
            return createComplexStrategy(shardingColumns, algorithmClassName);
        }
        if (type.equals(ShardingStrategyType.INLINE.name())) {
            return createInlineStrategy(shardingColumn, algorithmInlineExpression);
        }
        if (type.equals(ShardingStrategyType.HINT.name())) {
            return createHintStrategy(algorithmClassName);
        }
        if (type.equals(ShardingStrategyType.NONE.name())) {
            return new NoneShardingStrategyConfiguration();
        }
        return null;
    }
    
    private ShardingStrategyConfiguration createStandardStrategy(final String shardingColumn, final String preciseAlgorithmClassName, final String rangeAlgorithmClassName) {
        StandardShardingStrategyConfiguration result = new StandardShardingStrategyConfiguration();
        result.setShardingColumn(shardingColumn);
        result.setPreciseAlgorithmClassName(preciseAlgorithmClassName);
        result.setRangeAlgorithmClassName(rangeAlgorithmClassName);
        return result;
    }
    
    private ShardingStrategyConfiguration createComplexStrategy(final String shardingColumns, final String algorithmClassName) {
        ComplexShardingStrategyConfiguration result = new ComplexShardingStrategyConfiguration();
        result.setShardingColumns(shardingColumns);
        result.setAlgorithmClassName(algorithmClassName);
        return result;
    }
    
    private ShardingStrategyConfiguration createInlineStrategy(final String shardingColumn, final String algorithmInlineExpression) {
        InlineShardingStrategyConfiguration result = new InlineShardingStrategyConfiguration();
        result.setShardingColumn(shardingColumn);
        result.setAlgorithmInlineExpression(algorithmInlineExpression);
        return result;
    }
    
    private ShardingStrategyConfiguration createHintStrategy(final String algorithmClassName) {
        HintShardingStrategyConfiguration result = new HintShardingStrategyConfiguration();
        result.setAlgorithmClassName(algorithmClassName);
        return result;
    }
    
    @Override
    public void write(final JsonWriter out, final ShardingStrategyConfiguration value) throws IOException {
        out.beginObject();
        if (value instanceof StandardShardingStrategyConfiguration) {
            out.name("type").value(ShardingStrategyType.STANDARD.name());
            StandardShardingStrategyConfiguration shardingStrategyConfig = (StandardShardingStrategyConfiguration) value;
            out.name("shardingColumn").value(shardingStrategyConfig.getShardingColumn());
            out.name("preciseAlgorithmClassName").value(shardingStrategyConfig.getPreciseAlgorithmClassName());
            out.name("rangeAlgorithmClassName").value(shardingStrategyConfig.getRangeAlgorithmClassName());
        } else if (value instanceof ComplexShardingStrategyConfiguration) {
            out.name("type").value(ShardingStrategyType.COMPLEX.name());
            ComplexShardingStrategyConfiguration shardingStrategyConfig = (ComplexShardingStrategyConfiguration) value;
            out.name("shardingColumns").value(shardingStrategyConfig.getShardingColumns());
            out.name("algorithmClassName").value(shardingStrategyConfig.getAlgorithmClassName());
        } else if (value instanceof InlineShardingStrategyConfiguration) {
            out.name("type").value(ShardingStrategyType.INLINE.name());
            InlineShardingStrategyConfiguration shardingStrategyConfig = (InlineShardingStrategyConfiguration) value;
            out.name("shardingColumn").value(shardingStrategyConfig.getShardingColumn());
            out.name("algorithmInlineExpression").value(shardingStrategyConfig.getAlgorithmInlineExpression());
        } else if (value instanceof HintShardingStrategyConfiguration) {
            out.name("type").value(ShardingStrategyType.HINT.name());
            HintShardingStrategyConfiguration shardingStrategyConfig = (HintShardingStrategyConfiguration) value;
            out.name("algorithmClassName").value(shardingStrategyConfig.getAlgorithmClassName());
        } else if (value instanceof NoneShardingStrategyConfiguration) {
            out.name("type").value(ShardingStrategyType.NONE.name());
        }
        out.endObject();
    }
    
    private enum ShardingStrategyType {
        
        STANDARD, COMPLEX, INLINE, HINT, NONE
    }
}
