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

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

import com.dangdang.ddframe.rdb.sharding.config.common.api.ShardingRuleBuilder;
import com.google.common.base.Preconditions;
import groovy.lang.GroovyShell;
import lombok.AccessLevel;
import lombok.Getter;
import org.junit.BeforeClass;

public abstract class AbstractConfigTest {
    
    @Getter(AccessLevel.PROTECTED)
    private static Map<String, DataSource> DATASOURCE_MAP = new HashMap<>();
    
    private static Class<ShardingRuleBuilder> BUILDER_CLASS = ShardingRuleBuilder.class;
    
    @BeforeClass
    public static void setup() {
        DATASOURCE_MAP.put("db0", null);
        DATASOURCE_MAP.put("db1", null);
    }
    
    protected ShardingRuleBuilder getShardingRuleBuilder(final String path) {
        URL url = getClass().getResource("/" + packageName() + "/" + path + ".groovy");
        Preconditions.checkNotNull(url);
    
        ShardingRuleBuilder builder = new ShardingRuleBuilder();
        builder.setDataSourceMap(DATASOURCE_MAP);
    
        try {
            GroovyShell groovyShell = (GroovyShell) getMethod("getShell", String.class).invoke(builder, url.getFile());
            AbstractShardingRuleConfigFileDelegate delegate = (AbstractShardingRuleConfigFileDelegate) groovyShell.parse(url.toURI());
            getField("ruleConfigDelegate").set(builder, delegate);
            getMethod("initDelegate").invoke(builder);
        } catch (final IllegalAccessException | InvocationTargetException | IOException | URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    
        return builder;
    }
    
    private static Method getMethod(String name, Class<?>... parameters) {
        try {
            Method method = BUILDER_CLASS.getDeclaredMethod(name, parameters);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private static Field getField(String name) {
        try {
            Field field = BUILDER_CLASS.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    protected AbstractShardingRuleConfigFileDelegate getDelegate(final String path) {
        AbstractShardingRuleConfigFileDelegate delegate = getShardingRuleBuilder(path).getRuleConfigDelegate();
        delegate.run();
        return delegate;
    }
    
    protected abstract String packageName();
}
