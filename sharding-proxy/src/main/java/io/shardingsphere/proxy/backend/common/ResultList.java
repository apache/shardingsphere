/*
 * Copyright 2016-2018 shardingsphere.io.
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

package io.shardingsphere.proxy.backend.common;

import java.util.Iterator;
import java.util.List;

/**
 * Result list.
 * 
 * @author zhangyonglun 
 */
public final class ResultList {
    
    private final Iterator<Object> iterator;
    
    public ResultList(final List<Object> resultList) {
        iterator = resultList.iterator();
    }
    
    /**
     * Has next.
     *
     * @return has next
     */
    public boolean hasNext() {
        return iterator.hasNext();
    }
    
    /**
     * Next object.
     *
     * @return object
     */
    public Object next() {
        return iterator.next();
    }
}
