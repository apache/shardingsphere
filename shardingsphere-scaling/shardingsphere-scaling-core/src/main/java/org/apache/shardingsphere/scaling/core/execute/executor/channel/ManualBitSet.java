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

package org.apache.shardingsphere.scaling.core.execute.executor.channel;

import lombok.NoArgsConstructor;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Manual BitSet.
 */
@NoArgsConstructor
public final class ManualBitSet {
    
    private static final int BIT_SET_SIZE = 1024;
    
    private final List<BitSet> bitSets = new LinkedList<>();
    
    private long startIndex;
    
    /**
     * Sets the bit at the specified index to true.
     *
     * @param bitIndex a bit index
     */
    public synchronized void set(final long bitIndex) {
        int correctIndex = (int) (bitIndex - startIndex);
        int listIndex = correctIndex / BIT_SET_SIZE;
        for (int i = bitSets.size(); i <= listIndex; i++) {
            bitSets.add(new BitSet(BIT_SET_SIZE));
        }
        bitSets.get(listIndex).set(correctIndex % BIT_SET_SIZE);
    }
    
    /**
     * Get BitSet with specified range.
     *
     * @param fromIndex from index
     * @param toIndex to index
     * @return BitSet
     */
    public synchronized BitSet get(final long fromIndex, final long toIndex) {
        if (fromIndex >= toIndex) {
            return new BitSet();
        }
        BitSet result = new BitSet((int) (toIndex - fromIndex));
        int correctFromIndex = (int) (fromIndex - startIndex);
        int correctToIndex = (int) (toIndex - startIndex);
        int listFromIndex = correctFromIndex / BIT_SET_SIZE;
        int listToIndex = correctToIndex / BIT_SET_SIZE;
        for (int i = listFromIndex, k = 0; i <= listToIndex; i++) {
            BitSet bitSet = bitSets.get(i);
            int begin = i == listFromIndex ? correctFromIndex % BIT_SET_SIZE : 0;
            int end = i == listToIndex ? correctToIndex % BIT_SET_SIZE : BIT_SET_SIZE;
            for (int j = begin; j < end; j++) {
                if (bitSet.get(j)) {
                    result.set(k, true);
                }
                k++;
            }
        }
        return result;
    }
    
    /**
     * Get end index.
     *
     * @param fromIndex from index
     * @param size true bit size
     * @return index
     */
    public synchronized long getSetEndIndex(final long fromIndex, final int size) {
        if (size == 0) {
            return fromIndex;
        }
        int correctIndex = (int) (fromIndex - startIndex);
        int listIndex = correctIndex / BIT_SET_SIZE;
        int count = size;
        for (int i = listIndex; i < bitSets.size(); i++) {
            int begin = i == listIndex ? correctIndex % BIT_SET_SIZE : 0;
            for (int j = begin; j < BIT_SET_SIZE; j++) {
                if (bitSets.get(i).get(j) && --count == 0) {
                    return startIndex + i * BIT_SET_SIZE + j + 1;
                }
            }
        }
        throw new IndexOutOfBoundsException(String.format("BitSets(%s) do not have enough data from %d count %d", bitSets, correctIndex, size));
    }
    
    /**
     * Clear expire BitSet.
     *
     * @param bitIndex retain bit index
     */
    public void clear(final long bitIndex) {
        if ((bitIndex - startIndex) / BIT_SET_SIZE > 10) {
            synchronized (this) {
                int count = (int) ((bitIndex - startIndex) / BIT_SET_SIZE);
                if (count > 10) {
                    bitSets.subList(0, count).clear();
                    startIndex += count * BIT_SET_SIZE;
                }
            }
        }
    }
}
