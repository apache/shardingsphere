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

package org.apache.shardingsphere.infra.metadata.identifier;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCaseRule;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.LookupMode;
import org.apache.shardingsphere.infra.exception.kernel.metadata.AmbiguousIdentifierException;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

/**
 * Identifier index for metadata objects.
 *
 * @param <T> metadata object type
 */
@RequiredArgsConstructor
public final class IdentifierIndex<T> {
    
    private final DatabaseIdentifierContext databaseIdentifierContext;
    
    private final IdentifierScope identifierScope;
    
    private volatile Snapshot<T> snapshot = Snapshot.empty();
    
    /**
     * Rebuild identifier index by actual names.
     *
     * @param values actual name and metadata object map
     */
    public synchronized void rebuild(final Map<String, T> values) {
        Map<String, T> newExactValues = new LinkedHashMap<>(values.size(), 1F);
        Map<String, Collection<String>> newNormalizedIdentifiers = new LinkedHashMap<>(values.size(), 1F);
        IdentifierCaseRule rule = databaseIdentifierContext.getRule(identifierScope);
        for (Entry<String, T> entry : values.entrySet()) {
            newExactValues.put(entry.getKey(), entry.getValue());
            addNormalizedIdentifier(newNormalizedIdentifiers, rule, entry.getKey());
        }
        snapshot = createSnapshot(newExactValues, newNormalizedIdentifiers);
    }
    
    /**
     * Get all metadata objects.
     *
     * @return all metadata objects
     */
    public Collection<T> getAll() {
        return snapshot.getExactValues().values();
    }
    
    /**
     * Get all actual names.
     *
     * @return all actual names
     */
    public Collection<String> getAllNames() {
        return snapshot.getExactValues().keySet();
    }
    
    /**
     * Put metadata object by actual name.
     *
     * @param name actual name
     * @param value metadata object
     */
    public synchronized void put(final String name, final T value) {
        Snapshot<T> currentSnapshot = snapshot;
        Map<String, T> newExactValues = new LinkedHashMap<>(currentSnapshot.getExactValues());
        Map<String, Collection<String>> newNormalizedIdentifiers = copyNormalizedIdentifiers(currentSnapshot.getNormalizedIdentifiers());
        IdentifierCaseRule rule = databaseIdentifierContext.getRule(identifierScope);
        if (newExactValues.containsKey(name)) {
            removeNormalizedIdentifier(newNormalizedIdentifiers, rule, name);
        }
        newExactValues.put(name, value);
        addNormalizedIdentifier(newNormalizedIdentifiers, rule, name);
        snapshot = createSnapshot(newExactValues, newNormalizedIdentifiers);
    }
    
    /**
     * Remove metadata object by actual name.
     *
     * @param name actual name
     * @return removed metadata object
     */
    public synchronized T remove(final String name) {
        Snapshot<T> currentSnapshot = snapshot;
        if (!currentSnapshot.getExactValues().containsKey(name)) {
            return null;
        }
        Map<String, T> newExactValues = new LinkedHashMap<>(currentSnapshot.getExactValues());
        Map<String, Collection<String>> newNormalizedIdentifiers = copyNormalizedIdentifiers(currentSnapshot.getNormalizedIdentifiers());
        T result = newExactValues.remove(name);
        removeNormalizedIdentifier(newNormalizedIdentifiers, databaseIdentifierContext.getRule(identifierScope), name);
        snapshot = createSnapshot(newExactValues, newNormalizedIdentifiers);
        return result;
    }
    
    /**
     * Judge whether identifier index is empty or not.
     *
     * @return identifier index is empty or not
     */
    public boolean isEmpty() {
        return snapshot.getExactValues().isEmpty();
    }
    
    /**
     * Get size of identifier index.
     *
     * @return size of identifier index
     */
    public int size() {
        return snapshot.getExactValues().size();
    }
    
    /**
     * Find metadata object by identifier value.
     *
     * @param identifierValue identifier value
     * @return matched metadata object
     */
    public Optional<T> find(final IdentifierValue identifierValue) {
        Objects.requireNonNull(identifierValue, "identifierValue cannot be null.");
        Snapshot<T> currentSnapshot = snapshot;
        IdentifierCaseRule rule = databaseIdentifierContext.getRule(identifierScope);
        if (LookupMode.EXACT == rule.getLookupMode(identifierValue.getQuoteCharacter())) {
            return Optional.ofNullable(currentSnapshot.getExactValues().get(identifierValue.getValue()));
        }
        return findByNormalizedIdentifier(currentSnapshot, rule, identifierValue);
    }
    
    private Optional<T> findByNormalizedIdentifier(final Snapshot<T> currentSnapshot, final IdentifierCaseRule rule, final IdentifierValue identifierValue) {
        Collection<String> candidateIdentifiers = currentSnapshot.getNormalizedIdentifiers().get(rule.normalize(identifierValue.getValue()));
        if (null == candidateIdentifiers) {
            return Optional.empty();
        }
        String matchedIdentifier = null;
        Collection<String> ambiguousIdentifiers = null;
        for (String each : candidateIdentifiers) {
            if (!rule.matches(each, identifierValue.getValue(), identifierValue.getQuoteCharacter())) {
                continue;
            }
            if (null == matchedIdentifier) {
                matchedIdentifier = each;
                continue;
            }
            if (null == ambiguousIdentifiers) {
                ambiguousIdentifiers = new LinkedList<>();
                ambiguousIdentifiers.add(matchedIdentifier);
            }
            ambiguousIdentifiers.add(each);
        }
        if (null == matchedIdentifier) {
            return Optional.empty();
        }
        if (null == ambiguousIdentifiers) {
            return Optional.ofNullable(currentSnapshot.getExactValues().get(matchedIdentifier));
        }
        throw new AmbiguousIdentifierException(identifierValue.getValue(), ambiguousIdentifiers);
    }
    
    private void addNormalizedIdentifier(final Map<String, Collection<String>> values, final IdentifierCaseRule rule, final String name) {
        String normalizedName = rule.normalize(name);
        values.computeIfAbsent(normalizedName, key -> new LinkedList<>()).add(name);
    }
    
    private void removeNormalizedIdentifier(final Map<String, Collection<String>> values, final IdentifierCaseRule rule, final String name) {
        String normalizedName = rule.normalize(name);
        Collection<String> candidateIdentifiers = values.get(normalizedName);
        if (null == candidateIdentifiers) {
            return;
        }
        candidateIdentifiers.remove(name);
        if (candidateIdentifiers.isEmpty()) {
            values.remove(normalizedName);
        }
    }
    
    private Map<String, Collection<String>> copyNormalizedIdentifiers(final Map<String, Collection<String>> values) {
        Map<String, Collection<String>> result = new LinkedHashMap<>(values.size(), 1F);
        for (Entry<String, Collection<String>> entry : values.entrySet()) {
            result.put(entry.getKey(), new LinkedList<>(entry.getValue()));
        }
        return result;
    }
    
    private Snapshot<T> createSnapshot(final Map<String, T> exactValues, final Map<String, Collection<String>> normalizedIdentifiers) {
        Map<String, Collection<String>> immutableNormalizedIdentifiers = new LinkedHashMap<>(normalizedIdentifiers.size(), 1F);
        for (Entry<String, Collection<String>> entry : normalizedIdentifiers.entrySet()) {
            immutableNormalizedIdentifiers.put(entry.getKey(), Collections.unmodifiableCollection(new LinkedList<>(entry.getValue())));
        }
        return new Snapshot<>(Collections.unmodifiableMap(new LinkedHashMap<>(exactValues)), Collections.unmodifiableMap(immutableNormalizedIdentifiers));
    }
    
    @Override
    public String toString() {
        return snapshot.getExactValues().toString();
    }
    
    private static final class Snapshot<T> {
        
        private static final Snapshot<?> EMPTY = new Snapshot<>(Collections.emptyMap(), Collections.emptyMap());
        
        private final Map<String, T> exactValues;
        
        private final Map<String, Collection<String>> normalizedIdentifiers;
        
        private Snapshot(final Map<String, T> exactValues, final Map<String, Collection<String>> normalizedIdentifiers) {
            this.exactValues = exactValues;
            this.normalizedIdentifiers = normalizedIdentifiers;
        }
        
        @SuppressWarnings("unchecked")
        private static <T> Snapshot<T> empty() {
            return (Snapshot<T>) EMPTY;
        }
        
        private Map<String, T> getExactValues() {
            return exactValues;
        }
        
        private Map<String, Collection<String>> getNormalizedIdentifiers() {
            return normalizedIdentifiers;
        }
    }
}
