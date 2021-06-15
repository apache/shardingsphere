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

package org.apache.shardingsphere.spring.boot.fixture;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public final class TestJndiInitialContextFactory implements InitialContextFactory {
    
    private static JndiContext context;
    
    @Override
    public Context getInitialContext(final Hashtable<?, ?> environment) {
        return getContext();
    }
    
    /**
     * Bind.
     * 
     * @param name bind
     * @param obj object
     */
    public static void bind(final String name, final Object obj) {
        getContext().bind(name, obj);
    }
    
    private static JndiContext getContext() {
        if (null == context) {
            try {
                context = new JndiContext();
            } catch (final NamingException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return context;
    }
    
    private static final class JndiContext extends InitialContext {
        
        private final Map<String, Object> bindings = new HashMap<>();
        
        private JndiContext() throws NamingException {
            super(true);
        }
        
        @Override
        public void bind(final String name, final Object obj) {
            bindings.put(name, obj);
        }
        
        @Override
        public Object lookup(final String name) {
            return bindings.get(name);
        }
        
        @Override
        public Hashtable<?, ?> getEnvironment() {
            return new Hashtable<>();
        }
    }
}
