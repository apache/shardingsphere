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

package com.dangdang.ddframe.rdb.sharding.config.common.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import groovy.lang.GString;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 配置文件工具类.
 * 
 * @author gaohongtao
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConfigUtil {
    
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    
    public static List<String> generateString(final GString gString) {
        String[] strings = gString.getStrings();
        List<List<String>> valueScenario = getValueScenario(gString);
        List<String> result = new ArrayList<>(valueScenario.size());
        for (List<String> each : valueScenario) {
            StringBuilder stringItemBuilder = new StringBuilder();
            int numberOfValues = each.size();
            for (int i = 0, size = strings.length; i < size; i++) {
                stringItemBuilder.append(strings[i]);
                if (i < numberOfValues) {
                    stringItemBuilder.append(each.get(i));
                }
            }
            result.add(stringItemBuilder.toString());
        }
        
        return result;
    }
    
    public static List<String> generateList(final List list) {
        List<String> result = new ArrayList<>();
        for (Object each : list) {
            if (each instanceof GString) {
                result.addAll(generateString((GString) each));
            } else {
                result.add(each.toString());
            }
        }
        return result;
    }
    
    public static List<List<String>> getValueScenario(final GString gString) {
        List<List<String>> dimValue = new ArrayList<>(gString.getValues().length);
        for (Object each : gString.getValues()) {
            if (null == each) {
                continue;
            }
            if (each instanceof Collection) {
                dimValue.add(Lists.transform(Lists.newArrayList((Collection<?>) each), new Function<Object, String>() {
                    @Override
                    public String apply(final Object input) {
                        return input.toString();
                    }
                }));
            } else {
                dimValue.add(Lists.newArrayList(each.toString()));
            }
        }
        
        return descartes(dimValue);
    }
    
    public static <T> List<List<T>> descartes(final List<List<T>> dimvalue) {
        List<List<T>> result = new ArrayList<>();
        descartes(dimvalue, result, 0, new LinkedList<T>());
        return result;
    }
    
    private static <T> void descartes(final List<List<T>> dimvalue, final List<List<T>> result, final int layer, final LinkedList<T> current) {
        if (layer < dimvalue.size() - 1) {
            //大于一个集合时，第一个集合为空
            if (dimvalue.get(layer).size() == 0) {
                descartes(dimvalue, result, layer + 1, current);
            } else {
                for (int i = 0; i < dimvalue.get(layer).size(); i++) {
                    current.offerLast(dimvalue.get(layer).get(i));
                    descartes(dimvalue, result, layer + 1, current);
                    current.removeLast();
                }
            }
        } else if (layer == dimvalue.size() - 1) {
            //只有一个集合，且集合中没有元素
            if (dimvalue.get(layer).size() == 0) {
                List<T> resultItem = new ArrayList<>(current);
                result.add(resultItem);
            } else {
                //只有一个集合，且集合中有元素时：其笛卡尔积就是这个集合元素本身
                for (int i = 0; i < dimvalue.get(layer).size(); i++) {
                    List<T> resultItem = new ArrayList<>(current);
                    resultItem.add(dimvalue.get(layer).get(i));
                    result.add(resultItem);
                }
            }
        }
    }
}
