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

import org.d2ab.collection.*;
import org.d2ab.function.*;
import org.d2ab.iterator.*;
import org.d2ab.iterator.chars.CharIterator;
import org.d2ab.iterator.doubles.DoubleIterator;
import org.d2ab.iterator.ints.IntIterator;
import org.d2ab.iterator.longs.LongIterator;
import org.d2ab.util.Pair;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.function.BinaryOperator.maxBy;
import static java.util.function.BinaryOperator.minBy;
import static org.d2ab.util.Preconditions.requireAtLeastOne;
import static org.d2ab.util.Preconditions.requireAtLeastZero;

/**
 * An {@link Iterable} sequence of {@link Entry} elements with {@link Stream}-like operations for refining,
 * transforming and collating the list of {@link Entry} elements.
 */
@FunctionalInterface
public interface EntrySequence<K, V> extends IterableCollection<Entry<K, V>> {
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
		return Iterables.<Entry<K, V>>empty()::iterator;
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
		return from(Lists.of(items));
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
		return of(Maps.entry(left, right));
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
	static <K, V> EntrySequence<K, V> ofEntries(Object... items) {
		requireNonNull(items, "items");
		if (items.length % 2 != 0)
			throw new IllegalArgumentException("Expected an even number of items: " + items.length);

		List<Entry<K, V>> entries = new ArrayList<>();
		for (int i = 0; i < items.length; i += 2)
			entries.add(Maps.entry((K) items[i], (V) items[i + 1]));
		return from(entries);
	}

	/**
	 * Create an {@code EntrySequence} from an {@link Iterable} of entries.
	 *
	 * @see #of(Entry)
	 * @see #of(Entry...)
	 * @see #from(Iterable...)
	 * @see #cache(Iterable)
	 */
	static <K, V> EntrySequence<K, V> from(Iterable<Entry<K, V>> iterable) {
		requireNonNull(iterable, "iterable");

		return iterable::iterator;
	}

	/**
	 * Create a concatenated {@code EntrySequence} from several {@link Iterable}s of entries which are concatenated
	 * together to form the stream of entries in the {@code EntrySequence}.
	 *
	 * @see #of(Entry)
	 * @see #of(Entry...)
	 * @see #from(Iterable)
	 * @deprecated Use {@link #concat(Iterable[])} instead.
	 */
	@SafeVarargs
	@Deprecated
	static <K, V> EntrySequence<K, V> from(Iterable<Entry<K, V>>... iterables) {
		return concat(iterables);
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
	static <K, V> EntrySequence<K, V> concat(Iterable<Entry<K, V>>... iterables) {
		requireNonNull(iterables, "iterables");
		for (Iterable<Entry<K, V>> iterable : iterables)
			requireNonNull(iterable, "each iterable");

		return () -> new ChainingIterator<>(iterables);
	}

	/**
	 * Create a once-only {@code EntrySequence} from an {@link Iterator} of entries. Note that {@code EntrySequence}s
	 * created from {@link Iterator}s cannot be passed over more than once. Further attempts will register the
	 * {@code EntrySequence} as empty.
	 *
	 * @see #of(Entry)
	 * @see #of(Entry...)
	 * @see #from(Iterable)
	 * @see #cache(Iterator)
	 * @since 1.1
	 */
	static <K, V> EntrySequence<K, V> once(Iterator<Entry<K, V>> iterator) {
		requireNonNull(iterator, "iterator");

		return from(Iterables.once(iterator));
	}

	/**
	 * Create a once-only {@code EntrySequence} from a {@link Stream} of entries. Note that {@code EntrySequence}s
	 * created from {@link Stream}s cannot be passed over more than once. Further attempts will register the
	 * {@code EntrySequence} as empty.
	 *
	 * @see #of(Entry)
	 * @see #of(Entry...)
	 * @see #from(Iterable)
	 * @see #once(Iterator)
	 * @see #cache(Stream)
	 * @since 1.1
	 */
	static <K, V> EntrySequence<K, V> once(Stream<Entry<K, V>> stream) {
		requireNonNull(stream, "stream");

		return once(stream.iterator());
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
		requireNonNull(map, "map");

		return from(map.entrySet());
	}

	/**
	 * Create an {@code EntrySequence} with a cached copy of an {@link Iterable} of entries.
	 *
	 * @see #cache(Iterator)
	 * @see #cache(Stream)
	 * @see #from(Iterable)
	 * @since 1.1
	 */
	static <K, V> EntrySequence<K, V> cache(Iterable<Entry<K, V>> iterable) {
		requireNonNull(iterable, "iterable");

		return from(Iterables.toList(iterable));
	}

	/**
	 * Create an {@code EntrySequence} with a cached copy of an {@link Iterator} of entries.
	 *
	 * @see #cache(Iterable)
	 * @see #cache(Stream)
	 * @see #once(Iterator)
	 * @since 1.1
	 */
	static <K, V> EntrySequence<K, V> cache(Iterator<Entry<K, V>> iterator) {
		requireNonNull(iterator, "iterator");

		return from(Iterators.toList(iterator));
	}

	/**
	 * Create an {@code EntrySequence} with a cached copy of a {@link Stream} of entries.
	 *
	 * @see #cache(Iterable)
	 * @see #cache(Iterator)
	 * @see #once(Stream)
	 * @since 1.1
	 */
	static <K, V> EntrySequence<K, V> cache(Stream<Entry<K, V>> stream) {
		requireNonNull(stream, "stream");

		return from(stream.collect(Collectors.toList()));
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
		requireNonNull(supplier, "supplier");

		return () -> (InfiniteIterator<Entry<K, V>>) supplier::get;
	}

	/**
	 * @return an infinite {@code EntrySequence} where each {@link #iterator()} is generated by polling for a supplier
	 * and then using it to generate the sequence of entries. The sequence never terminates.
	 *
	 * @see #generate(Supplier)
	 * @see #recurse(Entry, UnaryOperator)
	 * @see #endingAt(Entry)
	 * @see #until(Entry)
	 */
	static <K, V> EntrySequence<K, V> multiGenerate(
			Supplier<? extends Supplier<? extends Entry<K, V>>> multiSupplier) {
		requireNonNull(multiSupplier, "multiSupplier");

		return () -> {
			Supplier<? extends Entry<K, V>> supplier = requireNonNull(
					multiSupplier.get(), "multiSupplier.get()");
			return (InfiniteIterator<Entry<K, V>>) supplier::get;
		};
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
	static <K, V> EntrySequence<K, V> recurse(K keySeed, V valueSeed,
	                                          BiFunction<K, V, ? extends Entry<K, V>> operator) {
		requireNonNull(operator, "operator");

		return recurse(Maps.entry(keySeed, valueSeed), entry -> operator.apply(entry.getKey(), entry.getValue()));
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
	static <K, V> EntrySequence<K, V> recurse(Entry<K, V> seed, UnaryOperator<Entry<K, V>> operator) {
		requireNonNull(operator, "operator");

		return () -> new RecursiveIterator<>(seed, operator);
	}

	/**
	 * Returns an {@code EntrySequence} produced by recursively applying the given mapper {@code f} and incrementer
	 * {@code g} operations to the given seeds, the first element being {@code f(keySeed, valueSeed)}, the second
	 * being {@code f(g(f(keySeed, valueSeed)))}, the third {@code f(g(f(g(f(keySeed, valueSeed)))))} and so on.
	 * The returned {@code EntrySequence} never terminates naturally.
	 *
	 * @param f a mapper function for producing elements that are to be included in the sequence, the first being
	 *          f(keySeed, valueSeed)
	 * @param g an incrementer function for producing the next unmapped element to be included in the sequence, applied
	 *          to the first mapped element f(keySeed, valueSeed) to produce the second unmapped value
	 *
	 * @return an {@code EntrySequence} produced by recursively applying the given mapper and incrementer operations to
	 * the given seeds
	 *
	 * @see #recurse(Entry, UnaryOperator)
	 * @see #recurse(Object, Object, BiFunction)
	 * @see #endingAt(Entry)
	 * @see #until(Entry)
	 */
	static <K, V, KK, VV> EntrySequence<KK, VV> recurse(K keySeed, V valueSeed,
	                                                    BiFunction<? super K, ? super V, ? extends Entry<KK, VV>> f,
	                                                    BiFunction<? super KK, ? super VV, ? extends Entry<K, V>> g) {
		requireNonNull(f, "f");
		requireNonNull(g, "g");

		Function<Entry<K, V>, Entry<KK, VV>> f1 = asEntryFunction(f);
		Function<Entry<KK, VV>, Entry<K, V>> g1 = asEntryFunction(g);
		return recurse(f.apply(keySeed, valueSeed), f1.compose(g1)::apply);
	}

	static <K, V> BinaryOperator<Entry<K, V>> asEntryBinaryOperator(
			QuaternaryFunction<? super K, ? super V, ? super K, ? super V, ? extends Entry<K, V>> f) {
		return (e1, e2) -> f.apply(e1.getKey(), e1.getValue(), e2.getKey(), e2.getValue());
	}

	static <K, V, R> Function<Entry<K, V>, R> asEntryFunction(
			BiFunction<? super K, ? super V, ? extends R> mapper) {
		return entry -> mapper.apply(entry.getKey(), entry.getValue());
	}

	static <K, V> Predicate<Entry<K, V>> asEntryPredicate(BiPredicate<? super K, ? super V> predicate) {
		return entry -> predicate.test(entry.getKey(), entry.getValue());
	}

	static <K, V> Consumer<Entry<K, V>> asEntryConsumer(BiConsumer<? super K, ? super V> action) {
		return entry -> action.accept(entry.getKey(), entry.getValue());
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
		requireNonNull(mapper, "mapper");

		return map(asEntryFunction(mapper));
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
		requireNonNull(mapper, "mapper");

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
		requireNonNull(keyMapper, "keyMapper");
		requireNonNull(valueMapper, "valueMapper");

		return map(entry -> Maps.entry(keyMapper.apply(entry.getKey()), valueMapper.apply(entry.getValue())));
	}

	/**
	 * Map the entries in this {@code EntrySequence} to another set of entries specified by the given {@code mapper}
	 * function. In addition to the current entry, the mapper has access to the index of each entry.
	 *
	 * @see #map(Function)
	 * @see #flatten(Function)
	 * @since 1.2
	 */
	default <KK, VV> EntrySequence<KK, VV> mapIndexed(
			ObjIntFunction<? super Entry<K, V>, ? extends Entry<KK, VV>> mapper) {
		requireNonNull(mapper, "mapper");

		return () -> new IndexingMappingIterator<>(iterator(), mapper);
	}

	/**
	 * Map the entries in this {@code EntrySequence} to another set of entries specified by the given {@code mapper}
	 * function. In addition to the current entry, the mapper has access to the index of each entry.
	 *
	 * @see #map(Function)
	 * @see #flatten(Function)
	 * @since 1.2
	 */
	default <KK, VV> EntrySequence<KK, VV> mapIndexed(
			ObjObjIntFunction<? super K, ? super V, ? extends Entry<KK, VV>> mapper) {
		requireNonNull(mapper, "mapper");

		return mapIndexed((e, i) -> mapper.apply(e.getKey(), e.getValue(), i));
	}

	/**
	 * Map the keys of the entries in this {@code EntrySequence} to another set of keys specified by the given
	 * {@code mapper} function.
	 *
	 * @see #map(Function)
	 * @see #map(BiFunction)
	 * @see #map(Function, Function)
	 * @see #flatten(Function)
	 */
	default <KK> EntrySequence<KK, V> mapKeys(Function<? super K, ? extends KK> mapper) {
		return map((k, v) -> Maps.entry(mapper.apply(k), v));
	}

	/**
	 * Map the values of the entries in this {@code EntrySequence} to another set of values specified by the given
	 * {@code mapper} function.
	 *
	 * @see #map(Function)
	 * @see #map(BiFunction)
	 * @see #map(Function, Function)
	 * @see #flatten(Function)
	 */
	default <VV> EntrySequence<K, VV> mapValues(Function<? super V, ? extends VV> mapper) {
		return map((k, v) -> Maps.entry(k, mapper.apply(v)));
	}

	/**
	 * Skip a set number of steps in this {@code EntrySequence}.
	 */
	default EntrySequence<K, V> skip(int skip) {
		requireAtLeastZero(skip, "skip");

		if (skip == 0)
			return this;

		return () -> new SkippingIterator<>(iterator(), skip);
	}

	/**
	 * Skip a set number of steps at the end of this {@code EntrySequence}.
	 *
	 * @since 1.1
	 */
	default EntrySequence<K, V> skipTail(int skip) {
		requireAtLeastZero(skip, "skip");

		if (skip == 0)
			return this;

		return () -> new TailSkippingIterator<>(iterator(), skip);
	}

	/**
	 * Limit the maximum number of results returned by this {@code EntrySequence}.
	 */
	default EntrySequence<K, V> limit(int limit) {
		requireAtLeastZero(limit, "limit");

		if (limit == 0)
			return empty();

		return () -> new LimitingIterator<>(iterator(), limit);
	}

	/**
	 * Limit the results returned by this {@code EntrySequence} to the last {@code limit} entries.
	 *
	 * @since 2.3
	 */
	default EntrySequence<K, V> limitTail(int limit) {
		requireAtLeastZero(limit, "limit");

		if (limit == 0)
			return empty();

		return () -> new TailLimitingIterator<>(iterator(), limit);
	}

	/**
	 * Filter the elements in this {@code EntrySequence}, keeping only the elements that match the given
	 * {@link BiPredicate}.
	 */
	default EntrySequence<K, V> filter(BiPredicate<? super K, ? super V> predicate) {
		requireNonNull(predicate, "predicate");

		return filter(asEntryPredicate(predicate));
	}

	/**
	 * Filter the elements in this {@code EntrySequence}, keeping only the entries that match the given
	 * {@link Predicate}.
	 */
	default EntrySequence<K, V> filter(Predicate<? super Entry<K, V>> predicate) {
		requireNonNull(predicate, "predicate");

		return () -> new FilteringIterator<>(iterator(), predicate);
	}

	/**
	 * Filter the entries in this {@code EntrySequence}, keeping only the elements that match the given
	 * {@link ObjIntPredicate}, which is passed the current entry and its index in the sequence.
	 *
	 * @since 1.2
	 */
	default EntrySequence<K, V> filterIndexed(ObjIntPredicate<? super Entry<K, V>> predicate) {
		requireNonNull(predicate, "predicate");

		return () -> new IndexedFilteringIterator<>(iterator(), predicate);
	}

	/**
	 * Filter the entries in this {@code EntrySequence}, keeping only the elements that match the given
	 * {@link ObjIntPredicate}, which is passed the current entry and its index in the sequence.
	 *
	 * @since 1.2
	 */
	default EntrySequence<K, V> filterIndexed(ObjObjIntPredicate<? super K, ? super V> predicate) {
		requireNonNull(predicate, "predicate");

		return filterIndexed((e, i) -> predicate.test(e.getKey(), e.getValue(), i));
	}

	/**
	 * @return a {@code EntrySequence} containing only the entries found in the given target array.
	 *
	 * @since 1.2
	 */
	@SuppressWarnings("unchecked")
	default EntrySequence<K, V> including(Entry<K, V>... entries) {
		requireNonNull(entries, "entries");

		return filter(e -> Arrayz.contains(entries, e));
	}

	/**
	 * @return a {@code EntrySequence} containing only the entries found in the given target iterable.
	 *
	 * @since 1.2
	 */
	default EntrySequence<K, V> including(Iterable<? extends Entry<K, V>> entries) {
		requireNonNull(entries, "entries");

		return filter(e -> Iterables.contains(entries, e));
	}

	/**
	 * @return a {@code EntrySequence} containing only the entries not found in the given target array.
	 *
	 * @since 1.2
	 */
	@SuppressWarnings("unchecked")
	default EntrySequence<K, V> excluding(Entry<K, V>... entries) {
		return filter(e -> !Arrayz.contains(entries, e));
	}

	/**
	 * @return a {@code EntrySequence} containing only the entries not found in the given target iterable.
	 *
	 * @since 1.2
	 */
	default EntrySequence<K, V> excluding(Iterable<? extends Entry<K, V>> entries) {
		requireNonNull(entries, "entries");

		return filter(e -> !Iterables.contains(entries, e));
	}

	/**
	 * @return a {@link Sequence} of the {@link Entry} elements in this {@code EntrySequence} flattened into their key
	 * and value components strung together.
	 */
	@SuppressWarnings("unchecked")
	default <T> Sequence<T> flatten() {
		return toSequence().flatten(entry -> Iterables.fromEntry((Entry) entry));
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
		requireNonNull(mapper, "mapper");

		return flatten(asEntryFunction(mapper));
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
		requireNonNull(mapper, "mapper");

		return ChainingIterable.concat(toSequence(mapper))::iterator;
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
		requireNonNull(mapper, "mapper");

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
		requireNonNull(mapper, "mapper");

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
		return until(Maps.entry(key, value));
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
		return endingAt(Maps.entry(key, value));
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
	default EntrySequence<K, V> until(BiPredicate<? super K, ? super V> predicate) {
		requireNonNull(predicate, "predicate");

		return until(asEntryPredicate(predicate));
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
	default EntrySequence<K, V> endingAt(BiPredicate<? super K, ? super V> predicate) {
		requireNonNull(predicate, "predicate");

		return endingAt(asEntryPredicate(predicate));
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
	default EntrySequence<K, V> until(Predicate<? super Entry<K, V>> predicate) {
		requireNonNull(predicate, "predicate");

		return () -> new ExclusiveTerminalIterator<>(iterator(), predicate);
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
	default EntrySequence<K, V> endingAt(Predicate<? super Entry<K, V>> predicate) {
		requireNonNull(predicate, "predicate");

		return () -> new InclusiveTerminalIterator<>(iterator(), predicate);
	}

	/**
	 * Begin this {@code EntrySequence} just after the given Entry is encountered, not including the entry in the
	 * {@code EntrySequence}.
	 *
	 * @see #startingAfter(Predicate)
	 * @see #startingAfter(BiPredicate)
	 * @see #startingFrom(Entry)
	 * @since 1.1
	 */
	default EntrySequence<K, V> startingAfter(Entry<K, V> element) {
		return () -> new ExclusiveStartingIterator<>(iterator(), element);
	}

	/**
	 * Begin this {@code EntrySequence} when the given Entry is encountered, including the entry as the first element
	 * in the {@code EntrySequence}.
	 *
	 * @see #startingFrom(Predicate)
	 * @see #startingFrom(BiPredicate)
	 * @see #startingAfter(Entry)
	 * @since 1.1
	 */
	default EntrySequence<K, V> startingFrom(Entry<K, V> element) {
		return () -> new InclusiveStartingIterator<>(iterator(), element);
	}

	/**
	 * Begin this {@code EntrySequence} just after the given predicate is satisfied, not including the entry that
	 * satisfies the predicate in the {@code EntrySequence}.
	 *
	 * @see #startingAfter(BiPredicate)
	 * @see #startingAfter(Entry)
	 * @see #startingFrom(Predicate)
	 * @since 1.1
	 */
	default EntrySequence<K, V> startingAfter(Predicate<? super Entry<K, V>> predicate) {
		requireNonNull(predicate, "predicate");

		return () -> new ExclusiveStartingIterator<>(iterator(), predicate);
	}

	/**
	 * Begin this {@code EntrySequence} when the given predicate is satisfied, including the entry that satisfies
	 * the predicate as the first element in the {@code EntrySequence}.
	 *
	 * @see #startingFrom(BiPredicate)
	 * @see #startingFrom(Entry)
	 * @see #startingAfter(Predicate)
	 * @since 1.1
	 */
	default EntrySequence<K, V> startingFrom(Predicate<? super Entry<K, V>> predicate) {
		requireNonNull(predicate, "predicate");

		return () -> new InclusiveStartingIterator<>(iterator(), predicate);
	}

	/**
	 * Begin this {@code EntrySequence} just after the given predicate is satisfied, not including the entry that
	 * satisfies the predicate in the {@code EntrySequence}.
	 *
	 * @see #startingAfter(Predicate)
	 * @see #startingAfter(Entry)
	 * @see #startingFrom(Predicate)
	 * @since 1.1
	 */
	default EntrySequence<K, V> startingAfter(BiPredicate<? super K, ? super V> predicate) {
		requireNonNull(predicate, "predicate");

		return startingAfter(asEntryPredicate(predicate));
	}

	/**
	 * Begin this {@code EntrySequence} when the given predicate is satisfied, including the entry that satisfies
	 * the predicate as the first element in the {@code EntrySequence}.
	 *
	 * @see #startingFrom(Predicate)
	 * @see #startingFrom(Entry)
	 * @see #startingAfter(Predicate)
	 * @since 1.1
	 */
	default EntrySequence<K, V> startingFrom(BiPredicate<? super K, ? super V> predicate) {
		requireNonNull(predicate, "predicate");

		return startingFrom(asEntryPredicate(predicate));
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
		requireNonNull(constructor, "constructor");

		List<Entry<K, V>> list = toList();
		return list.toArray(constructor.apply(list.size()));
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
		requireNonNull(constructor, "constructor");

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
		requireNonNull(constructor, "constructor");

		return toCollection(constructor);
	}

	/**
	 * Collect the entries in this {@code EntrySequence} into a {@link SortedSet}.
	 */
	default SortedSet<Entry<K, V>> toSortedSet() {
		return toSet(TreeSet::new);
	}

	/**
	 * Collect the entries in this {@code EntrySequence} into a {@link Map}. If the same key occurs more than once
	 * in the {@code EntrySequence}, the key is remapped in the resulting map to the latter corresponding value.
	 */
	default Map<K, V> toMap() {
		return toMap(HashMap::new);
	}

	/**
	 * Collect the entries in this {@code EntrySequence} into a {@link Map} of the type determined by the given
	 * constructor. If the same key occurs more than once in the {@code EntrySequence}, the key is remapped in the
	 * resulting map to the latter corresponding value.
	 */
	default <M extends Map<K, V>> M toMap(Supplier<? extends M> constructor) {
		requireNonNull(constructor, "constructor");

		M result = constructor.get();
		for (Entry<K, V> t : this)
			result.put(t.getKey(), t.getValue());

		return result;
	}

	/**
	 * Collect the entries in this {@code EntrySequence} into a {@link Map}, using the given {@code merger}
	 * {@link BiFunction} to merge values in the map, according to {@link Map#merge(Object, Object, BiFunction)}.
	 */
	default Map<K, V> toMergedMap(BiFunction<? super V, ? super V, ? extends V> merger) {
		return toMergedMap(HashMap::new, merger);
	}

	/**
	 * Collect the entries in this {@code EntrySequence} into a {@link Map} of the type determined by the given
	 * constructor. The given {@code merger} {@link BiFunction} is used to merge values in the map, according to
	 * {@link Map#merge(Object, Object, BiFunction)}.
	 */
	default <M extends Map<K, V>> M toMergedMap(Supplier<? extends M> constructor,
	                                            BiFunction<? super V, ? super V, ? extends V> merger) {
		requireNonNull(constructor, "constructor");
		requireNonNull(merger, "merger");

		M result = constructor.get();
		for (Entry<K, V> each : this)
			result.merge(each.getKey(), each.getValue(), merger);

		return result;
	}

	/**
	 * Performs a "group by" operation on the entries in this sequence, grouping elements according to their key and
	 * returning the results in a {@link Map}.
	 *
	 * @since 2.3
	 */
	default Map<K, List<V>> toGroupedMap() {
		return toGroupedMap(HashMap::new);
	}

	/**
	 * Performs a "group by" operation on the entries in this sequence, grouping elements according to their key and
	 * returning the results in a {@link Map} whose type is determined by the given {@code constructor}.
	 *
	 * @since 2.3
	 */
	default <M extends Map<K, List<V>>> M toGroupedMap(Supplier<? extends M> constructor) {
		requireNonNull(constructor, "constructor");

		return toGroupedMap(constructor, ArrayList::new);
	}

	/**
	 * Performs a "group by" operation on the entries in this sequence, grouping elements according to their key and
	 * returning the results in a {@link Map} whose type is determined by the given {@code constructor}, using the
	 * given {@code groupConstructor} to create the target {@link Collection} of the grouped values.
	 *
	 * @since 2.3
	 */
	default <M extends Map<K, C>, C extends Collection<V>> M toGroupedMap(
			Supplier<? extends M> mapConstructor, Supplier<C> groupConstructor) {
		requireNonNull(mapConstructor, "mapConstructor");
		requireNonNull(groupConstructor, "groupConstructor");

		return toGroupedMap(mapConstructor, Collectors.toCollection(groupConstructor));
	}

	/**
	 * Performs a "group by" operation on the entries in this sequence, grouping elements according to their key and
	 * returning the results in a {@link Map} whose type is determined by the given {@code constructor}, using the
	 * given group {@link Collector} to collect the grouped values.
	 *
	 * @since 2.3
	 */
	default <M extends Map<K, C>, C, A> M toGroupedMap(
			Supplier<? extends M> mapConstructor, Collector<? super V, A, C> groupCollector) {
		requireNonNull(mapConstructor, "mapConstructor");
		requireNonNull(groupCollector, "groupCollector");

		Supplier<? extends A> groupConstructor = groupCollector.supplier();
		BiConsumer<? super A, ? super V> groupAccumulator = groupCollector.accumulator();

		@SuppressWarnings("unchecked")
		Map<K, A> result = (Map<K, A>) mapConstructor.get();
		for (Entry<K, V> entry : this)
			groupAccumulator.accept(result.computeIfAbsent(entry.getKey(), k -> groupConstructor.get()),
			                        entry.getValue());

		if (!groupCollector.characteristics().contains(Collector.Characteristics.IDENTITY_FINISH)) {
			@SuppressWarnings("unchecked")
			Function<? super A, ? extends A> groupFinisher = (Function<? super A, ? extends A>) groupCollector
					.finisher();
			result.replaceAll((k, v) -> groupFinisher.apply(v));
		}

		@SuppressWarnings("unchecked")
		M castResult = (M) result;

		return castResult;
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
		requireNonNull(constructor, "constructor");

		return collectInto(constructor.get());
	}

	/**
	 * Collect this {@code EntrySequence} into an arbitrary container using the given constructor and adder.
	 */
	default <C> C collect(Supplier<? extends C> constructor, BiConsumer<? super C, ? super Entry<K, V>> adder) {
		requireNonNull(constructor, "constructor");
		requireNonNull(adder, "adder");

		return collectInto(constructor.get(), adder);
	}

	/**
	 * Collect this {@code EntrySequence} into an arbitrary container using the given {@link Collector}.
	 */
	default <S, R> S collect(Collector<Entry<K, V>, R, S> collector) {
		requireNonNull(collector, "collector");

		R intermediary = collect(collector.supplier(), collector.accumulator());
		return collector.finisher().apply(intermediary);
	}

	/**
	 * Collect this {@code EntrySequence} into the given {@link Collection}.
	 */
	default <U extends Collection<Entry<K, V>>> U collectInto(U collection) {
		requireNonNull(collection, "collection");

		return collectInto(collection, Collection::add);
	}

	/**
	 * Collect this {@code EntrySequence} into the given container, using the given adder.
	 */
	default <C> C collectInto(C result, BiConsumer<? super C, ? super Entry<K, V>> adder) {
		requireNonNull(result, "result");
		requireNonNull(adder, "adder");

		for (Entry<K, V> t : this)
			adder.accept(result, t);
		return result;
	}

	/**
	 * @return a {@link List} view of this {@code EntrySequence}, which is updated in real time as the backing store of
	 * the {@code EntrySequence} changes. The list does not implement {@link RandomAccess} and is best accessed in
	 * sequence. The list does not support {@link List#add}, only removal through {@link Iterator#remove}.
	 *
	 * @since 2.2
	 */
	default List<Entry<K, V>> asList() {
		return Iterables.asList(this);
	}

	/**
	 * Join this {@code EntrySequence} into a string separated by the given delimiter.
	 */
	default String join(String delimiter) {
		requireNonNull(delimiter, "delimiter");

		return join("", delimiter, "");
	}

	/**
	 * Join this {@code EntrySequence} into a string separated by the given delimiter, with the given prefix and
	 * suffix.
	 */
	default String join(String prefix, String delimiter, String suffix) {
		requireNonNull(prefix, "prefix");
		requireNonNull(delimiter, "delimiter");
		requireNonNull(suffix, "suffix");

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
		requireNonNull(operator, "operator");

		return Iterators.reduce(iterator(), operator);
	}

	/**
	 * Reduce this {@code EntrySequence} into a single element by iteratively applying the given function to
	 * the current result and each entry in this sequence. The function is passed the key and value of the result,
	 * followed by the keys and values of the current entry, respectively.
	 */
	default Optional<Entry<K, V>> reduce(QuaternaryFunction<K, V, K, V, Entry<K, V>> operator) {
		requireNonNull(operator, "operator");

		return reduce(asEntryBinaryOperator(operator));
	}

	/**
	 * Reduce this {@code EntrySequence} into a single element by iteratively applying the given binary operator to
	 * the current result and each entry in this sequence, starting with the given identity as the initial result.
	 */
	default Entry<K, V> reduce(Entry<K, V> identity, BinaryOperator<Entry<K, V>> operator) {
		requireNonNull(operator, "operator");

		return Iterators.reduce(iterator(), identity, operator);
	}

	/**
	 * Reduce this {@code EntrySequence} into a single element by iteratively applying the given binary operator to
	 * the current result and each entry in this sequence, starting with the given identity as the initial result.
	 * The function is passed the key and value of the result, followed by the keys and values of the current entry,
	 * respectively.
	 */
	default Entry<K, V> reduce(K key, V value, QuaternaryFunction<K, V, K, V, Entry<K, V>> operator) {
		requireNonNull(operator, "operator");

		return reduce(Maps.entry(key, value), asEntryBinaryOperator(operator));
	}

	/**
	 * @return the first entry of this {@code EntrySequence} or an empty {@link Optional} if there are no entries in
	 * the
	 * {@code EntrySequence}.
	 */
	default Optional<Entry<K, V>> first() {
		return at(0);
	}

	/**
	 * @return the last entry of this {@code EntrySequence} or an empty {@link Optional} if there are no entries in the
	 * {@code EntrySequence}.
	 */
	default Optional<Entry<K, V>> last() {
		return Iterators.last(iterator());
	}

	/**
	 * @return the element at the given index, or an empty {@link Optional} if the {@code EntrySequence} is smaller
	 * than
	 * the index.
	 */
	default Optional<Entry<K, V>> at(int index) {
		requireAtLeastZero(index, "index");

		return Iterators.get(iterator(), index);
	}

	/**
	 * @return the first entry of this {@code EntrySequence} that matches the given predicate, or an empty {@link
	 * Optional} if there are no matching entries in the {@code EntrySequence}.
	 *
	 * @since 1.3
	 */
	default Optional<Entry<K, V>> first(Predicate<? super Entry<K, V>> predicate) {
		requireNonNull(predicate, "predicate");

		return at(0, predicate);
	}

	/**
	 * @return the last entry of this {@code EntrySequence} the matches the given predicate, or an empty {@link
	 * Optional} if there are no matching entries in the {@code EntrySequence}.
	 *
	 * @since 1.3
	 */
	default Optional<Entry<K, V>> last(Predicate<? super Entry<K, V>> predicate) {
		requireNonNull(predicate, "predicate");

		return filter(predicate).last();
	}

	/**
	 * @return the entry at the given index out of the entries matching the given predicate, or an empty {@link
	 * Optional} if the {@code EntrySequence} of matching entries is smaller than the index.
	 *
	 * @since 1.3
	 */
	default Optional<Entry<K, V>> at(int index, Predicate<? super Entry<K, V>> predicate) {
		requireAtLeastZero(index, "index");
		requireNonNull(predicate, "predicate");

		return filter(predicate).at(index);
	}

	/**
	 * @return the first entry of this {@code EntrySequence} that matches the given predicate, or an empty {@link
	 * Optional} if there are no matching entries in the {@code EntrySequence}.
	 *
	 * @since 1.3
	 */
	default Optional<Entry<K, V>> first(BiPredicate<? super K, ? super V> predicate) {
		requireNonNull(predicate, "predicate");

		return at(0, predicate);
	}

	/**
	 * @return the last entry of this {@code EntrySequence} the matches the given predicate, or an empty {@link
	 * Optional} if there are no matching entries in the {@code EntrySequence}.
	 *
	 * @since 1.3
	 */
	default Optional<Entry<K, V>> last(BiPredicate<? super K, ? super V> predicate) {
		requireNonNull(predicate, "predicate");

		return filter(predicate).last();
	}

	/**
	 * @return the entry at the given index out of the entries matching the given predicate, or an empty {@link
	 * Optional} if the {@code EntrySequence} of matching entries is smaller than the index.
	 *
	 * @since 1.3
	 */
	default Optional<Entry<K, V>> at(int index, BiPredicate<? super K, ? super V> predicate) {
		requireAtLeastZero(index, "index");
		requireNonNull(predicate, "predicate");

		return filter(predicate).at(index);
	}

	/**
	 * Window the elements of this {@code EntrySequence} into a {@link Sequence} of {@code EntrySequence}s of entries,
	 * each with the size of the given window. The first item in each sequence is the second item in the previous
	 * sequence. The final sequence may be shorter than the window. This method is equivalent to
	 * {@code window(window, 1)}.
	 */
	default Sequence<EntrySequence<K, V>> window(int window) {
		requireAtLeastOne(window, "window");

		return window(window, 1);
	}

	/**
	 * Window the elements of this {@code EntrySequence} into a sequence of {@code EntrySequence}s of elements, each
	 * with the size of the given window, stepping {@code step} elements between each window. If the given step is less
	 * than the window size, the windows will overlap each other. If the step is larger than the window size, elements
	 * will be skipped in between windows.
	 */
	default Sequence<EntrySequence<K, V>> window(int window, int step) {
		requireAtLeastOne(window, "window");
		requireAtLeastOne(step, "step");

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
		requireAtLeastOne(size, "size");

		return window(size, size);
	}

	/**
	 * Batch the elements of this {@code EntrySequence} into a sequence of {@link EntrySequence}s of distinct elements,
	 * where the given predicate determines where to split the lists of partitioned elements. The predicate is given
	 * the current and next item in the iteration, and if it returns true a partition is created between the elements.
	 */
	default Sequence<EntrySequence<K, V>> batch(BiPredicate<? super Entry<K, V>, ? super Entry<K, V>> predicate) {
		requireNonNull(predicate, "predicate");

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
		requireNonNull(predicate, "predicate");

		return batch((e1, e2) -> predicate.test(e1.getKey(), e1.getValue(), e2.getKey(), e2.getValue()));
	}

	/**
	 * Split the elements of this {@code EntrySequence} into a sequence of {@code EntrySequence}s of distinct elements,
	 * around the given element. The elements around which the sequence is split are not included in the result.
	 */
	default Sequence<EntrySequence<K, V>> split(Entry<K, V> element) {
		return () -> new SplittingIterator<Entry<K, V>, EntrySequence<K, V>>(iterator(), element) {
			@Override
			protected EntrySequence<K, V> toSequence(List<Entry<K, V>> list) {
				return EntrySequence.from(list);
			}
		};
	}

	/**
	 * Split the elements of this {@code EntrySequence} into a sequence of {@code EntrySequence}s of distinct elements,
	 * where the given predicate determines which elements to split the partitioned elements around. The elements
	 * matching the predicate are not included in the result.
	 */
	default Sequence<EntrySequence<K, V>> split(Predicate<? super Entry<K, V>> predicate) {
		requireNonNull(predicate, "predicate");

		return () -> new SplittingIterator<Entry<K, V>, EntrySequence<K, V>>(iterator(), predicate) {
			@Override
			protected EntrySequence<K, V> toSequence(List<Entry<K, V>> list) {
				return EntrySequence.from(list);
			}
		};
	}

	/**
	 * Split the elements of this {@code EntrySequence} into a sequence of {@code EntrySequence}s of distinct elements,
	 * where the given predicate determines which elements to split the partitioned elements around. The elements
	 * matching the predicate are not included in the result.
	 */
	default Sequence<EntrySequence<K, V>> split(BiPredicate<? super K, ? super V> predicate) {
		requireNonNull(predicate, "predicate");

		return split(asEntryPredicate(predicate));
	}

	/**
	 * Skip x number of steps in between each invocation of the iterator of this {@code EntrySequence}.
	 */
	default EntrySequence<K, V> step(int step) {
		requireAtLeastOne(step, "step");

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
	 * @return this {@code EntrySequence} sorted according to the natural order of the key and value. Requires that the
	 * key and value in this sequence implements {@link Comparable} or a {@link ClassCastException} will be thrown.
	 *
	 * @throws ClassCastException if the keys and values in this {@code EntrySequence} does not implement {@link
	 *                            Comparable}.
	 */
	default EntrySequence<K, V> sorted() {
		return sorted(Maps.entryComparator());
	}

	/**
	 * @return this {@code EntrySequence} sorted according to the given {@link Comparator}.
	 */
	default EntrySequence<K, V> sorted(Comparator<? super Entry<? extends K, ? extends V>> comparator) {
		requireNonNull(comparator, "comparator");

		return () -> Iterators.unmodifiable(Lists.sort(toList(), comparator));
	}

	/**
	 * @return the minimal element in this {@code EntrySequence} according to their natural order. The entries in the
	 * sequence must all implement {@link Comparable} or a {@link ClassCastException} will be thrown at runtime.
	 *
	 * @since 1.2
	 */
	@SuppressWarnings("unchecked")
	default Optional<Entry<K, V>> min() {
		return min((Comparator) Comparator.naturalOrder());
	}

	/**
	 * @return the maximum element in this {@code EntrySequence} according to their natural order. The entries in the
	 * sequence must all implement {@link Comparable} or a {@link ClassCastException} will be thrown at runtime.
	 *
	 * @since 1.2
	 */
	@SuppressWarnings("unchecked")
	default Optional<Entry<K, V>> max() {
		return max((Comparator) Comparator.naturalOrder());
	}

	/**
	 * @return the minimal element in this {@code EntrySequence} according to the given {@link Comparator}.
	 */
	default Optional<Entry<K, V>> min(Comparator<? super Entry<K, V>> comparator) {
		requireNonNull(comparator, "comparator");

		return reduce(minBy(comparator));
	}

	/**
	 * @return the maximum element in this {@code EntrySequence} according to the given {@link Comparator}.
	 */
	default Optional<Entry<K, V>> max(Comparator<? super Entry<K, V>> comparator) {
		requireNonNull(comparator, "comparator");

		return reduce(maxBy(comparator));
	}

	/**
	 * @return true if all elements in this {@code EntrySequence} satisfy the given predicate, false otherwise.
	 */
	default boolean all(BiPredicate<? super K, ? super V> predicate) {
		requireNonNull(predicate, "predicate");

		return Iterables.all(this, asEntryPredicate(predicate));
	}

	/**
	 * @return true if no elements in this {@code EntrySequence} satisfy the given predicate, false otherwise.
	 */
	default boolean none(BiPredicate<? super K, ? super V> predicate) {
		requireNonNull(predicate, "predicate");

		return Iterables.none(this, asEntryPredicate(predicate));
	}

	/**
	 * @return true if any element in this {@code EntrySequence} satisfies the given predicate, false otherwise.
	 */
	default boolean any(BiPredicate<? super K, ? super V> predicate) {
		requireNonNull(predicate, "predicate");

		return Iterables.any(this, asEntryPredicate(predicate));
	}

	/**
	 * Allow the given {@link BiConsumer} to see the components of each entry in this {@code EntrySequence} as it is
	 * traversed.
	 */
	default EntrySequence<K, V> peek(BiConsumer<? super K, ? super V> action) {
		requireNonNull(action, "action");

		return peek(asEntryConsumer(action));
	}

	/**
	 * Allow the given {@link Consumer} to see each entry in this {@code EntrySequence} as it is traversed.
	 *
	 * @since 1.2.2
	 */
	default EntrySequence<K, V> peek(Consumer<? super Entry<K, V>> action) {
		requireNonNull(action, "action");

		return () -> new PeekingIterator<>(iterator(), action);
	}

	/**
	 * Allow the given {@link ObjObjIntConsumer} to see the components of each entry with their index as this
	 * {@code EntrySequence} is traversed.
	 *
	 * @since 1.2.2
	 */
	default EntrySequence<K, V> peekIndexed(ObjObjIntConsumer<? super K, ? super V> action) {
		requireNonNull(action, "action");

		return peekIndexed((p, x) -> action.accept(p.getKey(), p.getValue(), x));
	}

	/**
	 * Allow the given {@link ObjIntConsumer} to see each entry with its index as this {@code EntrySequence} is
	 * traversed.
	 *
	 * @since 1.2.2
	 */
	default EntrySequence<K, V> peekIndexed(ObjIntConsumer<? super Entry<K, V>> action) {
		requireNonNull(action, "action");

		return () -> new IndexPeekingIterator<>(iterator(), action);
	}

	/**
	 * Append the elements of the given {@link Iterator} to the end of this {@code EntrySequence}.
	 * <p>
	 * The appended elements will only be available on the first traversal of the resulting {@code Sequence}.
	 */
	default EntrySequence<K, V> append(Iterator<Entry<K, V>> iterator) {
		requireNonNull(iterator, "iterator");

		return append(Iterables.once(iterator));
	}

	/**
	 * Append the elements of the given {@link Iterable} to the end of this {@code EntrySequence}.
	 */
	default EntrySequence<K, V> append(Iterable<Entry<K, V>> iterable) {
		requireNonNull(iterable, "iterable");

		return ChainingIterable.concat(this, iterable)::iterator;
	}

	/**
	 * Append the given elements to the end of this {@code EntrySequence}.
	 */
	@SuppressWarnings("unchecked")
	default EntrySequence<K, V> append(Entry<K, V>... entries) {
		requireNonNull(entries, "entries");

		return append(Iterables.of(entries));
	}

	/**
	 * Append the given entry to the end of this {@code EntrySequence}.
	 */
	@SuppressWarnings("unchecked")
	default EntrySequence<K, V> appendEntry(K key, V value) {
		return append(Maps.entry(key, value));
	}

	/**
	 * Append the elements of the given {@link Stream} to the end of this {@code EntrySequence}.
	 * <p>
	 * The appended elements will only be available on the first traversal of the resulting {@code EntrySequence}.
	 */
	default EntrySequence<K, V> append(Stream<Entry<K, V>> stream) {
		requireNonNull(stream, "stream");

		return append(stream.iterator());
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
		requireNonNull(mapper, "mapper");

		return toSequence(asEntryFunction(mapper));
	}

	/**
	 * Convert this {@code EntrySequence} to a {@link Sequence} where each item is generated by the given mapper.
	 */
	default <T> Sequence<T> toSequence(Function<? super Entry<K, V>, ? extends T> mapper) {
		requireNonNull(mapper, "mapper");

		return () -> new MappingIterator<>(iterator(), mapper);
	}

	/**
	 * Convert this {@code EntrySequence} to a {@link BiSequence} of {@link Pair} elements.
	 */
	default BiSequence<K, V> toBiSequence() {
		return BiSequence.from(toSequence(Pair::from));
	}

	/**
	 * Convert this {@code EntrySequence} to a {@link CharSeq} using the given mapper function to map each entry to a
	 * {@code char}.
	 *
	 * @see #toSequence(BiFunction)
	 * @see #toChars(ToCharFunction)
	 * @see #toInts(ToIntBiFunction)
	 * @see #toLongs(ToLongBiFunction)
	 * @see #toDoubles(ToDoubleBiFunction)
	 * @see #map(BiFunction)
	 * @see #flatten(BiFunction)
	 * @since 1.1.1
	 */
	default CharSeq toChars(ToCharBiFunction<? super K, ? super V> mapper) {
		requireNonNull(mapper, "mapper");

		return toChars(e -> mapper.applyAsChar(e.getKey(), e.getValue()));
	}

	/**
	 * Convert this {@code EntrySequence} to an {@link IntSequence} using the given mapper function to map each entry
	 * to an {@code int}.
	 *
	 * @see #toSequence(BiFunction)
	 * @see #toInts(ToIntFunction)
	 * @see #toChars(ToCharBiFunction)
	 * @see #toLongs(ToLongBiFunction)
	 * @see #toDoubles(ToDoubleBiFunction)
	 * @see #map(BiFunction)
	 * @see #flatten(BiFunction)
	 * @since 1.1.1
	 */
	default IntSequence toInts(ToIntBiFunction<? super K, ? super V> mapper) {
		requireNonNull(mapper, "mapper");

		return toInts(e -> mapper.applyAsInt(e.getKey(), e.getValue()));
	}

	/**
	 * Convert this {@code EntrySequence} to a {@link LongSequence} using the given mapper function to map each entry
	 * to a {@code long}.
	 *
	 * @see #toSequence(BiFunction)
	 * @see #toLongs(ToLongFunction)
	 * @see #toChars(ToCharBiFunction)
	 * @see #toInts(ToIntBiFunction)
	 * @see #toDoubles(ToDoubleBiFunction)
	 * @see #map(BiFunction)
	 * @see #flatten(BiFunction)
	 * @since 1.1.1
	 */
	default LongSequence toLongs(ToLongBiFunction<? super K, ? super V> mapper) {
		requireNonNull(mapper, "mapper");

		return toLongs(e -> mapper.applyAsLong(e.getKey(), e.getValue()));
	}

	/**
	 * Convert this {@code EntrySequence} to a {@link DoubleSequence} using the given mapper function to map each entry
	 * to a {@code double}.
	 *
	 * @see #toSequence(BiFunction)
	 * @see #toDoubles(ToDoubleFunction)
	 * @see #toChars(ToCharBiFunction)
	 * @see #toInts(ToIntBiFunction)
	 * @see #toLongs(ToLongBiFunction)
	 * @see #map(BiFunction)
	 * @see #flatten(BiFunction)
	 * @since 1.1.1
	 */
	default DoubleSequence toDoubles(ToDoubleBiFunction<? super K, ? super V> mapper) {
		requireNonNull(mapper, "mapper");

		return toDoubles(e -> mapper.applyAsDouble(e.getKey(), e.getValue()));
	}

	/**
	 * Convert this {@code EntrySequence} to a {@link CharSeq} using the given mapper function to map each entry to a
	 * {@code char}.
	 *
	 * @see #toSequence(Function)
	 * @see #toChars(ToCharBiFunction)
	 * @see #toInts(ToIntFunction)
	 * @see #toLongs(ToLongFunction)
	 * @see #toDoubles(ToDoubleFunction)
	 * @see #map(Function)
	 * @see #flatten(Function)
	 * @since 1.1.1
	 */
	default CharSeq toChars(ToCharFunction<? super Entry<K, V>> mapper) {
		requireNonNull(mapper, "mapper");

		return () -> CharIterator.from(iterator(), mapper);
	}

	/**
	 * Convert this {@code EntrySequence} to an {@link IntSequence} using the given mapper function to map each entry
	 * to an {@code int}.
	 *
	 * @see #toSequence(Function)
	 * @see #toInts(ToIntBiFunction)
	 * @see #toChars(ToCharFunction)
	 * @see #toLongs(ToLongFunction)
	 * @see #toDoubles(ToDoubleFunction)
	 * @see #map(Function)
	 * @see #flatten(Function)
	 * @since 1.1.1
	 */
	default IntSequence toInts(ToIntFunction<? super Entry<K, V>> mapper) {
		requireNonNull(mapper, "mapper");

		return () -> IntIterator.from(iterator(), mapper);
	}

	/**
	 * Convert this {@code EntrySequence} to a {@link LongSequence} using the given mapper function to map each entry
	 * to a {@code long}.
	 *
	 * @see #toSequence(Function)
	 * @see #toLongs(ToLongBiFunction)
	 * @see #toChars(ToCharFunction)
	 * @see #toInts(ToIntFunction)
	 * @see #toDoubles(ToDoubleFunction)
	 * @see #map(Function)
	 * @see #flatten(Function)
	 * @since 1.1.1
	 */
	default LongSequence toLongs(ToLongFunction<? super Entry<K, V>> mapper) {
		requireNonNull(mapper, "mapper");

		return () -> LongIterator.from(iterator(), mapper);
	}

	/**
	 * Convert this {@code EntrySequence} to a {@link DoubleSequence} using the given mapper function to map each entry
	 * to a {@code double}.
	 *
	 * @see #toSequence(Function)
	 * @see #toDoubles(ToDoubleBiFunction)
	 * @see #toChars(ToCharFunction)
	 * @see #toInts(ToIntFunction)
	 * @see #toLongs(ToLongFunction)
	 * @see #map(Function)
	 * @see #flatten(Function)
	 * @since 1.1.1
	 */
	default DoubleSequence toDoubles(ToDoubleFunction<? super Entry<K, V>> mapper) {
		requireNonNull(mapper, "mapper");

		return () -> DoubleIterator.from(iterator(), mapper);
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
	default EntrySequence<K, V> repeat(int times) {
		requireAtLeastZero(times, "times");

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
		return () -> Iterators.unmodifiable(Lists.shuffle(toList()));
	}

	/**
	 * @return an {@code EntrySequence} which iterates over this {@code EntrySequence} in random order as determined by
	 * the given random generator.
	 */
	default EntrySequence<K, V> shuffle(Random random) {
		requireNonNull(random, "random");

		return () -> Iterators.unmodifiable(Lists.shuffle(toList(), random));
	}

	/**
	 * @return an {@code EntrySequence} which iterates over this {@code EntrySequence} in random order as determined by
	 * the given random generator. A new instance of {@link Random} is created by the given supplier at the start of
	 * each iteration.
	 *
	 * @since 1.2
	 */
	default EntrySequence<K, V> shuffle(Supplier<? extends Random> randomSupplier) {
		requireNonNull(randomSupplier, "randomSupplier");

		return () -> {
			Random random = requireNonNull(randomSupplier.get(), "randomSupplier.get()");
			return Iterators.unmodifiable(Lists.shuffle(toList(), random));
		};
	}

	/**
	 * @return true if this {@code EntrySequence} contains the given entry, false otherwise.
	 *
	 * @since 1.2
	 */
	default boolean contains(Entry<K, V> entry) {
		return Iterables.contains(this, entry);
	}

	/**
	 * @return true if this {@code EntrySequence} contains the given entry, false otherwise.
	 *
	 * @since 1.2
	 */
	default boolean contains(K key, V value) {
		for (Entry<K, V> each : this)
			if (Objects.equals(key, each.getKey()) && Objects.equals(value, each.getValue()))
				return true;

		return false;
	}

	/**
	 * Perform the given action for each element in this {@code EntrySequence}.
	 *
	 * @since 1.2
	 */
	default void forEach(BiConsumer<? super K, ? super V> action) {
		requireNonNull(action, "action");

		forEach(asEntryConsumer(action));
	}
}
