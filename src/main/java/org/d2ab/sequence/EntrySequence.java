/*
 * Copyright 2016 Daniel Skogquist Åborg
 *
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
 */

package org.d2ab.sequence;

import org.d2ab.function.QuaternaryFunction;
import org.d2ab.function.QuaternaryPredicate;
import org.d2ab.iterable.ChainingIterable;
import org.d2ab.iterable.Iterables;
import org.d2ab.iterator.*;
import org.d2ab.util.Entries;
import org.d2ab.util.Pair;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyIterator;

/**
 * An {@link Iterable} sequence of elements with {@link Stream}-like operations for refining, transforming and
 * collating the list of elements.
 */
@FunctionalInterface
public interface EntrySequence<K, V> extends Iterable<Entry<K, V>> {
	/**
	 * Create an empty {@code EntrySequence} with no items.
	 *
	 * @see #of(Entry)
	 * @see #of(Entry...)
	 * @see #ofEntry(Object, Object)
	 * @see #ofEntries(Object...)
	 * @see #from(Iterable)
	 */
	static <K, V> EntrySequence<K, V> empty() {
		return from(emptyIterator());
	}

	/**
	 * Create an {@code EntrySequence} with one {@link Entry}.
	 *
	 * @see #of(Entry...)
	 * @see #ofEntry(Object, Object)
	 * @see #ofEntries(Object...)
	 * @see #from(Iterable)
	 */
	static <K, V> EntrySequence<K, V> of(Entry<K, V> item) {
		return from(Collections.singleton(item));
	}

	/**
	 * Create an {@code EntrySequence} with the given {@link Entry} list.
	 *
	 * @see #of(Entry)
	 * @see #ofEntry(Object, Object)
	 * @see #ofEntries(Object...)
	 * @see #from(Iterable)
	 */
	@SafeVarargs
	static <K, V> EntrySequence<K, V> of(Entry<K, V>... items) {
		return from(asList(items));
	}

	/**
	 * Create an {@code EntrySequence} with one {@link Entry} of the given key and value.
	 *
	 * @see #ofEntries(Object...)
	 * @see #of(Entry)
	 * @see #of(Entry...)
	 * @see #from(Iterable)
	 */
	static <K, V> EntrySequence<K, V> ofEntry(K left, V right) {
		return of(Entries.of(left, right));
	}

	/**
	 * Create an {@code EntrySequence} with an {@link Entry} list created from the given keys and values in sequence in
	 * the input array.
	 *
	 * @throws IllegalArgumentException if the array of keys and values is not of even length.
	 * @see #ofEntry(Object, Object)
	 * @see #of(Entry)
	 * @see #of(Entry...)
	 * @see #from(Iterable)
	 */
	@SuppressWarnings("unchecked")
	static <K, V> EntrySequence<K, V> ofEntries(Object... os) {
		if (os.length % 2 != 0)
			throw new IllegalArgumentException("Expected an even set of objects, but got: " + os.length);

		List<Entry<K, V>> entries = new ArrayList<>();
		for (int i = 0; i < os.length; i += 2)
			entries.add(Entries.of((K) os[i], (V) os[i + 1]));
		return from(entries);
	}

	/**
	 * Create an {@code EntrySequence} from an {@link Iterable} of entries.
	 *
	 * @see #of(Entry)
	 * @see #of(Entry...)
	 * @see #from(Iterable...)
	 */
	static <K, V> EntrySequence<K, V> from(Iterable<Entry<K, V>> iterable) {
		return iterable::iterator;
	}

	/**
	 * Create a concatenated {@code EntrySequence} from several {@link Iterable}s of entries which are concatenated
	 * together to form the stream of entries in the {@code EntrySequence}.
	 *
	 * @see #of(Entry)
	 * @see #of(Entry...)
	 * @see #from(Iterable)
	 */
	@SafeVarargs
	static <K, V> EntrySequence<K, V> from(Iterable<Entry<K, V>>... iterables) {
		return () -> new ChainingIterator<>(iterables);
	}

	/**
	 * Create an {@code EntrySequence} from an {@link Iterator} of entries. Note that {@code EntrySequence}s created
	 * from {@link Iterator}s cannot be passed over more than once. Further attempts will register the
	 * {@code EntrySequence} as empty.
	 *
	 * @see #of(Entry)
	 * @see #of(Entry...)
	 * @see #from(Iterable)
	 */
	static <K, V> EntrySequence<K, V> from(Iterator<Entry<K, V>> iterator) {
		return () -> iterator;
	}

	/**
	 * Create an {@code EntrySequence} from a {@link Stream} of entries. Note that {@code EntrySequence}s created from
	 * {@link Stream}s cannot be passed over more than once. Further attempts will cause an
	 * {@link IllegalStateException} when the {@link Stream} is requested again.
	 *
	 * @see #of(Entry)
	 * @see #of(Entry...)
	 * @see #from(Iterable)
	 * @see #from(Iterator)
	 */
	static <K, V> EntrySequence<K, V> from(Stream<Entry<K, V>> stream) {
		return stream::iterator;
	}

	/**
	 * Create an {@code EntrySequence} of {@link Map.Entry} key/value items from a {@link Map} of items. The resulting
	 * {@code EntrySequence} can be mapped using {@link Pair} items, which implement {@link Map.Entry} and can thus be
	 * processed as part of the {@code EntrySequence}'s transformation steps.
	 *
	 * @see #of
	 * @see #from(Iterable)
	 */
	static <K, V> EntrySequence<K, V> from(Map<K, V> map) {
		return map.entrySet()::iterator;
	}

	/**
	 * @return an infinite {@code EntrySequence} generated by repeatedly calling the given supplier. The returned
	 * {@code EntrySequence} never terminates naturally.
	 *
	 * @see #recurse(Entry, UnaryOperator)
	 * @see #recurse(Object, Object, BiFunction)
	 * @see #endingAt(Entry)
	 * @see #until(Entry)
	 */
	static <K, V> EntrySequence<K, V> generate(Supplier<Entry<K, V>> supplier) {
		return () -> (InfiniteIterator<Entry<K, V>>) supplier::get;
	}

	/**
	 * Returns an {@code EntrySequence} produced by recursively applying the given operation to the given seeds, which
	 * form the first element of the sequence, the second being {@code f(keySeed, valueSeed)}, the third
	 * {@code f(f(keySeed, valueSeed))} and so on. The returned {@code EntrySequence} never terminates naturally.
	 *
	 * @return an {@code EntrySequence} produced by recursively applying the given operation to the given seed
	 *
	 * @see #recurse(Entry, UnaryOperator)
	 * @see #generate(Supplier)
	 * @see #endingAt(Entry)
	 * @see #until(Entry)
	 */
	static <K, V> EntrySequence<K, V> recurse(K keySeed, V valueSeed, BiFunction<K, V, ? extends Entry<K, V>> op) {
		return recurse(Entries.of(keySeed, valueSeed), Entries.asUnaryOperator(op));
	}

	/**
	 * Returns an {@code EntrySequence} produced by recursively applying the given operation to the given seed, which
	 * form the first element of the sequence, the second being {@code f(seed)}, the third {@code f(f(seed))} and so
	 * on. The returned {@code EntrySequence} never terminates naturally.
	 *
	 * @return an {@code EntrySequence} produced by recursively applying the given operation to the given seed
	 *
	 * @see #recurse(Entry, UnaryOperator)
	 * @see #recurse(Object, Object, BiFunction)
	 * @see #generate(Supplier)
	 * @see #endingAt(Entry)
	 * @see #until(Entry)
	 */
	static <K, V> EntrySequence<K, V> recurse(Entry<K, V> entry, UnaryOperator<Entry<K, V>> unaryOperator) {
		return () -> new RecursiveIterator<>(entry, unaryOperator);
	}

	/**
	 * Returns an {@code EntrySequence} produced by recursively applying the given mapper {@code f} and incrementer
	 * {@code g} operations to the given seeds, the first element being {@code f(keySeed, valueSeed)}, the second
	 * being {@code f(g(f(keySeed, valueSeed)))}, the third {@code f(g(f(g(f(keySeed, valueSeed)))))} and so on.
	 * The returned {@code EntrySequence} never terminates naturally.
	 *
	 * @param f a mapper function for producing elements that are to be included in the sequence, the first being
	 *          f(keySeed, valueSeed)
	 * @param g an incrementer function for producing the next unmapped element to be included in the sequence,
	 *          applied to the first mapped element f(keySeed, valueSeed) to produce the second unmapped value
	 *
	 * @return an {@code EntrySequence} produced by recursively applying the given mapper and incrementer operations
	 * to the
	 * given seeds
	 *
	 * @see #recurse(Entry, UnaryOperator)
	 * @see #recurse(Object, Object, BiFunction)
	 * @see #endingAt(Entry)
	 * @see #until(Entry)
	 */
	static <K, V, KK, VV> EntrySequence<KK, VV> recurse(K keySeed, V valueSeed,
	                                                    BiFunction<? super K, ? super V, ? extends Entry<KK, VV>> f,
	                                                    BiFunction<? super KK, ? super VV, ? extends Entry<K, V>> g) {
		return () -> new RecursiveIterator<>(f.apply(keySeed, valueSeed), Entries.asUnaryOperator(f, g));
	}

	/**
	 * Map the entries in this {@code EntrySequence} to another set of entries specified by the given {@code mapper}
	 * function.
	 *
	 * @see #map(Function)
	 * @see #map(Function, Function)
	 * @see #flatten(Function)
	 */
	default <KK, VV> EntrySequence<KK, VV> map(BiFunction<? super K, ? super V, ? extends Entry<KK, VV>> mapper) {
		return map(Entries.asFunction(mapper));
	}

	/**
	 * Map the entries in this {@code EntrySequence} to another set of entries specified by the given {@code mapper}
	 * function.
	 *
	 * @see #map(BiFunction)
	 * @see #map(Function, Function)
	 * @see #flatten(Function)
	 */
	default <KK, VV> EntrySequence<KK, VV> map(Function<? super Entry<K, V>, ? extends Entry<KK, VV>> mapper) {
		return () -> new MappingIterator<>(iterator(), mapper);
	}

	/**
	 * Map the entries in this {@code EntrySequence} to another set of entries specified by the given {@code keyMapper}
	 * amd {@code valueMapper} functions.
	 *
	 * @see #map(BiFunction)
	 * @see #map(Function)
	 * @see #flatten(Function)
	 */
	default <KK, VV> EntrySequence<KK, VV> map(Function<? super K, ? extends KK> keyMapper,
	                                           Function<? super V, ? extends VV> valueMapper) {
		return map(Entries.asFunction(keyMapper, valueMapper));
	}

	/**
	 * Skip a set number of steps in this {@code EntrySequence}.
	 */
	default EntrySequence<K, V> skip(int skip) {
		return () -> new SkippingIterator<>(iterator(), skip);
	}

	/**
	 * Limit the maximum number of results returned by this {@code EntrySequence}.
	 */
	default EntrySequence<K, V> limit(int limit) {
		return () -> new LimitingIterator<>(iterator(), limit);
	}

	/**
	 * Filter the elements in this {@code EntrySequence}, keeping only the elements that match the given
	 * {@link BiPredicate}.
	 */
	default EntrySequence<K, V> filter(BiPredicate<? super K, ? super V> predicate) {
		return filter(Entries.asPredicate(predicate));
	}

	/**
	 * Filter the elements in this {@code EntrySequence}, keeping only the entries that match the given
	 * {@link Predicate}.
	 */
	default EntrySequence<K, V> filter(Predicate<? super Entry<K, V>> predicate) {
		return () -> new FilteringIterator<>(iterator(), predicate);
	}

	/**
	 * Flatten the elements in this {@code EntrySequence} according to the given mapper {@link BiFunction}. The
	 * resulting {@code EntrySequence} contains the elements that is the result of applying the mapper
	 * {@link BiFunction} to each element, appended together inline as a single {@code EntrySequence}.
	 *
	 * @see #flatten(Function)
	 * @see #flattenKeys(Function)
	 * @see #flattenValues(Function)
	 * @see #map(BiFunction)
	 * @see #map(Function)
	 */
	default <KK, VV> EntrySequence<KK, VV> flatten(
			BiFunction<? super K, ? super V, ? extends Iterable<Entry<KK, VV>>> mapper) {
		return flatten(Entries.asFunction(mapper));
	}

	/**
	 * Flatten the elements in this {@code EntrySequence} according to the given mapper {@link Function}. The
	 * resulting {@code EntrySequence} contains the entries that is the result of applying the mapper
	 * {@link Function} to each entry, appended together inline as a single {@code EntrySequence}.
	 *
	 * @see #flatten(BiFunction)
	 * @see #flattenKeys(Function)
	 * @see #flattenValues(Function)
	 * @see #map(BiFunction)
	 * @see #map(Function)
	 */
	default <KK, VV> EntrySequence<KK, VV> flatten(
			Function<? super Entry<K, V>, ? extends Iterable<Entry<KK, VV>>> mapper) {
		ChainingIterable<Entry<KK, VV>> result = new ChainingIterable<>();
		toSequence(mapper).forEach(result::append);
		return result::iterator;
	}

	/**
	 * Flatten the keys of each entry in this sequence, applying multiples of keys returned by the given
	 * mapper to the same value of each entry.
	 *
	 * @see #flattenValues(Function)
	 * @see #flatten(Function)
	 * @see #flatten(BiFunction)
	 */
	default <KK> EntrySequence<KK, V> flattenKeys(Function<? super Entry<K, V>, ? extends Iterable<KK>> mapper) {
		return () -> new KeyFlatteningEntryIterator<>(iterator(), mapper);
	}

	/**
	 * Flatten the values of each entry in this sequence, applying multiples of values returned by the given
	 * mapper to the same key of each entry.
	 *
	 * @see #flattenKeys(Function)
	 * @see #flatten(Function)
	 * @see #flatten(BiFunction)
	 */
	default <VV> EntrySequence<K, VV> flattenValues(Function<? super Entry<K, V>, ? extends Iterable<VV>> mapper) {
		return () -> new ValueFlatteningEntryIterator<>(iterator(), mapper);
	}

	/**
	 * Terminate this {@code EntrySequence} just before the given element is encountered, not including the element in
	 * the {@code EntrySequence}.
	 *
	 * @see #until(Predicate)
	 * @see #endingAt(Entry)
	 * @see #generate(Supplier)
	 * @see #recurse
	 * @see #repeat()
	 */
	default EntrySequence<K, V> until(Entry<K, V> terminal) {
		return () -> new ExclusiveTerminalIterator<>(iterator(), terminal);
	}

	/**
	 * Terminate this {@code EntrySequence} when the given element is encountered, including the element as the last
	 * element in the {@code EntrySequence}.
	 *
	 * @see #endingAt(Predicate)
	 * @see #until(Entry)
	 * @see #generate(Supplier)
	 * @see #recurse(Entry, UnaryOperator)
	 * @see #repeat()
	 */
	default EntrySequence<K, V> endingAt(Entry<K, V> terminal) {
		return () -> new InclusiveTerminalIterator<>(iterator(), terminal);
	}

	/**
	 * Terminate this {@code EntrySequence} just before the entry with the given key and value is encountered,
	 * not including the entry in the {@code EntrySequence}.
	 *
	 * @see #until(Entry)
	 * @see #until(Predicate)
	 * @see #until(BiPredicate)
	 * @see #endingAt(Entry)
	 * @see #generate(Supplier)
	 * @see #recurse(Entry, UnaryOperator)
	 * @see #repeat()
	 */
	default EntrySequence<K, V> until(K key, V value) {
		return until(Entries.of(key, value));
	}

	/**
	 * Terminate this {@code EntrySequence} when the entry the given key and value is encountered,
	 * including the element as the last element in the {@code EntrySequence}.
	 *
	 * @see #endingAt(Entry)
	 * @see #endingAt(Predicate)
	 * @see #endingAt(BiPredicate)
	 * @see #until(Entry)
	 * @see #generate(Supplier)
	 * @see #recurse(Entry, UnaryOperator)
	 * @see #repeat()
	 */
	default EntrySequence<K, V> endingAt(K key, V value) {
		return endingAt(Entries.of(key, value));
	}

	/**
	 * Terminate this {@code EntrySequence} just before the given predicate is satisfied, not including the element
	 * that
	 * satisfies the predicate in the {@code EntrySequence}.
	 *
	 * @see #until(Predicate)
	 * @see #until(Object, Object)
	 * @see #until(Entry)
	 * @see #endingAt(Predicate)
	 * @see #generate(Supplier)
	 * @see #recurse(Entry, UnaryOperator)
	 * @see #repeat()
	 */
	default EntrySequence<K, V> until(BiPredicate<? super K, ? super V> terminal) {
		return until(Entries.asPredicate(terminal));
	}

	/**
	 * Terminate this {@code EntrySequence} when the given predicate is satisfied, including the element that satisfies
	 * the predicate as the last element in the {@code EntrySequence}.
	 *
	 * @see #endingAt(Predicate)
	 * @see #endingAt(Object, Object)
	 * @see #endingAt(Entry)
	 * @see #until(Predicate)
	 * @see #generate(Supplier)
	 * @see #recurse(Entry, UnaryOperator)
	 * @see #repeat()
	 */
	default EntrySequence<K, V> endingAt(BiPredicate<? super K, ? super V> terminal) {
		return endingAt(Entries.asPredicate(terminal));
	}

	/**
	 * Terminate this {@code EntrySequence} just before the given predicate is satisfied, not including the element
	 * that
	 * satisfies the predicate in the {@code EntrySequence}.
	 *
	 * @see #until(BiPredicate)
	 * @see #until(Entry)
	 * @see #endingAt(Predicate)
	 * @see #generate(Supplier)
	 * @see #recurse(Entry, UnaryOperator)
	 * @see #repeat()
	 */
	default EntrySequence<K, V> until(Predicate<? super Entry<K, V>> terminal) {
		return () -> new ExclusiveTerminalIterator<>(iterator(), terminal);
	}

	/**
	 * Terminate this {@code EntrySequence} when the given predicate is satisfied, including the element that satisfies
	 * the predicate as the last element in the {@code EntrySequence}.
	 *
	 * @see #endingAt(BiPredicate)
	 * @see #endingAt(Entry)
	 * @see #until(Predicate)
	 * @see #generate(Supplier)
	 * @see #recurse(Entry, UnaryOperator)
	 * @see #repeat()
	 */
	default EntrySequence<K, V> endingAt(Predicate<? super Entry<K, V>> terminal) {
		return () -> new InclusiveTerminalIterator<>(iterator(), terminal);
	}

	/**
	 * Collect the entries in this {@code EntrySequence} into an array.
	 */
	default Entry<K, V>[] toArray() {
		return toArray(Entry[]::new);
	}

	/**
	 * Collect the entries in this {@code EntrySequence} into an array of the type determined by the given array
	 * constructor.
	 */
	default Entry<K, V>[] toArray(IntFunction<Entry<K, V>[]> constructor) {
		List list = toList();
		@SuppressWarnings("unchecked")
		Entry<K, V>[] array = (Entry<K, V>[]) list.toArray(constructor.apply(list.size()));
		return array;
	}

	/**
	 * Collect the entries in this {@code EntrySequence} into a {@link List}.
	 */
	default List<Entry<K, V>> toList() {
		return toList(ArrayList::new);
	}

	/**
	 * Collect the entries in this {@code EntrySequence} into a {@link List} of the type determined by the given
	 * constructor.
	 */
	default List<Entry<K, V>> toList(Supplier<List<Entry<K, V>>> constructor) {
		return toCollection(constructor);
	}

	/**
	 * Collect the entries in this {@code EntrySequence} into a {@link Set}.
	 */
	default Set<Entry<K, V>> toSet() {
		return toSet(HashSet::new);
	}

	/**
	 * Collect the entries in this {@code EntrySequence} into a {@link Set} of the type determined by the given
	 * constructor.
	 */
	default <S extends Set<Entry<K, V>>> S toSet(Supplier<? extends S> constructor) {
		return toCollection(constructor);
	}

	/**
	 * Collect the entries in this {@code EntrySequence} into a {@link SortedSet}.
	 */
	default SortedSet<Entry<K, V>> toSortedSet() {
		return toSet(TreeSet::new);
	}

	/**
	 * Collect the entries in this {@code EntrySequence} into a {@link Map}.
	 */
	default Map<K, V> toMap() {
		return toMap(HashMap::new);
	}

	/**
	 * Collect the entries in this {@code EntrySequence} into a {@link Map} of the type determined by the given
	 * constructor.
	 */
	default <M extends Map<K, V>> M toMap(Supplier<? extends M> constructor) {
		return collect(constructor, Entries::put);
	}

	/**
	 * Collect the entries in this {@code EntrySequence} into a {@link SortedMap}.
	 */
	default SortedMap<K, V> toSortedMap() {
		return toMap(TreeMap::new);
	}

	/**
	 * Collect this {@code EntrySequence} into a {@link Collection} of the type determined by the given constructor.
	 */
	default <C extends Collection<Entry<K, V>>> C toCollection(Supplier<? extends C> constructor) {
		return collect(constructor, Collection::add);
	}

	/**
	 * Collect this {@code EntrySequence} into an arbitrary container using the given constructor and adder.
	 */
	default <C> C collect(Supplier<? extends C> constructor, BiConsumer<? super C, ? super Entry<K, V>> adder) {
		return collectInto(constructor.get(), adder);
	}

	/**
	 * Collect this {@code EntrySequence} into an arbitrary container using the given {@link Collector}.
	 */
	default <S, R> S collect(Collector<Entry<K, V>, R, S> collector) {
		R intermediary = collect(collector.supplier(), collector.accumulator());
		return collector.finisher().apply(intermediary);
	}

	/**
	 * Collect this {@code EntrySequence} into the given {@link Collection}.
	 */
	default <U extends Collection<Entry<K, V>>> U collectInto(U collection) {
		return collectInto(collection, Collection::add);
	}

	/**
	 * Collect this {@code EntrySequence} into the given container, using the given adder.
	 */
	default <C> C collectInto(C result, BiConsumer<? super C, ? super Entry<K, V>> adder) {
		forEach(entry -> adder.accept(result, entry));
		return result;
	}

	/**
	 * Join this {@code EntrySequence} into a string separated by the given delimiter.
	 */
	default String join(String delimiter) {
		return join("", delimiter, "");
	}

	/**
	 * Join this {@code EntrySequence} into a string separated by the given delimiter, with the given prefix and
	 * suffix.
	 */
	default String join(String prefix, String delimiter, String suffix) {
		StringBuilder result = new StringBuilder();
		result.append(prefix);
		boolean first = true;
		for (Entry<K, V> each : this) {
			if (first)
				first = false;
			else
				result.append(delimiter);
			result.append(each);
		}
		result.append(suffix);
		return result.toString();
	}

	/**
	 * Reduce this {@code EntrySequence} into a single element by iteratively applying the given binary operator to
	 * the current result and each entry in this sequence.
	 */
	default Optional<Entry<K, V>> reduce(BinaryOperator<Entry<K, V>> operator) {
		return Iterators.reduce(iterator(), operator);
	}

	/**
	 * Reduce this {@code EntrySequence} into a single element by iteratively applying the given function to
	 * the current result and each entry in this sequence. The function is passed the key and value of the result,
	 * followed by the keys and values of the current entry, respectively.
	 */
	default Optional<Entry<K, V>> reduce(QuaternaryFunction<K, V, K, V, Entry<K, V>> operator) {
		return reduce(Entries.asBinaryOperator(operator));
	}

	/**
	 * Reduce this {@code EntrySequence} into a single element by iteratively applying the given binary operator to
	 * the current result and each entry in this sequence, starting with the given identity as the initial result.
	 */
	default Entry<K, V> reduce(Entry<K, V> identity, BinaryOperator<Entry<K, V>> operator) {
		return Iterators.reduce(iterator(), identity, operator);
	}

	/**
	 * Reduce this {@code EntrySequence} into a single element by iteratively applying the given binary operator to
	 * the current result and each entry in this sequence, starting with the given identity as the initial result.
	 * The function is passed the key and value of the result, followed by the keys and values of the current entry,
	 * respectively.
	 */
	default Entry<K, V> reduce(K key, V value, QuaternaryFunction<K, V, K, V, Entry<K, V>> operator) {
		return reduce(Entries.of(key, value), Entries.asBinaryOperator(operator));
	}

	/**
	 * @return the first entry of this {@code EntrySequence} or an empty {@link Optional} if there are no entries in
	 * the {@code EntrySequence}.
	 */
	default Optional<Entry<K, V>> first() {
		return get(0);
	}

	/**
	 * @return the second entry of this {@code EntrySequence} or an empty {@link Optional} if there are one or less
	 * entries in the {@code EntrySequence}.
	 */
	default Optional<Entry<K, V>> second() {
		return get(1);
	}

	/**
	 * @return the third entry of this {@code EntrySequence} or an empty {@link Optional} if there are two or less
	 * entries in the {@code EntrySequence}.
	 */
	default Optional<Entry<K, V>> third() {
		return get(2);
	}

	/**
	 * @return the element at the given index, or an empty {@link Optional} if the {@code EntrySequence} is smaller
	 * than the index.
	 */
	default Optional<Entry<K, V>> get(long index) {
		return Iterators.get(iterator(), index);
	}

	/**
	 * @return the last entry of this {@code EntrySequence} or an empty {@link Optional} if there are no entries in
	 * the {@code EntrySequence}.
	 */
	default Optional<Entry<K, V>> last() {
		Iterator<Entry<K, V>> iterator = iterator();
		if (!iterator.hasNext())
			return Optional.empty();

		Entry<K, V> last;
		do {
			last = iterator.next();
		} while (iterator.hasNext());

		return Optional.of(last);
	}

	/**
	 * Window the elements of this {@code EntrySequence} into a {@link Sequence} of {@code EntrySequence}s of entrues,
	 * each with the size of the given window. The first item in each sequence is the second item in the previous
	 * sequence. The final sequence may be shorter than the window. This method is equivalent to
	 * {@code window(window, 1)}.
	 */
	default Sequence<EntrySequence<K, V>> window(int window) {
		return window(window, 1);
	}

	/**
	 * Window the elements of this {@code EntrySequence} into a sequence of {@code EntrySequence}s of elements, each
	 * with the size of the given window, stepping {@code step} elements between each window. If the given step is less
	 * than the window size, the windows will overlap each other. If the step is larger than the window size, elements
	 * will be skipped in between windows.
	 */
	default Sequence<EntrySequence<K, V>> window(int window, int step) {
		return () -> new WindowingIterator<Entry<K, V>, EntrySequence<K, V>>(iterator(), window, step) {
			@Override
			protected EntrySequence<K, V> toSequence(List<Entry<K, V>> list) {
				return EntrySequence.from(list);
			}
		};
	}

	/**
	 * Batch the elements of this {@code EntrySequence} into a sequence of {@code EntrySequence}s of distinct elements,
	 * each with the given batch size. This method is equivalent to {@code window(size, size)}.
	 */
	default Sequence<EntrySequence<K, V>> batch(int size) {
		return window(size, size);
	}

	/**
	 * Batch the elements of this {@code EntrySequence} into a sequence of {@link EntrySequence}s of distinct elements,
	 * where the given predicate determines where to split the lists of partitioned elements. The predicate is given
	 * the current and next item in the iteration, and if it returns true a partition is created between the elements.
	 */
	default Sequence<EntrySequence<K, V>> batch(BiPredicate<? super Entry<K, V>, ? super Entry<K, V>> predicate) {
		return () -> new PredicatePartitioningIterator<Entry<K, V>, EntrySequence<K, V>>(iterator(), predicate) {
			@Override
			protected EntrySequence<K, V> toSequence(List<Entry<K, V>> list) {
				return EntrySequence.from(list);
			}
		};
	}

	/**
	 * Batch the elements of this {@code EntrySequence} into a sequence of {@link EntrySequence}s of distinct elements,
	 * where the given predicate determines where to split the lists of partitioned elements. The predicate is given
	 * the keys and values of the current and next items in the iteration, and if it returns true a partition is
	 * created between the elements.
	 */
	default Sequence<EntrySequence<K, V>> batch(
			QuaternaryPredicate<? super K, ? super V, ? super K, ? super V> predicate) {
		return batch((e1, e2) -> predicate.test(e1.getKey(), e1.getValue(), e2.getKey(), e2.getValue()));
	}

	/**
	 * Skip x number of steps in between each invocation of the iterator of this {@code EntrySequence}.
	 */
	default EntrySequence<K, V> step(int step) {
		return () -> new SteppingIterator<>(iterator(), step);
	}

	/**
	 * @return an {@code EntrySequence} where each item in this {@code EntrySequence} occurs only once, the first time
	 * it is encountered.
	 */
	default EntrySequence<K, V> distinct() {
		return () -> new DistinctIterator<>(iterator());
	}

	/**
	 * @return this {@code EntrySequence} sorted according to the natural order.
	 */
	default EntrySequence<K, V> sorted() {
		return () -> new SortingIterator<>(iterator());
	}

	/**
	 * @return this {@code EntrySequence} sorted according to the given {@link Comparator}.
	 */
	default EntrySequence<K, V> sorted(Comparator<? super Entry<? extends K, ? extends V>> comparator) {
		return () -> new SortingIterator<>(iterator(), comparator);
	}

	/**
	 * @return the minimal element in this {@code EntrySequence} according to the given {@link Comparator}.
	 */
	default Optional<Entry<K, V>> min(Comparator<? super Entry<? extends K, ? extends V>> comparator) {
		return reduce(BinaryOperator.minBy(comparator));
	}

	/**
	 * @return the maximum element in this {@code EntrySequence} according to the given {@link Comparator}.
	 */
	default Optional<Entry<K, V>> max(Comparator<? super Entry<? extends K, ? extends V>> comparator) {
		return reduce(BinaryOperator.maxBy(comparator));
	}

	/**
	 * @return the count of elements in this {@code EntrySequence}.
	 */
	default int count() {
		int count = 0;
		for (Entry<K, V> ignored : this)
			count++;
		return count;
	}

	/**
	 * @return this {@code EntrySequence} as a {@link Stream} of entries.
	 */
	default Stream<Entry<K, V>> stream() {
		return StreamSupport.stream(spliterator(), false);
	}

	/**
	 * @return true if all elements in this {@code EntrySequence} satisfy the given predicate, false otherwise.
	 */
	default boolean all(BiPredicate<? super K, ? super V> biPredicate) {
		return Iterables.all(this, Entries.asPredicate(biPredicate));
	}

	/**
	 * @return true if no elements in this {@code EntrySequence} satisfy the given predicate, false otherwise.
	 */
	default boolean none(BiPredicate<? super K, ? super V> predicate) {
		return !any(predicate);
	}

	/**
	 * @return true if any element in this {@code EntrySequence} satisfies the given predicate, false otherwise.
	 */
	default boolean any(BiPredicate<? super K, ? super V> biPredicate) {
		return Iterables.any(this, Entries.asPredicate(biPredicate));
	}

	/**
	 * Allow the given {@link Consumer} to see each element in this {@code EntrySequence} as it is traversed.
	 */
	default EntrySequence<K, V> peek(BiConsumer<K, V> action) {
		Consumer<? super Entry<K, V>> consumer = Entries.asConsumer(action);
		return () -> new PeekingIterator<>(iterator(), consumer);
	}

	/**
	 * Append the elements of the given {@link Iterator} to the end of this {@code EntrySequence}.
	 * <p>
	 * The appended elements will only be available on the first traversal of the resulting {@code Sequence}.
	 */
	default EntrySequence<K, V> append(Iterator<? extends Entry<K, V>> iterator) {
		return append(Iterables.from(iterator));
	}

	/**
	 * Append the elements of the given {@link Iterable} to the end of this {@code EntrySequence}.
	 */
	default EntrySequence<K, V> append(Iterable<? extends Entry<K, V>> that) {
		@SuppressWarnings("unchecked")
		Iterable<Entry<K, V>> chainingSequence = new ChainingIterable<>(this, that);
		return chainingSequence::iterator;
	}

	/**
	 * Append the given elements to the end of this {@code EntrySequence}.
	 */
	@SuppressWarnings("unchecked")
	default EntrySequence<K, V> append(Entry<K, V>... entries) {
		return append(Iterables.from(entries));
	}

	/**
	 * Append the given entry to the end of this {@code EntrySequence}.
	 */
	@SuppressWarnings("unchecked")
	default EntrySequence<K, V> appendEntry(K key, V value) {
		return append(Entries.of(key, value));
	}

	/**
	 * Append the elements of the given {@link Stream} to the end of this {@code EntrySequence}.
	 * <p>
	 * The resulting {@code BiSequence} can only be traversed once, further attempts to traverse will results in a
	 * {@link IllegalStateException}.
	 */
	default EntrySequence<K, V> append(Stream<Entry<K, V>> stream) {
		return append(Iterables.from(stream));
	}

	/**
	 * Convert this {@code EntrySequence} to a {@link Sequence} of {@link Entry} elements.
	 */
	default Sequence<Entry<K, V>> toSequence() {
		return Sequence.from(this);
	}

	/**
	 * Convert this {@code EntrySequence} to a {@link Sequence} where each item is generated by the given mapper.
	 */
	default <T> Sequence<T> toSequence(BiFunction<? super K, ? super V, ? extends T> mapper) {
		return toSequence(Entries.asFunction(mapper));
	}

	/**
	 * Convert this {@code EntrySequence} to a {@link Sequence} where each item is generated by the given mapper.
	 */
	default <T> Sequence<T> toSequence(Function<? super Entry<K, V>, ? extends T> mapper) {
		return () -> new MappingIterator<>(iterator(), mapper);
	}

	/**
	 * Repeat this {@code EntrySequence} forever, producing a sequence that never terminates unless the original
	 * sequence is empty in which case the resulting sequence is also empty.
	 */
	default EntrySequence<K, V> repeat() {
		return () -> new RepeatingIterator<>(this, -1);
	}

	/**
	 * Repeat this {@code EntrySequence} the given number of times.
	 */
	default EntrySequence<K, V> repeat(long times) {
		return () -> new RepeatingIterator<>(this, times);
	}

	/**
	 * @return an {@code EntrySequence} which iterates over this {@code EntrySequence} in reverse order.
	 */
	default EntrySequence<K, V> reverse() {
		return () -> new ReverseIterator<>(iterator());
	}

	/**
	 * @return an {@code EntrySequence} which iterates over this {@code EntrySequence} in random order.
	 */
	default EntrySequence<K, V> shuffle() {
		return () -> {
			List<Entry<K, V>> list = toList();
			Collections.shuffle(list);
			return list.iterator();
		};
	}

	/**
	 * @return an {@code EntrySequence} which iterates over this {@code EntrySequence} in random order as determined by the
	 * given random generator.
	 */
	default EntrySequence<K, V> shuffle(Random md) {
		return () -> {
			List<Entry<K, V>> list = toList();
			Collections.shuffle(list, md);
			return list.iterator();
		};
	}

	/**
	 * Remove all elements matched by this sequence using {@link Iterator#remove()}.
	 */
	default void removeAll() {
		Iterables.removeAll(this);
	}
}
