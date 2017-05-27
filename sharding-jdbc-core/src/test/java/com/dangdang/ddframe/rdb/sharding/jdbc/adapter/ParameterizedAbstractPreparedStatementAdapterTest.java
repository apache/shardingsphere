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

package com.dangdang.ddframe.rdb.sharding.jdbc.adapter;

import com.dangdang.ddframe.rdb.sharding.jdbc.util.JdbcMethodInvocation;
import com.dangdang.ddframe.rdb.sharding.jdbc.util.ParameterList;
import lombok.RequiredArgsConstructor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.CALLS_REAL_METHODS;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
public class ParameterizedAbstractPreparedStatementAdapterTest {
    
    private final String methodName;
    
    private final Class[] parameterClasses;
    
    private final Object[] parameters;
    
    @Parameterized.Parameters(name = "{index}: method: {0}")
    public static Collection init() throws MalformedURLException {
        return Arrays.asList(new Object[][] {
                {"setNull", new Class[]{int.class, int.class}, new Object[] {1, Types.CHAR}},
                {"setNull", new Class[]{int.class, int.class, String.class}, new Object[] {1, Types.CHAR, "USER_DEFINE"}},
                {"setBoolean", new Class[]{int.class, boolean.class}, new Object[] {1, true}},
                {"setByte", new Class[]{int.class, byte.class}, new Object[] {1, (byte) 1}},
                {"setShort", new Class[]{int.class, short.class}, new Object[] {1, (short) 1}},
                {"setInt", new Class[]{int.class, int.class}, new Object[] {1, 1}},
                {"setLong", new Class[]{int.class, long.class}, new Object[] {1, 12121212332L}},
                {"setFloat", new Class[]{int.class, float.class}, new Object[] {1, 1F}},
                {"setDouble", new Class[]{int.class, double.class}, new Object[] {1, 1D}},
                {"setString", new Class[]{int.class, String.class}, new Object[] {1, "String"}},
                {"setBigDecimal", new Class[]{int.class, BigDecimal.class}, new Object[] {1, new BigDecimal("1212")}},
                {"setDate", new Class[]{int.class, Date.class}, new Object[] {1, new Date(1213213222)}},
                {"setDate", new Class[]{int.class, Date.class, Calendar.class}, new Object[] {1, new Date(1213213222), new GregorianCalendar()}},
                {"setTime", new Class[]{int.class, Time.class}, new Object[] {1, new Time(12232323)}},
                {"setTime", new Class[]{int.class, Time.class, Calendar.class}, new Object[] {1, new Time(12232323), new GregorianCalendar()}},
                {"setTimestamp", new Class[]{int.class, Timestamp.class}, new Object[] {1, new Timestamp(1232323)}},
                {"setTimestamp", new Class[]{int.class, Timestamp.class, Calendar.class}, new Object[] {1, new Timestamp(1232323), new GregorianCalendar()}},
                {"setBytes", new Class[]{int.class, byte[].class}, new Object[] {1, "String".getBytes()}},
                {"setBlob", new Class[]{int.class, Blob.class}, new Object[] {1, Mockito.mock(Blob.class)}},
                {"setBlob", new Class[]{int.class, InputStream.class}, new Object[] {1, Mockito.mock(InputStream.class)}},
                {"setBlob", new Class[]{int.class, InputStream.class, long.class}, new Object[] {1, Mockito.mock(InputStream.class), 10000121L}},
                {"setClob", new Class[]{int.class, Clob.class}, new Object[] {1, Mockito.mock(Clob.class)}},
                {"setClob", new Class[]{int.class, Reader.class}, new Object[] {1, Mockito.mock(Reader.class)}},
                {"setClob", new Class[]{int.class, Reader.class, long.class}, new Object[] {1, Mockito.mock(Reader.class), 10000121221L}},
                {"setAsciiStream", new Class[]{int.class, InputStream.class}, new Object[] {1, Mockito.mock(InputStream.class)}},
                {"setAsciiStream", new Class[]{int.class, InputStream.class, int.class}, new Object[] {1, Mockito.mock(InputStream.class), 121212}},
                {"setAsciiStream", new Class[]{int.class, InputStream.class, long.class}, new Object[] {1, Mockito.mock(InputStream.class), 10000121221L}},
                {"setUnicodeStream", new Class[]{int.class, InputStream.class, int.class}, new Object[] {1, Mockito.mock(InputStream.class), 121212}},
                {"setBinaryStream", new Class[]{int.class, InputStream.class}, new Object[] {1, Mockito.mock(InputStream.class)}},
                {"setBinaryStream", new Class[]{int.class, InputStream.class, int.class}, new Object[] {1, Mockito.mock(InputStream.class), 121212}},
                {"setBinaryStream", new Class[]{int.class, InputStream.class, long.class}, new Object[] {1, Mockito.mock(InputStream.class), 10000121221L}},
                {"setCharacterStream", new Class[]{int.class, Reader.class}, new Object[] {1, Mockito.mock(Reader.class)}},
                {"setCharacterStream", new Class[]{int.class, Reader.class, int.class}, new Object[] {1, Mockito.mock(Reader.class), 121212}},
                {"setCharacterStream", new Class[]{int.class, Reader.class, long.class}, new Object[] {1, Mockito.mock(Reader.class), 10000121221L}},
                {"setURL", new Class[]{int.class, URL.class}, new Object[] {1, new URL("http://www.dangdang.com/test")}},
                {"setSQLXML", new Class[]{int.class, SQLXML.class}, new Object[] {1, Mockito.mock(SQLXML.class)}},
                {"setRef", new Class[]{int.class, Ref.class}, new Object[] {1, Mockito.mock(Ref.class)}},
                {"setObject", new Class[]{int.class, Object.class}, new Object[] {1, new Object()}},
                {"setObject", new Class[]{int.class, Object.class, int.class}, new Object[] {1, new Object(), Types.BLOB}},
                {"setObject", new Class[]{int.class, Object.class, int.class, int.class}, new Object[] {1, new Object(), Types.DECIMAL, 10}},
        });
    }
    
    @Test
    public void test() throws Exception {
        AbstractPreparedStatementAdapter ps = Mockito.mock(AbstractPreparedStatementAdapter.class, CALLS_REAL_METHODS);
        Class<AbstractPreparedStatementAdapter> clazz = AbstractPreparedStatementAdapter.class;
        Field containerField = clazz.getDeclaredField("parameters");
        containerField.setAccessible(true);
        ParameterList container = new ParameterList(PreparedStatement.class);
        containerField.set(ps, container);
        clazz.getMethod(methodName, parameterClasses).invoke(ps, parameters);
        Field invocationListField = ParameterList.class.getDeclaredField("jdbcMethodInvocations");
        invocationListField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<JdbcMethodInvocation> list = (List<JdbcMethodInvocation>) invocationListField.get(container);
        assertThat(list.size(), is(1));
        JdbcMethodInvocation invocation = list.get(0);
        Field methodField = JdbcMethodInvocation.class.getDeclaredField("method");
        methodField.setAccessible(true);
        Method method = (Method) methodField.get(invocation);
        assertThat(method.getName(), is(methodName));
        assertThat(method.getParameterTypes(), is(parameterClasses));
        Field argumentsField = JdbcMethodInvocation.class.getDeclaredField("arguments");
        argumentsField.setAccessible(true);
        assertThat((Object[]) argumentsField.get(invocation), is(parameters));
    }
}
