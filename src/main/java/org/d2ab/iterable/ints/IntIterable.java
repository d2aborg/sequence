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

package org.d2ab.iterable.ints;

import org.d2ab.iterator.ints.ArrayIntIterator;
import org.d2ab.iterator.ints.IntIterator;
import org.d2ab.util.Pair;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@FunctionalInterface
public interface IntIterable extends Iterable<Integer> {
	@Override
	IntIterator iterator();

	/**
	 * Performs the given action for each {@code int} in this iterable.
	 */
	@Override
	default void forEach(Consumer<? super Integer> consumer) {
		forEachInt((consumer instanceof IntConsumer) ? (IntConsumer) consumer : consumer::accept);
	}

	/**
	 * Performs the given action for each {@code int} in this iterable.
	 */
	default void forEachInt(IntConsumer consumer) {
		IntIterator iterator = iterator();
		while (iterator.hasNext())
			consumer.accept(iterator.nextInt());
	}

	/**
	 * Converts a container of some kind into a possibly once-only {@link IntIterable}.
	 *
	 * @param container the non-null container to turn into an {@link IntIterable}, can be one of {@link Iterable},
	 *                  {@link IntIterable}, {@link Iterator}, {@link IntIterator}, {@link Stream} of {@link
	 *                  Integer}s, {@link IntStream}, array of {@link Integer}s, or int array.
	 *
	 * @return the container as a {@link IntIterable}.
	 *
	 * @throws ClassCastException if the container is not one of the permitted classes.
	 */
	@SuppressWarnings("unchecked")
	static IntIterable from(Object container) {
		if (container instanceof IntIterable)
			return (IntIterable) container;
		else if (container instanceof Iterable)
			return from((Iterable<Integer>) container);
		else if (container instanceof IntIterator)
			return from((IntIterator) container);
		else if (container instanceof Stream)
			return from((Iterator<Integer>) container);
		else if (container instanceof IntStream)
			return from((IntStream) container);
		else if (container instanceof int[])
			return of((int[]) container);
		else if (container instanceof Integer[])
			return from((Integer[]) container);
		else if (container instanceof Pair)
			return from(((Pair<Integer, Integer>) container)::iterator);
		else
			throw new ClassCastException("Required an Iterable, IntIterable, Iterator, IntIterator, array of " +
			                             "Integer, int array, Stream of Integer, or IntStream but got: " +
			                             container.getClass());
	}

	static IntIterable from(Iterable<Integer> iterable) {
		return () -> IntIterator.from(iterable);
	}

	static IntIterable from(IntIterator iterator) {
		return iterator.asIterable();
	}

	static IntIterable from(Iterator<Integer> iterator) {
		return () -> IntIterator.from(iterator);
	}

	static IntIterable from(IntStream intStream) {
		return from(IntIterator.from(intStream.iterator()));
	}

	static IntIterable of(int... integers) {
		return () -> new ArrayIntIterator(integers);
	}

	static IntIterable from(Integer... integers) {
		return from(Arrays.asList(integers));
	}

	static IntIterable from(Stream<Integer> stream) {
		return from(stream.iterator());
	}
}
