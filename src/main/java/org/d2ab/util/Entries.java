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

package org.d2ab.util;

import org.d2ab.function.Functions;
import org.d2ab.function.QuaternaryFunction;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.*;

import static java.util.Comparator.*;

/**
 * Utility methods related to {@link Entry}.
 */
public class Entries {
	@SuppressWarnings("unchecked")
	private static final Comparator NULLS_FIRST = nullsFirst((Comparator) naturalOrder());

	private static final Function<Entry, Object> GET_KEY = (Function<Entry, Object>) Entry::getKey;
	private static final Function<Entry, Object> GET_VALUE = (Function<Entry, Object>) Entry::getValue;

	@SuppressWarnings("unchecked")
	private static final Comparator<Entry> COMPARATOR = comparing(GET_KEY, NULLS_FIRST).thenComparing(GET_VALUE,
	                                                                                                  NULLS_FIRST);

	private Entries() {
	}

	/**
	 * Creates a new {@link Entry} with the given key and value. Calling {@link Entry#setValue(Object)} on the
	 * entry will result in an {@link UnsupportedOperationException} being thrown.
	 */
	public static <K, V> Entry<K, V> of(@Nullable K key, @Nullable V value) {
		return new EntryImpl<>(key, value);
	}

	public static <K, V> Map<K, V> put(Map<K, V> result, Entry<K, V> each) {
		result.put(each.getKey(), each.getValue());
		return result;
	}

	public static <K, V> UnaryOperator<Entry<K, V>> asUnaryOperator(BiFunction<? super K, ? super V, ? extends
			Entry<K, V>> op) {
		return entry -> op.apply(entry.getKey(), entry.getValue());
	}

	public static <K, V, KK, VV> UnaryOperator<Entry<KK, VV>> asUnaryOperator(
			BiFunction<? super K, ? super V, ? extends Entry<KK, VV>> f,
			BiFunction<? super KK, ? super VV, ? extends Entry<K, V>> g) {
		return Functions.toUnaryOperator(asFunction(f), asFunction(g));
	}

	public static <K, V> BinaryOperator<Entry<K, V>> asBinaryOperator(QuaternaryFunction<? super K, ? super V, ? super
			K, ? super V, ? extends Entry<K, V>> f) {
		return (e1, e2) -> f.apply(e1.getKey(), e1.getValue(), e2.getKey(), e2.getValue());
	}

	public static <K, V, R> Function<Entry<K, V>, R> asFunction(BiFunction<? super K, ? super V, ? extends R> mapper) {
		return entry -> mapper.apply(entry.getKey(), entry.getValue());
	}

	public static <K, V, KK, VV> Function<Entry<K, V>, Entry<KK, VV>> asFunction(
			Function<? super K, ? extends KK> keyMapper, Function<? super V, ? extends VV> valueMapper) {
		return entry -> of(keyMapper.apply(entry.getKey()), valueMapper.apply(entry.getValue()));
	}

	public static <K, V> Predicate<Entry<K, V>> asPredicate(BiPredicate<? super K, ? super V> predicate) {
		return entry -> predicate.test(entry.getKey(), entry.getValue());
	}

	public static <K, V> Consumer<Entry<K, V>> asConsumer(BiConsumer<? super K, ? super V> action) {
		return entry -> action.accept(entry.getKey(), entry.getValue());
	}

	private static class EntryImpl<K, V> implements Entry<K, V>, Comparable<Entry<K, V>> {
		@Nullable
		private final K key;
		@Nullable
		private final V value;

		private EntryImpl(@Nullable K key, @Nullable V value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public int hashCode() {
			int result = (key != null) ? key.hashCode() : 0;
			result = (31 * result) + ((value != null) ? value.hashCode() : 0);
			return result;
		}

		@Override
		public boolean equals(Object o) {
			if (o == this)
				return true;
			if (!(o instanceof Entry))
				return false;

			Entry that = (Entry) o;
			return Objects.equals(key, that.getKey()) && Objects.equals(value, that.getValue());
		}

		@Override
		public String toString() {
			return "<" + key + ", " + value + '>';
		}

		@SuppressWarnings("NullableProblems")
		@Override
		public K getKey() {
			return key;
		}

		@SuppressWarnings("NullableProblems")
		@Override
		public V getValue() {
			return value;
		}

		@Override
		public V setValue(V value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int compareTo(Entry<K, V> that) {
			return COMPARATOR.compare(this, that);
		}
	}
}
