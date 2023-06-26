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

package org.apache.shardingsphere.data.pipeline.core.consistencycheck.result;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.DataConsistencyCheckUtils;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;

/**
 * Data match calculated result.
 */
@Getter
@Slf4j
public final class DataMatchCalculatedResult implements DataConsistencyCalculatedResult {
    
    private final Object maxUniqueKeyValue;
    
    private final int recordsCount;
    
    @Getter(AccessLevel.NONE)
    private final Collection<Collection<Object>> records;
    
    public DataMatchCalculatedResult(final Object maxUniqueKeyValue, final Collection<Collection<Object>> records) {
        this.maxUniqueKeyValue = maxUniqueKeyValue;
        recordsCount = records.size();
        this.records = records;
    }
    
    @Override
    public Optional<Object> getMaxUniqueKeyValue() {
        return Optional.of(maxUniqueKeyValue);
    }
    
    @SneakyThrows(SQLException.class)
    @Override
    public boolean equals(final Object o) {
        if (null == o) {
            return false;
        }
        if (this == o) {
            return true;
        }
        if (!(o instanceof DataMatchCalculatedResult)) {
            log.warn("DataMatchCalculatedResult type not match, o.className={}.", o.getClass().getName());
            return false;
        }
        final DataMatchCalculatedResult that = (DataMatchCalculatedResult) o;
        if (recordsCount != that.recordsCount || !Objects.equals(maxUniqueKeyValue, that.maxUniqueKeyValue)) {
            log.warn("Record count or max unique key value not match, recordCount1={}, recordCount2={}, maxUniqueKeyValue1={}, maxUniqueKeyValue2={}.",
                    recordsCount, that.recordsCount, maxUniqueKeyValue, that.maxUniqueKeyValue);
            return false;
        }
        EqualsBuilder equalsBuilder = new EqualsBuilder();
        Iterator<Collection<Object>> thisRecordsIterator = records.iterator();
        Iterator<Collection<Object>> thatRecordsIterator = that.records.iterator();
        while (thisRecordsIterator.hasNext() && thatRecordsIterator.hasNext()) {
            equalsBuilder.reset();
            Collection<Object> thisRecord = thisRecordsIterator.next();
            Collection<Object> thatRecord = thatRecordsIterator.next();
            if (thisRecord.size() != thatRecord.size()) {
                log.warn("Record column size not match, size1={}, size2={}, record1={}, record2={}.", thisRecord.size(), thatRecord.size(), thisRecord, thatRecord);
                return false;
            }
            if (!DataConsistencyCheckUtils.recordsEquals(thisRecord, thatRecord, equalsBuilder)) {
                log.warn("Records not equals, record1={}, record2={}.", thatRecord, thatRecord);
                return false;
            }
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getMaxUniqueKeyValue().orElse(null)).append(getRecordsCount()).append(records).toHashCode();
    }
}
