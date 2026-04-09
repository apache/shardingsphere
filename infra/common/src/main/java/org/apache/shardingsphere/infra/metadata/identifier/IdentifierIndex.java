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

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
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
public final class IdentifierIndex<T> {
    
    private final DatabaseIdentifierContext databaseIdentifierContext;
    
    private final IdentifierScope identifierScope;
    
    private volatile Snapshot<T> snapshot = Snapshot.empty();
    
    public IdentifierIndex(final DatabaseIdentifierContext databaseIdentifierContext, final IdentifierScope identifierScope) {
        this.databaseIdentifierContext = databaseIdentifierContext;
        this.identifierScope = identifierScope;
    }
    
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
        snapshot = createSnapshot(newExactValues, newNormalizedIdentifiers, rule);
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
        Map<String, Collection<String>> newNormalizedIdentifiers = copyNormalizedIdentifiers(currentSnapshot.getNormalizedIdentifierNames());
        IdentifierCaseRule rule = databaseIdentifierContext.getRule(identifierScope);
        if (newExactValues.containsKey(name)) {
            removeNormalizedIdentifier(newNormalizedIdentifiers, rule, name);
        }
        newExactValues.put(name, value);
        addNormalizedIdentifier(newNormalizedIdentifiers, rule, name);
        snapshot = createSnapshot(newExactValues, newNormalizedIdentifiers, rule);
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
        Map<String, Collection<String>> newNormalizedIdentifiers = copyNormalizedIdentifiers(currentSnapshot.getNormalizedIdentifierNames());
        IdentifierCaseRule rule = databaseIdentifierContext.getRule(identifierScope);
        T result = newExactValues.remove(name);
        removeNormalizedIdentifier(newNormalizedIdentifiers, rule, name);
        snapshot = createSnapshot(newExactValues, newNormalizedIdentifiers, rule);
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
        NormalizedBucket<T> normalizedBucket = currentSnapshot.getNormalizedBuckets().get(rule.normalize(identifierValue.getValue()));
        if (null == normalizedBucket) {
            return Optional.empty();
        }
        return QuoteCharacter.NONE == identifierValue.getQuoteCharacter()
                ? findByUnquotedNormalizedIdentifier(normalizedBucket, identifierValue.getValue())
                : findByQuotedNormalizedIdentifier(currentSnapshot, rule, normalizedBucket, identifierValue);
    }
    
    private Optional<T> findByUnquotedNormalizedIdentifier(final NormalizedBucket<T> normalizedBucket, final String identifierValue) {
        if (!normalizedBucket.hasUnquotedIdentifier()) {
            return Optional.empty();
        }
        if (normalizedBucket.hasSingleUnquotedIdentifier()) {
            return Optional.ofNullable(normalizedBucket.getSingleUnquotedValue());
        }
        throw new AmbiguousIdentifierException(identifierValue, normalizedBucket.getUnquotedIdentifiers());
    }
    
    private Optional<T> findByQuotedNormalizedIdentifier(final Snapshot<T> currentSnapshot, final IdentifierCaseRule rule,
                                                         final NormalizedBucket<T> normalizedBucket, final IdentifierValue identifierValue) {
        if (normalizedBucket.hasSingleIdentifier()) {
            return rule.matches(normalizedBucket.getSingleIdentifier(), identifierValue.getValue(), identifierValue.getQuoteCharacter())
                    ? Optional.ofNullable(normalizedBucket.getSingleValue())
                    : Optional.empty();
        }
        String matchedIdentifier = null;
        Collection<String> ambiguousIdentifiers = null;
        for (String each : normalizedBucket.getIdentifiers()) {
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
    
    private Snapshot<T> createSnapshot(final Map<String, T> exactValues, final Map<String, Collection<String>> normalizedIdentifiers, final IdentifierCaseRule rule) {
        Map<String, Collection<String>> immutableNormalizedIdentifiers = new LinkedHashMap<>(normalizedIdentifiers.size(), 1F);
        Map<String, NormalizedBucket<T>> normalizedBuckets = new LinkedHashMap<>(normalizedIdentifiers.size(), 1F);
        for (Entry<String, Collection<String>> entry : normalizedIdentifiers.entrySet()) {
            Collection<String> identifiers = Collections.unmodifiableCollection(new LinkedList<>(entry.getValue()));
            immutableNormalizedIdentifiers.put(entry.getKey(), identifiers);
            normalizedBuckets.put(entry.getKey(), createNormalizedBucket(exactValues, identifiers, rule));
        }
        return new Snapshot<>(Collections.unmodifiableMap(new LinkedHashMap<>(exactValues)),
                Collections.unmodifiableMap(immutableNormalizedIdentifiers), Collections.unmodifiableMap(normalizedBuckets));
    }
    
    private NormalizedBucket<T> createNormalizedBucket(final Map<String, T> exactValues, final Collection<String> identifiers, final IdentifierCaseRule rule) {
        String singleIdentifier = null;
        T singleValue = null;
        if (1 == identifiers.size()) {
            singleIdentifier = identifiers.iterator().next();
            singleValue = exactValues.get(singleIdentifier);
        }
        String singleUnquotedIdentifier = null;
        T singleUnquotedValue = null;
        Collection<String> unquotedIdentifiers = null;
        for (String each : identifiers) {
            if (!rule.matches(each, each, QuoteCharacter.NONE)) {
                continue;
            }
            if (null == singleUnquotedIdentifier) {
                singleUnquotedIdentifier = each;
                singleUnquotedValue = exactValues.get(each);
                continue;
            }
            if (null == unquotedIdentifiers) {
                unquotedIdentifiers = new LinkedList<>();
                unquotedIdentifiers.add(singleUnquotedIdentifier);
            }
            unquotedIdentifiers.add(each);
        }
        return new NormalizedBucket<>(singleIdentifier, singleValue, identifiers, singleUnquotedIdentifier,
                singleUnquotedValue, null == unquotedIdentifiers ? null : Collections.unmodifiableCollection(unquotedIdentifiers));
    }
    
    @Override
    public String toString() {
        return snapshot.getExactValues().toString();
    }
    
    private static final class Snapshot<T> {
        
        private static final Snapshot<?> EMPTY = new Snapshot<>(Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
        
        private final Map<String, T> exactValues;
        
        private final Map<String, Collection<String>> normalizedIdentifierNames;
        
        private final Map<String, NormalizedBucket<T>> normalizedBuckets;
        
        private Snapshot(final Map<String, T> exactValues, final Map<String, Collection<String>> normalizedIdentifierNames, final Map<String, NormalizedBucket<T>> normalizedBuckets) {
            this.exactValues = exactValues;
            this.normalizedIdentifierNames = normalizedIdentifierNames;
            this.normalizedBuckets = normalizedBuckets;
        }
        
        @SuppressWarnings("unchecked")
        private static <T> Snapshot<T> empty() {
            return (Snapshot<T>) EMPTY;
        }
        
        private Map<String, T> getExactValues() {
            return exactValues;
        }
        
        private Map<String, Collection<String>> getNormalizedIdentifierNames() {
            return normalizedIdentifierNames;
        }
        
        private Map<String, NormalizedBucket<T>> getNormalizedBuckets() {
            return normalizedBuckets;
        }
    }
    
    private static final class NormalizedBucket<T> {
        
        private final String singleIdentifier;
        
        private final T singleValue;
        
        private final Collection<String> identifiers;
        
        private final String singleUnquotedIdentifier;
        
        private final T singleUnquotedValue;
        
        private final Collection<String> unquotedIdentifiers;
        
        private NormalizedBucket(final String singleIdentifier, final T singleValue, final Collection<String> identifiers,
                                 final String singleUnquotedIdentifier, final T singleUnquotedValue, final Collection<String> unquotedIdentifiers) {
            this.singleIdentifier = singleIdentifier;
            this.singleValue = singleValue;
            this.identifiers = identifiers;
            this.singleUnquotedIdentifier = singleUnquotedIdentifier;
            this.singleUnquotedValue = singleUnquotedValue;
            this.unquotedIdentifiers = unquotedIdentifiers;
        }
        
        private boolean hasSingleIdentifier() {
            return null != singleIdentifier;
        }
        
        private String getSingleIdentifier() {
            return singleIdentifier;
        }
        
        private T getSingleValue() {
            return singleValue;
        }
        
        private Collection<String> getIdentifiers() {
            return identifiers;
        }
        
        private boolean hasUnquotedIdentifier() {
            return null != singleUnquotedIdentifier;
        }
        
        private boolean hasSingleUnquotedIdentifier() {
            return null == unquotedIdentifiers && null != singleUnquotedIdentifier;
        }
        
        private T getSingleUnquotedValue() {
            return singleUnquotedValue;
        }
        
        private Collection<String> getUnquotedIdentifiers() {
            return unquotedIdentifiers;
        }
    }
}
