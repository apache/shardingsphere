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

package org.apache.shardingsphere.sqlfederation.compiler.sql.function.opengauss.impl;

import org.apache.shardingsphere.infra.version.ShardingSphereVersion;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class OpenGaussSystemFunctionTest {
    
    @Test
    void assertSystemFunctionsCoverAllBranches() throws ReflectiveOperationException {
        Field dirtyField = ShardingSphereVersion.class.getDeclaredField("BUILD_DIRTY");
        dirtyField.setAccessible(true);
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        Object unsafe = getUnsafeInstance(unsafeClass);
        Object staticBase = getStaticBase(unsafeClass, unsafe, dirtyField);
        long staticOffset = getStaticOffset(unsafeClass, unsafe, dirtyField);
        boolean originalDirty = getBoolean(unsafeClass, unsafe, staticBase, staticOffset);
        try {
            putBoolean(unsafeClass, unsafe, staticBase, staticOffset, false);
            String expectedCleanVersion = "ShardingSphere-Proxy " + ShardingSphereVersion.VERSION + "-" + ShardingSphereVersion.BUILD_COMMIT_ID_ABBREV;
            String actualCleanVersion = OpenGaussSystemFunction.version();
            assertThat(actualCleanVersion, is(expectedCleanVersion));
            putBoolean(unsafeClass, unsafe, staticBase, staticOffset, true);
            String expectedDirtyVersion = expectedCleanVersion + "-dirty";
            String actualDirtyVersion = OpenGaussSystemFunction.version();
            assertThat(actualDirtyVersion, is(expectedDirtyVersion));
        } finally {
            putBoolean(unsafeClass, unsafe, staticBase, staticOffset, originalDirty);
        }
        String actualOpenGaussVersion = OpenGaussSystemFunction.openGaussVersion();
        assertThat(actualOpenGaussVersion, is(ShardingSphereVersion.VERSION));
        int actualPasswordDeadline = OpenGaussSystemFunction.gsPasswordDeadline();
        assertThat(actualPasswordDeadline, is(90));
        int actualIntervalToNum = OpenGaussSystemFunction.intervalToNum(8);
        assertThat(actualIntervalToNum, is(8));
        int actualPasswordNotifyTime = OpenGaussSystemFunction.gsPasswordNotifyTime();
        assertThat(actualPasswordNotifyTime, is(7));
    }
    
    private Object getUnsafeInstance(final Class<?> unsafeClass) throws ReflectiveOperationException {
        Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        return unsafeField.get(null);
    }
    
    private Object getStaticBase(final Class<?> unsafeClass, final Object unsafe, final Field field) throws ReflectiveOperationException {
        Method staticFieldBase = unsafeClass.getMethod("staticFieldBase", Field.class);
        return staticFieldBase.invoke(unsafe, field);
    }
    
    private long getStaticOffset(final Class<?> unsafeClass, final Object unsafe, final Field field) throws ReflectiveOperationException {
        Method staticFieldOffset = unsafeClass.getMethod("staticFieldOffset", Field.class);
        return (long) staticFieldOffset.invoke(unsafe, field);
    }
    
    private boolean getBoolean(final Class<?> unsafeClass, final Object unsafe, final Object staticBase, final long staticOffset) throws ReflectiveOperationException {
        Method getBoolean = unsafeClass.getMethod("getBoolean", Object.class, long.class);
        return (boolean) getBoolean.invoke(unsafe, staticBase, staticOffset);
    }
    
    private void putBoolean(final Class<?> unsafeClass, final Object unsafe, final Object staticBase, final long staticOffset, final boolean value) throws ReflectiveOperationException {
        Method putBoolean = unsafeClass.getMethod("putBoolean", Object.class, long.class, boolean.class);
        putBoolean.invoke(unsafe, staticBase, staticOffset, value);
    }
}
