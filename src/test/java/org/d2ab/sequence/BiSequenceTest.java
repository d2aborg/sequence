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

import org.d2ab.collection.Iterables;
import org.d2ab.collection.Lists;
import org.d2ab.collection.Maps;
import org.d2ab.function.QuaternaryFunction;
import org.d2ab.iterator.Iterators;
import org.d2ab.test.SequentialCollector;
import org.d2ab.util.Pair;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static java.util.Comparator.reverseOrder;
import static org.d2ab.test.IsCharIterableContainingInOrder.containsChars;
import static org.d2ab.test.IsDoubleIterableContainingInOrder.containsDoubles;
import static org.d2ab.test.IsIntIterableContainingInOrder.containsInts;
import static org.d2ab.test.IsIterableBeginningWith.beginsWith;
import static org.d2ab.test.IsLongIterableContainingInOrder.containsLongs;
import static org.d2ab.test.Tests.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@SuppressWarnings("unchecked")
public class BiSequenceTest {
	private final BiSequence<String, Integer> empty = BiSequence.empty();

	private final BiSequence<String, Integer> _1 = BiSequence.from(Lists.create(Pair.of("1", 1)));
	private final BiSequence<String, Integer> _12 = BiSequence.from(Lists.create(Pair.of("1", 1), Pair.of("2", 2)));
	private final BiSequence<String, Integer> _123 = BiSequence.from(
			Lists.create(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3)));
	private final BiSequence<String, Integer> _1234 = BiSequence.from(
			Lists.create(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4)));
	private final BiSequence<String, Integer> _12345 = BiSequence.from(Lists.create(
			Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4), Pair.of("5", 5)));
	private final BiSequence<String, Integer> _123456789 = BiSequence.from(
			Lists.create(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4), Pair.of("5", 5),
			             Pair.of("6", 6), Pair.of("7", 7), Pair.of("8", 8), Pair.of("9", 9)));

	private final BiSequence<String, Integer> random1 = BiSequence.from(Lists.create(Pair.of("17", 17)));
	private final BiSequence<String, Integer> random2 = BiSequence.from(
			Lists.create(Pair.of("17", 17), Pair.of("32", 32)));
	private final BiSequence<String, Integer> random3 = BiSequence.from(
			Lists.create(Pair.of("4", 4), Pair.of("2", 2), Pair.of("3", 3)));
	private final BiSequence<String, Integer> random9 = BiSequence.from(
			Lists.create(Pair.of("67", 67), Pair.of("5", 5), Pair.of("43", 43), Pair.of("3", 3), Pair.of("5", 5),
			             Pair.of("7", 7), Pair.of("24", 24), Pair.of("5", 5), Pair.of("67", 67)));

	private final Pair<String, Integer>[] pairs123 = new Pair[]{Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3)};
	private final Pair<String, Integer>[] pairs12345 =
			new Pair[]{Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4), Pair.of("5", 5)};
	private final Pair<String, Integer>[] pairs456 = new Pair[]{Pair.of("4", 4), Pair.of("5", 5), Pair.of("6", 6)};
	private final Pair<String, Integer>[] pairs789 = new Pair[]{Pair.of("7", 7), Pair.of("8", 8), Pair.of("9", 9)};

	@Test
	public void empty() {
		twice(() -> assertThat(empty, is(emptyIterable())));
	}

	@Test
	public void ofNone() {
		BiSequence<String, Integer> sequence = BiSequence.of();

		twice(() -> assertThat(sequence, is(emptyIterable())));
	}

	@Test
	public void ofWithNulls() {
		BiSequence<String, Integer> sequence =
				BiSequence.of(Pair.of("1", 1), Pair.of(null, 2), Pair.of("3", 3), Pair.of("4", null),
				              Pair.of(null, null));

		twice(() -> assertThat(sequence,
		                       contains(Pair.of("1", 1), Pair.of(null, 2), Pair.of("3", 3), Pair.of("4", null),
		                                Pair.of(null, null))));
	}

	@Test
	public void ofOne() {
		twice(() -> assertThat(_1, contains(Pair.of("1", 1))));
	}

	@Test
	public void ofMany() {
		twice(() -> assertThat(_123, contains(pairs123)));
	}

	@Test
	public void ofPair() {
		assertThat(BiSequence.ofPair("1", 1), contains(Pair.of("1", 1)));
	}

	@Test
	public void ofPairs() {
		assertThat(BiSequence.ofPairs("1", 1, "2", 2, "3", 3), contains(pairs123));

		expecting(IllegalArgumentException.class, () -> BiSequence.ofPairs("1"));
	}

	@Test
	public void fromBiSequence() {
		BiSequence<String, Integer> sequence = BiSequence.from(_12345);

		twice(() -> assertThat(sequence, contains(pairs12345)));
	}

	@Test
	public void fromIterable() {
		BiSequence<String, Integer> sequence = BiSequence.from(Iterables.of(pairs12345));

		twice(() -> assertThat(sequence, contains(pairs12345)));
	}

	@Test
	public void fromMap() {
		BiSequence<String, Integer> sequence = BiSequence.from(Maps.builder("1", 1)
		                                                           .put("2", 2)
		                                                           .put("3", 3)
		                                                           .build());

		twice(() -> assertThat(sequence, contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3))));
	}

	@Test
	public void concatIterables() {
		Iterable<Pair<String, Integer>> first = Iterables.of(pairs123);
		Iterable<Pair<String, Integer>> second = Iterables.of(pairs456);
		Iterable<Pair<String, Integer>> third = Iterables.of(pairs789);

		BiSequence<String, Integer> sequence = BiSequence.concat(first, second, third);

		twice(() -> assertThat(sequence, contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4),
		                                          Pair.of("5", 5), Pair.of("6", 6), Pair.of("7", 7), Pair.of("8", 8),
		                                          Pair.of("9", 9))));
	}

	@Test
	public void concatNoIterables() {
		BiSequence<String, Integer> sequence = BiSequence.concat(new Iterable[0]);

		twice(() -> assertThat(sequence, is(emptyIterable())));
	}

	@Test
	public void fromIterables() {
		Iterable<Pair<String, Integer>> first = Iterables.of(pairs123);
		Iterable<Pair<String, Integer>> second = Iterables.of(pairs456);
		Iterable<Pair<String, Integer>> third = Iterables.of(pairs789);

		BiSequence<String, Integer> sequence = BiSequence.from(first, second, third);

		twice(() -> assertThat(sequence, contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4),
		                                          Pair.of("5", 5), Pair.of("6", 6), Pair.of("7", 7), Pair.of("8", 8),
		                                          Pair.of("9", 9))));
	}

	@Test
	public void fromNoIterables() {
		BiSequence<String, Integer> sequence = BiSequence.from(new Iterable[0]);

		twice(() -> assertThat(sequence, is(emptyIterable())));
	}

	@Test
	public void onceIterator() {
		BiSequence<String, Integer> sequence = BiSequence.once(Iterators.of(pairs12345));

		assertThat(sequence, contains(pairs12345));
		assertThat(sequence, is(emptyIterable()));
	}

	@Test
	public void onceStream() {
		BiSequence<String, Integer> sequence = BiSequence.once(Stream.of(pairs12345));

		assertThat(sequence, contains(pairs12345));
		assertThat(sequence, is(emptyIterable()));
	}

	@Test
	public void cacheIterable() {
		List<Pair<String, Integer>> list = Lists.create(pairs12345);
		BiSequence<String, Integer> cached = BiSequence.cache(list::iterator);
		list.set(0, Pair.of("17", 17));

		twice(() -> assertThat(cached, contains(pairs12345)));

		cached.clear();
		twice(() -> assertThat(cached, is(emptyIterable())));
	}

	@Test
	public void cacheIterator() {
		List<Pair<String, Integer>> list = Lists.create(pairs12345);
		BiSequence<String, Integer> cached = BiSequence.cache(list.iterator());
		list.set(0, Pair.of("17", 17));

		twice(() -> assertThat(cached, contains(pairs12345)));

		cached.clear();
		twice(() -> assertThat(cached, is(emptyIterable())));
	}

	@Test
	public void cacheStream() {
		List<Pair<String, Integer>> list = Lists.create(pairs12345);
		BiSequence<String, Integer> cached = BiSequence.cache(list.stream());
		list.set(0, Pair.of("17", 17));

		twice(() -> assertThat(cached, contains(pairs12345)));

		cached.clear();
		twice(() -> assertThat(cached, is(emptyIterable())));
	}

	@Test
	public void forLoop() {
		twice(() -> {
			for (Pair<String, Integer> ignored : empty)
				fail("Should not get called");
		});

		twice(() -> {
			int expected = 1;
			for (Pair<String, Integer> i : _12345)
				assertThat(i, is(Pair.of(String.valueOf(expected), expected++)));

			assertThat(expected, is(6));
		});
	}

	@Test
	public void forEach() {
		twice(() -> {
			empty.forEach(p -> fail("Should not get called"));

			AtomicInteger value = new AtomicInteger(1);
			_1.forEach(p -> assertThat(p, is(Pair.of(String.valueOf(value.get()), value.getAndIncrement()))));

			value.set(1);
			_12.forEach(p -> assertThat(p, is(Pair.of(String.valueOf(value.get()), value.getAndIncrement()))));

			value.set(1);
			_12345.forEach(p -> assertThat(p, is(Pair.of(String.valueOf(value.get()), value.getAndIncrement()))));
		});
	}

	@Test
	public void forEachBiConsumer() {
		twice(() -> {
			empty.forEach((l, r) -> fail("Should not get called"));

			AtomicInteger value = new AtomicInteger(1);
			_1.forEach((l, r) -> {
				assertThat(l, is(String.valueOf(value.get())));
				assertThat(r, is(value.getAndIncrement()));
			});

			value.set(1);
			_12.forEach((l, r) -> {
				assertThat(l, is(String.valueOf(value.get())));
				assertThat(r, is(value.getAndIncrement()));
			});

			value.set(1);
			_12345.forEach((l, r) -> {
				assertThat(l, is(String.valueOf(value.get())));
				assertThat(r, is(value.getAndIncrement()));
			});
		});
	}

	@Test
	public void iterator() {
		twice(() -> {
			Iterator iterator = _123.iterator();

			assertThat(iterator.hasNext(), is(true));
			assertThat(iterator.next(), is(Pair.of("1", 1)));

			assertThat(iterator.hasNext(), is(true));
			assertThat(iterator.next(), is(Pair.of("2", 2)));

			assertThat(iterator.hasNext(), is(true));
			assertThat(iterator.next(), is(Pair.of("3", 3)));

			assertThat(iterator.hasNext(), is(false));
			expecting(NoSuchElementException.class, iterator::next);
		});
	}

	@Test
	public void skip() {
		BiSequence<String, Integer> threeSkipNone = _123.skip(0);
		twice(() -> assertThat(threeSkipNone, is(sameInstance(_123))));

		BiSequence<String, Integer> threeSkipOne = _123.skip(1);
		twice(() -> assertThat(threeSkipOne, contains(Pair.of("2", 2), Pair.of("3", 3))));

		BiSequence<String, Integer> threeSkipTwo = _123.skip(2);
		twice(() -> assertThat(threeSkipTwo, contains(Pair.of("3", 3))));

		BiSequence<String, Integer> threeSkipThree = _123.skip(3);
		twice(() -> assertThat(threeSkipThree, is(emptyIterable())));

		BiSequence<String, Integer> threeSkipFour = _123.skip(4);
		twice(() -> assertThat(threeSkipFour, is(emptyIterable())));

		expecting(NoSuchElementException.class, () -> threeSkipThree.iterator().next());
		expecting(NoSuchElementException.class, () -> threeSkipFour.iterator().next());

		assertThat(removeFirst(threeSkipOne), is(Pair.of("2", 2)));
		twice(() -> assertThat(threeSkipOne, contains(Pair.of("3", 3))));
		twice(() -> assertThat(_123, contains(Pair.of("1", 1), Pair.of("3", 3))));
	}

	@Test
	public void skipTail() {
		BiSequence<String, Integer> threeSkipTailNone = _123.skipTail(0);
		twice(() -> assertThat(threeSkipTailNone, is(sameInstance(_123))));

		BiSequence<String, Integer> threeSkipTailOne = _123.skipTail(1);
		twice(() -> assertThat(threeSkipTailOne, contains(Pair.of("1", 1), Pair.of("2", 2))));

		BiSequence<String, Integer> threeSkipTailTwo = _123.skipTail(2);
		twice(() -> assertThat(threeSkipTailTwo, contains(Pair.of("1", 1))));

		BiSequence<String, Integer> threeSkipTailThree = _123.skipTail(3);
		twice(() -> assertThat(threeSkipTailThree, is(emptyIterable())));

		BiSequence<String, Integer> threeSkipTailFour = _123.skipTail(4);
		twice(() -> assertThat(threeSkipTailFour, is(emptyIterable())));

		expecting(NoSuchElementException.class, () -> threeSkipTailThree.iterator().next());
		expecting(NoSuchElementException.class, () -> threeSkipTailFour.iterator().next());

		expecting(UnsupportedOperationException.class, () -> removeFirst(threeSkipTailOne));
		twice(() -> assertThat(threeSkipTailOne, contains(Pair.of("1", 1), Pair.of("2", 2))));
		twice(() -> assertThat(_123, contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3))));

		BiSequence<String, Integer> nineSkipTailNone = _123456789.skipTail(0);
		twice(() -> assertThat(nineSkipTailNone, is(sameInstance(_123456789))));

		BiSequence<String, Integer> nineSkipTailOne = _123456789.skipTail(1);
		twice(() -> assertThat(nineSkipTailOne,
		                       contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4),
		                                Pair.of("5", 5), Pair.of("6", 6), Pair.of("7", 7), Pair.of("8", 8))));

		BiSequence<String, Integer> nineSkipTailTwo = _123456789.skipTail(2);
		twice(() -> assertThat(nineSkipTailTwo,
		                       contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4),
		                                Pair.of("5", 5), Pair.of("6", 6), Pair.of("7", 7))));

		BiSequence<String, Integer> nineSkipTailThree = _123456789.skipTail(3);
		twice(() -> assertThat(nineSkipTailThree,
		                       contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4),
		                                Pair.of("5", 5), Pair.of("6", 6))));

		BiSequence<String, Integer> nineSkipTailFour = _123456789.skipTail(4);
		twice(() -> assertThat(nineSkipTailFour,
		                       contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4),
		                                Pair.of("5", 5))));
	}

	@Test
	public void limit() {
		BiSequence<String, Integer> threeLimitedToNone = _123.limit(0);
		twice(() -> assertThat(threeLimitedToNone, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> threeLimitedToNone.iterator().next());

		BiSequence<String, Integer> threeLimitedToOne = _123.limit(1);
		twice(() -> assertThat(threeLimitedToOne, contains(Pair.of("1", 1))));
		Iterator<Pair<String, Integer>> iterator = threeLimitedToOne.iterator();
		iterator.next();
		expecting(NoSuchElementException.class, iterator::next);

		BiSequence<String, Integer> threeLimitedToTwo = _123.limit(2);
		twice(() -> assertThat(threeLimitedToTwo, contains(Pair.of("1", 1), Pair.of("2", 2))));

		BiSequence<String, Integer> threeLimitedToThree = _123.limit(3);
		twice(() -> assertThat(threeLimitedToThree, contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3))));

		BiSequence<String, Integer> threeLimitedToFour = _123.limit(4);
		twice(() -> assertThat(threeLimitedToFour, contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3))));

		assertThat(removeFirst(threeLimitedToFour), is(Pair.of("1", 1)));
		twice(() -> assertThat(threeLimitedToFour, contains(Pair.of("2", 2), Pair.of("3", 3))));
		twice(() -> assertThat(_123, contains(Pair.of("2", 2), Pair.of("3", 3))));
	}

	@Test
	public void limitTail() {
		BiSequence<String, Integer> threeLimitTailToNone = _123.limitTail(0);
		twice(() -> assertThat(threeLimitTailToNone, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> threeLimitTailToNone.iterator().next());

		BiSequence<String, Integer> threeLimitTailToOne = _123.limitTail(1);
		twice(() -> assertThat(threeLimitTailToOne, contains(Pair.of("3", 3))));
		Iterator<Pair<String, Integer>> iterator = threeLimitTailToOne.iterator();
		iterator.next();
		expecting(NoSuchElementException.class, iterator::next);

		BiSequence<String, Integer> threeLimitTailToTwo = _123.limitTail(2);
		twice(() -> assertThat(threeLimitTailToTwo, contains(Pair.of("2", 2), Pair.of("3", 3))));

		BiSequence<String, Integer> threeLimitTailToThree = _123.limitTail(3);
		twice(() -> assertThat(threeLimitTailToThree,
		                       contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3))));

		BiSequence<String, Integer> threeLimitTailToFour = _123.limitTail(4);
		twice(() -> assertThat(threeLimitTailToFour,
		                       contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(threeLimitTailToFour));
		twice(() -> assertThat(threeLimitTailToFour, contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3))));
		twice(() -> assertThat(_123, contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3))));

		BiSequence<String, Integer> nineLimitTailToNone = _123456789.limitTail(0);
		twice(() -> assertThat(nineLimitTailToNone, is(emptyIterable())));

		BiSequence<String, Integer> nineLimitTailToOne = _123456789.limitTail(1);
		twice(() -> assertThat(nineLimitTailToOne, contains(Pair.of("9", 9))));

		BiSequence<String, Integer> nineLimitTailToTwo = _123456789.limitTail(2);
		twice(() -> assertThat(nineLimitTailToTwo, contains(Pair.of("8", 8), Pair.of("9", 9))));

		BiSequence<String, Integer> nineLimitTailToThree = _123456789.limitTail(3);
		twice(() -> assertThat(nineLimitTailToThree,
		                       contains(Pair.of("7", 7), Pair.of("8", 8), Pair.of("9", 9))));

		BiSequence<String, Integer> nineLimitTailToFour = _123456789.limitTail(4);
		twice(() -> assertThat(nineLimitTailToFour,
		                       contains(Pair.of("6", 6), Pair.of("7", 7), Pair.of("8", 8), Pair.of("9", 9))));
	}

	@Test
	public void appendEmpty() {
		BiSequence<String, Integer> appendedEmpty = empty.append(Iterables.empty());
		twice(() -> assertThat(appendedEmpty, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> appendedEmpty.iterator().next());
	}

	@Test
	public void append() {
		BiSequence<String, Integer> appended = _123.append(BiSequence.of(pairs456)).append(BiSequence.of
				(pairs789));

		twice(() -> assertThat(appended, contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4),
		                                          Pair.of("5", 5), Pair.of("6", 6), Pair.of("7", 7), Pair.of("8", 8),
		                                          Pair.of("9", 9))));
	}

	@Test
	public void appendPair() {
		BiSequence<String, Integer> appended = _123.appendPair("4", 4);
		twice(() -> assertThat(appended, contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3),
		                                          Pair.of("4", 4))));
	}

	@Test
	public void appendEmptyIterator() {
		BiSequence<String, Integer> appendedEmpty = empty.append(Iterators.empty());
		twice(() -> assertThat(appendedEmpty, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> appendedEmpty.iterator().next());
	}

	@Test
	public void appendIterator() {
		BiSequence<String, Integer> appended = _123.append(
				Iterators.of(Pair.of("4", 4), Pair.of("5", 5), Pair.of("6", 6)))
		                                           .append(Iterators.of(Pair.of("7", 7), Pair.of("8", 8)));

		assertThat(appended,
		           contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4), Pair.of("5", 5),
		                    Pair.of("6", 6), Pair.of("7", 7), Pair.of("8", 8)));
		assertThat(appended, contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3)));
	}

	@Test
	public void appendEmptyStream() {
		BiSequence<String, Integer> appendedEmpty = empty.append(Stream.of());
		twice(() -> assertThat(appendedEmpty, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> appendedEmpty.iterator().next());
	}

	@Test
	public void appendStream() {
		BiSequence<String, Integer> appended = _123.append(Stream.of(Pair.of("4", 4), Pair.of("5", 5), Pair.of("6",
		                                                                                                       6)))
		                                           .append(Stream.of(Pair.of("7", 7), Pair.of("8", 8)));

		assertThat(appended,
		           contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4), Pair.of("5", 5),
		                    Pair.of("6", 6), Pair.of("7", 7), Pair.of("8", 8)));
		assertThat(appended, contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3)));
	}

	@Test
	public void appendEmptyArray() {
		BiSequence<String, Integer> appendedEmpty = empty.append();
		twice(() -> assertThat(appendedEmpty, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> appendedEmpty.iterator().next());
	}

	@Test
	public void appendArray() {
		BiSequence<String, Integer> appended = _123.append(Pair.of("4", 4), Pair.of("5", 5), Pair.of("6", 6))
		                                           .append(Pair.of("7", 7), Pair.of("8", 8));

		twice(() -> assertThat(appended, contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4),
		                                          Pair.of("5", 5), Pair.of("6", 6), Pair.of("7", 7),
		                                          Pair.of("8", 8))));
	}

	@Test
	public void appendIsLazy() {
		Iterator<Pair<String, Integer>> first = Iterators.of(pairs123);
		Iterator<Pair<String, Integer>> second = Iterators.of(pairs456);

		BiSequence<String, Integer> then = BiSequence.once(first).append(() -> second);

		// check delayed iteration
		assertThat(first.hasNext(), is(true));
		assertThat(second.hasNext(), is(true));

		assertThat(then, contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4), Pair.of("5", 5),
		                          Pair.of("6", 6)));
		assertThat(then, is(emptyIterable())); // iterators exhausted on second run
	}

	@Test
	public void appendIsLazyWhenSkippingHasNext() {
		Iterator<Pair<String, Integer>> first = Iterators.of(Pair.of("1", 1));
		Iterator<Pair<String, Integer>> second = Iterators.of(Pair.of("2", 2));

		BiSequence<String, Integer> sequence = BiSequence.once(first).append(BiSequence.once(second));

		// check delayed iteration
		Iterator<Pair<String, Integer>> iterator = sequence.iterator();
		assertThat(iterator.next(), is(Pair.of("1", 1)));
		assertThat(iterator.next(), is(Pair.of("2", 2)));
		assertThat(iterator.hasNext(), is(false));

		assertThat(sequence, is(emptyIterable())); // second run is empty
	}

	@Test
	public void toSequence() {
		Sequence<Pair<String, Integer>> emptySequence = empty.toSequence();
		twice(() -> assertThat(emptySequence, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptySequence.iterator().next());

		Sequence<Pair<String, Integer>> sequence = _12345.toSequence();
		twice(() -> assertThat(sequence, contains(pairs12345)));

		assertThat(removeFirst(sequence), is(Pair.of("1", 1)));
		twice(() -> assertThat(sequence, contains(Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4),
		                                          Pair.of("5", 5))));
		twice(() -> assertThat(_12345, contains(Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4),
		                                        Pair.of("5", 5))));
	}

	@Test
	public void toSequenceLeftRightMapper() {
		Sequence<String> emptyLeftSequence = empty.toSequence((l, r) -> l);
		twice(() -> assertThat(emptyLeftSequence, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyLeftSequence.iterator().next());

		Sequence<Integer> emptyRightSequence = empty.toSequence((l, r) -> r);
		twice(() -> assertThat(emptyRightSequence, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyRightSequence.iterator().next());

		Sequence<String> leftSequence = _12345.toSequence((l, r) -> l);
		twice(() -> assertThat(leftSequence, contains("1", "2", "3", "4", "5")));

		Sequence<Integer> rightSequence = _12345.toSequence((l, r) -> r);
		twice(() -> assertThat(rightSequence, contains(1, 2, 3, 4, 5)));

		assertThat(removeFirst(leftSequence), is("1"));
		twice(() -> assertThat(leftSequence, contains("2", "3", "4", "5")));
		twice(() -> assertThat(_12345, contains(Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4),
		                                        Pair.of("5", 5))));

		assertThat(removeFirst(rightSequence), is(2));
		twice(() -> assertThat(rightSequence, contains(3, 4, 5)));
		twice(() -> assertThat(_12345, contains(Pair.of("3", 3), Pair.of("4", 4), Pair.of("5", 5))));
	}

	@Test
	public void toSequencePairMapper() {
		Sequence<String> emptyLeftSequence = empty.toSequence(Pair::getLeft);
		twice(() -> assertThat(emptyLeftSequence, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyLeftSequence.iterator().next());

		Sequence<Integer> emptyRightSequence = empty.toSequence(Pair::getRight);
		twice(() -> assertThat(emptyRightSequence, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyRightSequence.iterator().next());

		Sequence<String> leftSequence = _12345.toSequence(Pair::getLeft);
		twice(() -> assertThat(leftSequence, contains("1", "2", "3", "4", "5")));

		Sequence<Integer> rightSequence = _12345.toSequence(Pair::getRight);
		twice(() -> assertThat(rightSequence, contains(1, 2, 3, 4, 5)));

		assertThat(removeFirst(leftSequence), is("1"));
		twice(() -> assertThat(leftSequence, contains("2", "3", "4", "5")));
		twice(() -> assertThat(_12345, contains(Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4),
		                                        Pair.of("5", 5))));

		assertThat(removeFirst(rightSequence), is(2));
		twice(() -> assertThat(rightSequence, contains(3, 4, 5)));
		twice(() -> assertThat(_12345, contains(Pair.of("3", 3), Pair.of("4", 4), Pair.of("5", 5))));
	}

	@Test
	public void toEntrySequence() {
		EntrySequence<String, Integer> emptyEntrySequence = empty.toEntrySequence();
		twice(() -> assertThat(emptyEntrySequence, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyEntrySequence.iterator().next());

		EntrySequence<String, Integer> entrySequence = _12345.toEntrySequence();
		twice(() -> assertThat(entrySequence, contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3),
		                                               Pair.of("4", 4), Pair.of("5", 5))));

		assertThat(removeFirst(entrySequence), is(Pair.of("1", 1)));
		twice(() -> assertThat(entrySequence, contains(Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4),
		                                               Pair.of("5", 5))));
		twice(() -> assertThat(_12345, contains(Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4),
		                                        Pair.of("5", 5))));
	}

	@Test
	public void filter() {
		BiSequence<String, Integer> emptyFiltered = empty.filter((s, i) -> parseInt(s) == i && i % 2 == 0);
		twice(() -> assertThat(emptyFiltered, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyFiltered.iterator().next());

		BiSequence<String, Integer> oneFiltered = _1.filter((s, i) -> parseInt(s) == i && i % 2 == 0);
		twice(() -> assertThat(oneFiltered, is(emptyIterable())));

		BiSequence<String, Integer> twoFiltered = _12.filter((s, i) -> parseInt(s) == i && i % 2 == 0);
		twice(() -> assertThat(twoFiltered, contains(Pair.of("2", 2))));

		assertThat(removeFirst(twoFiltered), is(Pair.of("2", 2)));
		twice(() -> assertThat(twoFiltered, is(emptyIterable())));
		twice(() -> assertThat(_12, contains(Pair.of("1", 1))));

		BiSequence<String, Integer> filtered = _123456789.filter((s, i) -> parseInt(s) == i && i % 2 == 0);
		twice(() -> assertThat(filtered, contains(Pair.of("2", 2), Pair.of("4", 4), Pair.of("6", 6),
		                                          Pair.of("8", 8))));
	}

	@Test
	public void filterIndexed() {
		BiSequence<String, Integer> emptyFiltered = empty.filterIndexed((l, r, x) -> parseInt(l) == r && x > 0);
		twice(() -> assertThat(emptyFiltered, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyFiltered.iterator().next());

		BiSequence<String, Integer> oneFiltered = _1.filterIndexed((l, r, x) -> parseInt(l) == r && x > 0);
		twice(() -> assertThat(oneFiltered, is(emptyIterable())));

		BiSequence<String, Integer> twoFiltered = _12.filterIndexed((l, r, x) -> parseInt(l) == r && x > 0);
		twice(() -> assertThat(twoFiltered, contains(Pair.of("2", 2))));

		assertThat(removeFirst(twoFiltered), is(Pair.of("2", 2)));
		twice(() -> assertThat(twoFiltered, is(emptyIterable())));
		twice(() -> assertThat(_12, contains(Pair.of("1", 1))));

		BiSequence<String, Integer> filtered = _123456789.filterIndexed((l, r, x) -> parseInt(l) == r && x > 3);
		twice(() -> assertThat(filtered, contains(Pair.of("5", 5), Pair.of("6", 6), Pair.of("7", 7), Pair.of("8", 8),
		                                          Pair.of("9", 9))));
	}

	@Test
	public void filterPairIndexed() {
		BiSequence<String, Integer> emptyFiltered = empty.filterIndexed(
				(p, x) -> parseInt(p.getLeft()) == p.getRight() && x > 0);
		twice(() -> assertThat(emptyFiltered, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyFiltered.iterator().next());

		BiSequence<String, Integer> oneFiltered = _1.filterIndexed(
				(p, x) -> parseInt(p.getLeft()) == p.getRight() && x > 0);
		twice(() -> assertThat(oneFiltered, is(emptyIterable())));

		BiSequence<String, Integer> twoFiltered = _12.filterIndexed(
				(p, x) -> parseInt(p.getLeft()) == p.getRight() && x > 0);
		twice(() -> assertThat(twoFiltered, contains(Pair.of("2", 2))));

		assertThat(removeFirst(twoFiltered), is(Pair.of("2", 2)));
		twice(() -> assertThat(twoFiltered, is(emptyIterable())));
		twice(() -> assertThat(_12, contains(Pair.of("1", 1))));

		BiSequence<String, Integer> filtered = _123456789.filterIndexed(
				(p, x) -> parseInt(p.getLeft()) == p.getRight() && x > 3);
		twice(() -> assertThat(filtered, contains(Pair.of("5", 5), Pair.of("6", 6), Pair.of("7", 7), Pair.of("8", 8),
		                                          Pair.of("9", 9))));
	}

	@Test
	public void includingArray() {
		BiSequence<String, Integer> emptyIncluding = empty.including(Pair.of("1", 1), Pair.of("3", 3), Pair.of("5", 5),
		                                                             Pair.of("17", 17));
		twice(() -> assertThat(emptyIncluding, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyIncluding.iterator().next());

		BiSequence<String, Integer> including = _12345.including(Pair.of("1", 1), Pair.of("3", 3), Pair.of("5", 5),
		                                                         Pair.of("17", 17));
		twice(() -> assertThat(including, contains(Pair.of("1", 1), Pair.of("3", 3), Pair.of("5", 5))));

		BiSequence<String, Integer> includingAll = _12345.including(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3),
		                                                            Pair.of("4", 4), Pair.of("5", 5),
		                                                            Pair.of("17", 17));
		twice(() -> assertThat(includingAll, contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3),
		                                              Pair.of("4", 4), Pair.of("5", 5))));

		BiSequence<String, Integer> includingNone = _12345.including();
		twice(() -> assertThat(includingNone, is(emptyIterable())));

		assertThat(removeFirst(including), is(Pair.of("1", 1)));
		twice(() -> assertThat(including, contains(Pair.of("3", 3), Pair.of("5", 5))));
		twice(() -> assertThat(_12345, contains(Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4), Pair.of("5", 5))));
	}

	@Test
	public void includingIterable() {
		BiSequence<String, Integer> emptyIncluding = empty.including(
				Iterables.of(Pair.of("1", 1), Pair.of("3", 3), Pair.of("5", 5), Pair.of("17", 17)));
		twice(() -> assertThat(emptyIncluding, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyIncluding.iterator().next());

		BiSequence<String, Integer> including = _12345.including(
				Iterables.of(Pair.of("1", 1), Pair.of("3", 3), Pair.of("5", 5), Pair.of("17", 17)));
		twice(() -> assertThat(including, contains(Pair.of("1", 1), Pair.of("3", 3), Pair.of("5", 5))));

		BiSequence<String, Integer> includingAll = _12345.including(
				Iterables.of(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4), Pair.of("5", 5),
				             Pair.of("17", 17)));
		twice(() -> assertThat(includingAll, contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3),
		                                              Pair.of("4", 4), Pair.of("5", 5))));

		BiSequence<String, Integer> includingNone = _12345.including(Iterables.of());
		twice(() -> assertThat(includingNone, is(emptyIterable())));

		assertThat(removeFirst(including), is(Pair.of("1", 1)));
		twice(() -> assertThat(including, contains(Pair.of("3", 3), Pair.of("5", 5))));
		twice(() -> assertThat(_12345, contains(Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4), Pair.of("5", 5))));
	}

	@Test
	public void excludingArray() {
		BiSequence<String, Integer> emptyExcluding = empty.excluding(Pair.of("1", 1), Pair.of("3", 3), Pair.of("5", 5),
		                                                             Pair.of("17", 17));
		twice(() -> assertThat(emptyExcluding, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyExcluding.iterator().next());

		BiSequence<String, Integer> excluding = _12345.excluding(Pair.of("1", 1), Pair.of("3", 3), Pair.of("5", 5),
		                                                         Pair.of("17", 17));
		twice(() -> assertThat(excluding, contains(Pair.of("2", 2), Pair.of("4", 4))));

		BiSequence<String, Integer> excludingAll = _12345.excluding(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3),
		                                                            Pair.of("4", 4), Pair.of("5", 5),
		                                                            Pair.of("17", 17));
		twice(() -> assertThat(excludingAll, is(emptyIterable())));

		BiSequence<String, Integer> excludingNone = _12345.excluding();
		twice(() -> assertThat(excludingNone, contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3),
		                                               Pair.of("4", 4), Pair.of("5", 5))));

		assertThat(removeFirst(excluding), is(Pair.of("2", 2)));
		twice(() -> assertThat(excluding, contains(Pair.of("4", 4))));
		twice(() -> assertThat(_12345, contains(Pair.of("1", 1), Pair.of("3", 3), Pair.of("4", 4), Pair.of("5", 5))));
	}

	@Test
	public void excludingIterable() {
		BiSequence<String, Integer> emptyExcluding = empty.excluding(
				Iterables.of(Pair.of("1", 1), Pair.of("3", 3), Pair.of("5", 5), Pair.of("17", 17)));
		twice(() -> assertThat(emptyExcluding, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyExcluding.iterator().next());

		BiSequence<String, Integer> excluding = _12345.excluding(
				Iterables.of(Pair.of("1", 1), Pair.of("3", 3), Pair.of("5", 5), Pair.of("17", 17)));
		twice(() -> assertThat(excluding, contains(Pair.of("2", 2), Pair.of("4", 4))));

		BiSequence<String, Integer> excludingAll = _12345.excluding(
				Iterables.of(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4), Pair.of("5", 5),
				             Pair.of("17", 17)));
		twice(() -> assertThat(excludingAll, is(emptyIterable())));

		BiSequence<String, Integer> excludingNone = _12345.excluding(Iterables.of());
		twice(() -> assertThat(excludingNone, contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3),
		                                               Pair.of("4", 4), Pair.of("5", 5))));

		assertThat(removeFirst(excluding), is(Pair.of("2", 2)));
		twice(() -> assertThat(excluding, contains(Pair.of("4", 4))));
		twice(() -> assertThat(_12345, contains(Pair.of("1", 1), Pair.of("3", 3), Pair.of("4", 4), Pair.of("5", 5))));
	}

	@Test
	public void filterAndMap() {
		BiSequence<Integer, String> evensSwapped = _123456789.filter((s, x) -> x % 2 == 0)
		                                                     .map(Integer::parseInt, Object::toString);

		twice(() -> assertThat(evensSwapped,
		                       contains(Pair.of(2, "2"), Pair.of(4, "4"), Pair.of(6, "6"), Pair.of(8, "8"))));

		assertThat(removeFirst(evensSwapped), is(Pair.of(2, "2")));
		twice(() -> assertThat(evensSwapped, contains(Pair.of(4, "4"), Pair.of(6, "6"), Pair.of(8, "8"))));
	}

	@Test
	public void mapBiFunction() {
		BiSequence<Integer, String> emptyMapped = empty.map((s, i) -> Pair.of(parseInt(s), i.toString()));
		twice(() -> assertThat(emptyMapped, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyMapped.iterator().next());

		BiSequence<Integer, String> mapped = _123.map((s, i) -> Pair.of(parseInt(s), i.toString()));
		twice(() -> assertThat(mapped, contains(Pair.of(1, "1"), Pair.of(2, "2"), Pair.of(3, "3"))));

		assertThat(removeFirst(mapped), is(Pair.of(1, "1")));
		twice(() -> assertThat(mapped, contains(Pair.of(2, "2"), Pair.of(3, "3"))));
	}

	@Test
	public void mapTwoFunctions() {
		BiSequence<Integer, String> emptyMapped = empty.map(Integer::parseInt, Object::toString);
		twice(() -> assertThat(emptyMapped, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyMapped.iterator().next());

		BiSequence<Integer, String> mapped = _123.map(Integer::parseInt, Object::toString);
		twice(() -> assertThat(mapped, contains(Pair.of(1, "1"), Pair.of(2, "2"), Pair.of(3, "3"))));

		assertThat(removeFirst(mapped), is(Pair.of(1, "1")));
		twice(() -> assertThat(mapped, contains(Pair.of(2, "2"), Pair.of(3, "3"))));
	}

	@Test
	public void mapPairFunction() {
		BiSequence<Integer, String> emptyMapped = empty.map(Pair::swap);
		twice(() -> assertThat(emptyMapped, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyMapped.iterator().next());

		BiSequence<Integer, String> mapped = _123.map(Pair::swap);
		twice(() -> assertThat(mapped, contains(Pair.of(1, "1"), Pair.of(2, "2"), Pair.of(3, "3"))));

		assertThat(removeFirst(mapped), is(Pair.of(1, "1")));
		twice(() -> assertThat(mapped, contains(Pair.of(2, "2"), Pair.of(3, "3"))));
	}

	@Test
	public void mapIsLazy() {
		BiSequence<Integer, String> mapped = BiSequence.of(Pair.of("1", 1), null) // null will be hit when mapping
		                                               .map((s, i) -> Pair.of(parseInt(s), i.toString()));

		twice(() -> {
			// NPE here if not lazy
			Iterator<Pair<Integer, String>> iterator = mapped.iterator();

			assertThat(iterator.next(), is(Pair.of(1, "1")));

			try {
				iterator.next();
				fail("Expected NPE");
			} catch (NullPointerException ignored) {
				// expected
			}
		});
	}

	@Test
	public void mapWithIndex() {
		BiSequence<Integer, String> emptyMapped = empty.mapIndexed((p, i) -> {
			throw new IllegalStateException("Should not get called");
		});
		twice(() -> assertThat(emptyMapped, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyMapped.iterator().next());

		AtomicInteger index = new AtomicInteger();
		BiSequence<Integer, String> oneMapped = _1.mapIndexed((p, i) -> {
			assertThat(i, is(index.getAndIncrement()));
			return p.swap();
		});
		twice(() -> {
			index.set(0);
			assertThat(oneMapped, contains(Pair.of(1, "1")));
		});

		index.set(0);
		assertThat(removeFirst(oneMapped), is(Pair.of(1, "1")));
		twice(() -> assertThat(oneMapped, is(emptyIterable())));

		BiSequence<Integer, String> twoMapped = _12.mapIndexed((p, i) -> {
			assertThat(i, is(index.getAndIncrement()));
			return p.swap();
		});
		twice(() -> {
			index.set(0);
			assertThat(twoMapped, contains(Pair.of(1, "1"), Pair.of(2, "2")));
		});

		BiSequence<Integer, String> fiveMapped = _12345.mapIndexed((p, i) -> {
			assertThat(i, is(index.getAndIncrement()));
			return p.swap();
		});
		twice(() -> {
			index.set(0);
			assertThat(fiveMapped,
			           contains(Pair.of(1, "1"), Pair.of(2, "2"), Pair.of(3, "3"), Pair.of(4, "4"), Pair.of(5, "5")));
		});
	}

	@Test
	public void mapBiFunctionWithIndex() {
		BiSequence<Integer, String> emptyMapped = empty.mapIndexed((l, r, i) -> {
			throw new IllegalStateException("Should not get called");
		});
		twice(() -> assertThat(emptyMapped, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyMapped.iterator().next());

		AtomicInteger index = new AtomicInteger();
		BiSequence<Integer, String> oneMapped = _1.mapIndexed((l, r, i) -> {
			assertThat(i, is(index.getAndIncrement()));
			return Pair.of(r, l);
		});
		twiceIndexed(index, 1, () -> assertThat(oneMapped, contains(Pair.of(1, "1"))));

		index.set(0);
		assertThat(removeFirst(oneMapped), is(Pair.of(1, "1")));
		twice(() -> assertThat(oneMapped, is(emptyIterable())));

		index.set(0);
		BiSequence<Integer, String> twoMapped = _12.mapIndexed((l, r, i) -> {
			assertThat(i, is(index.getAndIncrement()));
			return Pair.of(r, l);
		});
		twiceIndexed(index, 2, () -> assertThat(twoMapped, contains(Pair.of(1, "1"), Pair.of(2, "2"))));

		BiSequence<Integer, String> fiveMapped = _12345.mapIndexed((l, r, i) -> {
			assertThat(i, is(index.getAndIncrement()));
			return Pair.of(r, l);
		});
		twiceIndexed(index, 5, () -> assertThat(fiveMapped,
		                                        contains(Pair.of(1, "1"), Pair.of(2, "2"), Pair.of(3, "3"),
		                                                 Pair.of(4, "4"),
		                                                 Pair.of(5, "5"))));
	}

	@Test
	public void mapLeft() {
		BiSequence<String, Integer> emptyMapped = empty.mapLeft(s -> "l" + s);
		twice(() -> assertThat(emptyMapped, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyMapped.iterator().next());

		BiSequence<String, Integer> mapped = _123.mapLeft(s -> "l" + s);
		twice(() -> assertThat(mapped, contains(Pair.of("l1", 1), Pair.of("l2", 2), Pair.of("l3", 3))));

		assertThat(removeFirst(mapped), is(Pair.of("l1", 1)));
		twice(() -> assertThat(mapped, contains(Pair.of("l2", 2), Pair.of("l3", 3))));
	}

	@Test
	public void mapRight() {
		BiSequence<String, Integer> emptyMapped = empty.mapRight(i -> i + 1);
		twice(() -> assertThat(emptyMapped, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyMapped.iterator().next());

		BiSequence<String, Integer> mapped = _123.mapRight(i -> i + 1);
		twice(() -> assertThat(mapped, contains(Pair.of("1", 2), Pair.of("2", 3), Pair.of("3", 4))));

		assertThat(removeFirst(mapped), is(Pair.of("1", 2)));
		twice(() -> assertThat(mapped, contains(Pair.of("2", 3), Pair.of("3", 4))));
	}

	@Test
	public void recurse() {
		BiSequence<String, Integer> sequence =
				BiSequence.recurse("1", 1, (k, v) -> Pair.of(String.valueOf(v + 1), v + 1));
		twice(() -> assertThat(sequence.limit(3), contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3))));
	}

	@Test
	public void recurseTwins() {
		BiSequence<String, Integer> sequence =
				BiSequence.recurse(1, "1", (k, v) -> Pair.of(v, k), (k, v) -> Pair.of(v + 1, String.valueOf(v + 1)));
		twice(() -> assertThat(sequence.limit(3), contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3))));
	}

	@Test
	public void until() {
		BiSequence<String, Integer> emptyUntil = empty.until(Pair.of("4", 4));
		twice(() -> assertThat(emptyUntil, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyUntil.iterator().next());

		BiSequence<String, Integer> sequence = _12345.until(Pair.of("4", 4));
		twice(() -> assertThat(sequence, contains(pairs123)));

		assertThat(removeFirst(sequence), is(Pair.of("1", 1)));
		twice(() -> assertThat(sequence, contains(Pair.of("2", 2), Pair.of("3", 3))));
	}

	@Test
	public void endingAt() {
		BiSequence<String, Integer> emptyEndingAt = empty.endingAt(Pair.of("3", 3));
		twice(() -> assertThat(emptyEndingAt, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyEndingAt.iterator().next());

		BiSequence<String, Integer> sequence = _12345.endingAt(Pair.of("3", 3));
		twice(() -> assertThat(sequence, contains(pairs123)));

		assertThat(removeFirst(sequence), is(Pair.of("1", 1)));
		twice(() -> assertThat(sequence, contains(Pair.of("2", 2), Pair.of("3", 3))));
	}

	@Test
	public void untilPredicate() {
		BiSequence<String, Integer> emptyUntil = empty.until(e -> e.equals(Pair.of("4", 4)));
		twice(() -> assertThat(emptyUntil, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyUntil.iterator().next());

		BiSequence<String, Integer> sequence = _12345.until(e -> e.equals(Pair.of("4", 4)));
		twice(() -> assertThat(sequence, contains(pairs123)));

		assertThat(removeFirst(sequence), is(Pair.of("1", 1)));
		twice(() -> assertThat(sequence, contains(Pair.of("2", 2), Pair.of("3", 3))));
	}

	@Test
	public void endingAtPredicate() {
		BiSequence<String, Integer> emptyEndingAt = empty.endingAt(e -> e.equals(Pair.of("3", 3)));
		twice(() -> assertThat(emptyEndingAt, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyEndingAt.iterator().next());

		BiSequence<String, Integer> sequence = _12345.endingAt(e -> e.equals(Pair.of("3", 3)));
		twice(() -> assertThat(sequence, contains(pairs123)));

		assertThat(removeFirst(sequence), is(Pair.of("1", 1)));
		twice(() -> assertThat(sequence, contains(Pair.of("2", 2), Pair.of("3", 3))));
	}

	@Test
	public void untilBinary() {
		BiSequence<String, Integer> emptyUntil = empty.until("4", 4);
		twice(() -> assertThat(emptyUntil, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyUntil.iterator().next());

		BiSequence<String, Integer> sequence = _12345.until("4", 4);
		twice(() -> assertThat(sequence, contains(pairs123)));

		assertThat(removeFirst(sequence), is(Pair.of("1", 1)));
		twice(() -> assertThat(sequence, contains(Pair.of("2", 2), Pair.of("3", 3))));
	}

	@Test
	public void endingAtBinary() {
		BiSequence<String, Integer> emptyEndingAt = empty.endingAt("3", 3);
		twice(() -> assertThat(emptyEndingAt, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyEndingAt.iterator().next());

		BiSequence<String, Integer> sequence = _12345.endingAt("3", 3);
		twice(() -> assertThat(sequence, contains(pairs123)));

		assertThat(removeFirst(sequence), is(Pair.of("1", 1)));
		twice(() -> assertThat(sequence, contains(Pair.of("2", 2), Pair.of("3", 3))));
	}

	@Test
	public void untilBinaryPredicate() {
		BiSequence<String, Integer> emptyUntil = empty.until((l, r) -> l.equals("4") && r == 4);
		twice(() -> assertThat(emptyUntil, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyUntil.iterator().next());

		BiSequence<String, Integer> sequence = _12345.until((l, r) -> l.equals("4") && r == 4);
		twice(() -> assertThat(sequence, contains(pairs123)));

		assertThat(removeFirst(sequence), is(Pair.of("1", 1)));
		twice(() -> assertThat(sequence, contains(Pair.of("2", 2), Pair.of("3", 3))));
	}

	@Test
	public void endingAtBinaryPredicate() {
		BiSequence<String, Integer> emptyEndingAt = empty.endingAt((l, r) -> l.equals("3") && r == 3);
		twice(() -> assertThat(emptyEndingAt, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyEndingAt.iterator().next());

		BiSequence<String, Integer> sequence = _12345.endingAt((l, r) -> l.equals("3") && r == 3);
		twice(() -> assertThat(sequence, contains(pairs123)));

		assertThat(removeFirst(sequence), is(Pair.of("1", 1)));
		twice(() -> assertThat(sequence, contains(Pair.of("2", 2), Pair.of("3", 3))));
	}

	@Test
	public void startingAfter() {
		BiSequence<String, Integer> startingEmpty = empty.startingAfter(Pair.of("5", 5));
		twice(() -> assertThat(startingEmpty, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> startingEmpty.iterator().next());

		BiSequence<String, Integer> sequence = _123456789.startingAfter(Pair.of("5", 5));
		twice(() -> assertThat(sequence, contains(Pair.of("6", 6), Pair.of("7", 7), Pair.of("8", 8),
		                                          Pair.of("9", 9))));

		assertThat(removeFirst(sequence), is(Pair.of("6", 6)));
		twice(() -> assertThat(sequence, contains(Pair.of("7", 7), Pair.of("8", 8), Pair.of("9", 9))));

		BiSequence<String, Integer> noStart = _12345.startingAfter(Pair.of("10", 10));
		twice(() -> assertThat(noStart, is(emptyIterable())));
	}

	@Test
	public void startingAfterPredicate() {
		BiSequence<String, Integer> startingEmpty = empty.startingAfter(p -> p.getRight() == 5);
		twice(() -> assertThat(startingEmpty, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> startingEmpty.iterator().next());

		BiSequence<String, Integer> sequence = _123456789.startingAfter(p -> p.getRight() == 5);
		twice(() -> assertThat(sequence, contains(Pair.of("6", 6), Pair.of("7", 7), Pair.of("8", 8),
		                                          Pair.of("9", 9))));

		assertThat(removeFirst(sequence), is(Pair.of("6", 6)));
		twice(() -> assertThat(sequence, contains(Pair.of("7", 7), Pair.of("8", 8), Pair.of("9", 9))));

		BiSequence<String, Integer> noStart = _12345.startingAfter(p -> p.getRight() == 10);
		twice(() -> assertThat(noStart, is(emptyIterable())));
	}

	@Test
	public void startingAfterBiPredicate() {
		BiSequence<String, Integer> startingEmpty = empty.startingAfter((l, r) -> r == 5);
		twice(() -> assertThat(startingEmpty, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> startingEmpty.iterator().next());

		BiSequence<String, Integer> sequence = _123456789.startingAfter((l, r) -> r == 5);
		twice(() -> assertThat(sequence, contains(Pair.of("6", 6), Pair.of("7", 7), Pair.of("8", 8),
		                                          Pair.of("9", 9))));

		assertThat(removeFirst(sequence), is(Pair.of("6", 6)));
		twice(() -> assertThat(sequence, contains(Pair.of("7", 7), Pair.of("8", 8), Pair.of("9", 9))));

		BiSequence<String, Integer> noStart = _12345.startingAfter((l, r) -> r == 10);
		twice(() -> assertThat(noStart, is(emptyIterable())));
	}

	@Test
	public void startingFrom() {
		BiSequence<String, Integer> startingEmpty = empty.startingFrom(Pair.of("5", 5));
		twice(() -> assertThat(startingEmpty, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> startingEmpty.iterator().next());

		BiSequence<String, Integer> sequence = _123456789.startingFrom(Pair.of("5", 5));
		twice(() -> assertThat(sequence, contains(Pair.of("5", 5), Pair.of("6", 6), Pair.of("7", 7), Pair.of("8", 8),
		                                          Pair.of("9", 9))));

		assertThat(removeFirst(sequence), is(Pair.of("5", 5)));
		twice(() -> assertThat(sequence, is(emptyIterable())));

		BiSequence<String, Integer> noStart = _12345.startingFrom(Pair.of("10", 10));
		twice(() -> assertThat(noStart, is(emptyIterable())));
	}

	@Test
	public void startingFromPredicate() {
		BiSequence<String, Integer> startingEmpty = empty.startingFrom(p -> p.getRight() == 5);
		twice(() -> assertThat(startingEmpty, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> startingEmpty.iterator().next());

		BiSequence<String, Integer> sequence = _123456789.startingFrom(p -> p.getRight() == 5);
		twice(() -> assertThat(sequence, contains(Pair.of("5", 5), Pair.of("6", 6), Pair.of("7", 7), Pair.of("8", 8),
		                                          Pair.of("9", 9))));

		assertThat(removeFirst(sequence), is(Pair.of("5", 5)));
		twice(() -> assertThat(sequence, is(emptyIterable())));

		BiSequence<String, Integer> noStart = _12345.startingFrom(p -> p.getRight() == 10);
		twice(() -> assertThat(noStart, is(emptyIterable())));
	}

	@Test
	public void startingFromBiPredicate() {
		BiSequence<String, Integer> startingEmpty = empty.startingFrom((l, r) -> r == 5);
		twice(() -> assertThat(startingEmpty, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> startingEmpty.iterator().next());

		BiSequence<String, Integer> sequence = _123456789.startingFrom((l, r) -> r == 5);
		twice(() -> assertThat(sequence, contains(Pair.of("5", 5), Pair.of("6", 6), Pair.of("7", 7), Pair.of("8", 8),
		                                          Pair.of("9", 9))));

		assertThat(removeFirst(sequence), is(Pair.of("5", 5)));
		twice(() -> assertThat(sequence, is(emptyIterable())));

		BiSequence<String, Integer> noStart = _12345.startingFrom((l, r) -> r == 10);
		twice(() -> assertThat(noStart, is(emptyIterable())));
	}

	@Test
	public void toList() {
		twice(() -> {
			List<Pair<String, Integer>> list = _12345.toList();
			assertThat(list, instanceOf(ArrayList.class));
			assertThat(list, contains(pairs12345));
		});
	}

	@Test
	public void toLinkedList() {
		twice(() -> {
			List<Pair<String, Integer>> list = _12345.toList(LinkedList::new);
			assertThat(list, instanceOf(LinkedList.class));
			assertThat(list, contains(pairs12345));
		});
	}

	@Test
	public void toSet() {
		twice(() -> {
			Set<Pair<String, Integer>> set = _12345.toSet();
			assertThat(set, instanceOf(HashSet.class));
			assertThat(set, containsInAnyOrder(pairs12345));
		});
	}

	@Test
	public void toSortedSet() {
		twice(() -> {
			SortedSet<Pair<String, Integer>> sortedSet = _12345.toSortedSet();
			assertThat(sortedSet, instanceOf(TreeSet.class));
			assertThat(sortedSet, contains(pairs12345));
		});
	}

	@Test
	public void toSetWithType() {
		twice(() -> {
			Set<Pair<String, Integer>> set = _12345.toSet(LinkedHashSet::new);
			assertThat(set, instanceOf(LinkedHashSet.class));
			assertThat(set, contains(pairs12345));
		});
	}

	@Test
	public void toCollection() {
		twice(() -> {
			Deque<Pair<String, Integer>> deque = _12345.toCollection(ArrayDeque::new);
			assertThat(deque, instanceOf(ArrayDeque.class));
			assertThat(deque, contains(pairs12345));
		});
	}

	@Test
	public void collectIntoCollection() {
		twice(() -> {
			Deque<Pair<String, Integer>> deque = new ArrayDeque<>();
			Deque<Pair<String, Integer>> result = _12345.collectInto(deque);

			assertThat(result, is(sameInstance(deque)));
			assertThat(result,
			           contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4), Pair.of("5", 5)));
		});
	}

	@Test
	public void toMap() {
		twice(() -> {
			Map<String, Integer> map = _1234.toMap();
			assertThat(map, instanceOf(HashMap.class));
			assertThat(map, is(equalTo(Maps.builder("1", 1).put("2", 2).put("3", 3).put("4", 4).build())));
		});
	}

	@Test
	public void toMapWithSupplier() {
		twice(() -> {
			Map<String, Integer> linkedMap = _1234.toMap(LinkedHashMap::new);
			assertThat(linkedMap, instanceOf(LinkedHashMap.class));
			assertThat(linkedMap, is(equalTo(Maps.builder("1", 1).put("2", 2).put("3", 3).put("4", 4).build())));
		});
	}

	@Test
	public void toMergedMap() {
		BiSequence<String, Integer> sequence = BiSequence.of(Pair.of("1", 1), Pair.of("1", 2),
		                                                     Pair.of("2", 2), Pair.of("2", 3),
		                                                     Pair.of("3", 3), Pair.of("4", 4));

		twice(() -> {
			Map<String, Integer> map = sequence.toMergedMap((old, value) -> old);
			assertThat(map, instanceOf(HashMap.class));
			assertThat(map, is(equalTo(Maps.builder("1", 1).put("2", 2).put("3", 3).put("4", 4).build())));
		});

		twice(() -> {
			Map<String, Integer> map = sequence.toMergedMap((old, value) -> value);
			assertThat(map, instanceOf(HashMap.class));
			assertThat(map, is(equalTo(Maps.builder("1", 2).put("2", 3).put("3", 3).put("4", 4).build())));
		});
	}

	@Test
	public void toMergedMapWithSupplier() {
		BiSequence<String, Integer> sequence = BiSequence.of(Pair.of("1", 1), Pair.of("1", 2),
		                                                     Pair.of("2", 2), Pair.of("2", 3),
		                                                     Pair.of("3", 3), Pair.of("4", 4));

		twice(() -> {
			Map<String, Integer> map = sequence.toMergedMap(LinkedHashMap::new, (old, value) -> old);
			assertThat(map, instanceOf(LinkedHashMap.class));
			assertThat(map, is(equalTo(Maps.builder("1", 1).put("2", 2).put("3", 3).put("4", 4).build())));
		});

		twice(() -> {
			Map<String, Integer> map = sequence.toMergedMap(LinkedHashMap::new, (old, value) -> value);
			assertThat(map, instanceOf(LinkedHashMap.class));
			assertThat(map, is(equalTo(Maps.builder("1", 2).put("2", 3).put("3", 3).put("4", 4).build())));
		});
	}

	@Test
	public void toGroupedMap() {
		twice(() -> assertThat(empty.toGroupedMap(), is(emptyMap())));
		twice(() -> assertThat(_1.toGroupedMap(), is(singletonMap("1", Lists.of(1)))));
		twice(() -> assertThat(_12.toGroupedMap(), is(Maps.builder()
		                                                  .put("1", Lists.of(1))
		                                                  .put("2", Lists.of(2))
		                                                  .build())));

		twice(() -> assertThat(empty.map((l, r) -> Pair.of(r / 3, r)).toGroupedMap(), is(emptyMap())));
		twice(() -> assertThat(_1.map((l, r) -> Pair.of(r / 3, r)).toGroupedMap(),
		                       is(singletonMap(0, Lists.of(1)))));
		twice(() -> assertThat(_12.map((l, r) -> Pair.of(r / 3, r)).toGroupedMap(),
		                       is(singletonMap(0, Lists.of(1, 2)))));
		twice(() -> assertThat(_12345.map((l, r) -> Pair.of(r / 3, r)).toGroupedMap(),
		                       is(Maps.builder()
		                              .put(0, Lists.of(1, 2))
		                              .put(1, Lists.of(3, 4, 5))
		                              .build())));

		twice(() -> {
			Map<Integer, List<Integer>> map = _123456789.map((l, r) -> Pair.of(r % 3 == 0 ? null : r % 3, r))
			                                            .toGroupedMap();

			assertThat(map, is(instanceOf(HashMap.class)));
			assertThat(map, is(equalTo(Maps.builder()
			                               .put(1, new ArrayList<>(Lists.of(1, 4, 7)))
			                               .put(2, new ArrayList<>(Lists.of(2, 5, 8)))
			                               .put(null, new ArrayList<>(Lists.of(3, 6, 9)))
			                               .build())));
		});
	}

	@Test
	public void toGroupedMapWithMapConstructor() {
		Supplier<Map<String, List<Integer>>> createLinkedHashMap = LinkedHashMap::new;
		twice(() -> assertThat(empty.toGroupedMap(createLinkedHashMap), is(emptyMap())));
		twice(() -> assertThat(_1.toGroupedMap(createLinkedHashMap), is(singletonMap("1", Lists.of(1)))));
		twice(() -> assertThat(_12.toGroupedMap(createLinkedHashMap), is(Maps.builder()
		                                                                     .put("1", Lists.of(1))
		                                                                     .put("2", Lists.of(2))
		                                                                     .build())));

		Supplier<Map<Integer, List<Integer>>> createLinkedHashMap2 = LinkedHashMap::new;
		twice(() -> assertThat(empty.map((l, r) -> Pair.of(r / 3, r)).toGroupedMap(createLinkedHashMap2),
		                       is(emptyMap())));
		twice(() -> assertThat(_1.map((l, r) -> Pair.of(r / 3, r)).toGroupedMap(createLinkedHashMap2),
		                       is(singletonMap(0, Lists.of(1)))));
		twice(() -> assertThat(_12.map((l, r) -> Pair.of(r / 3, r)).toGroupedMap(createLinkedHashMap2),
		                       is(singletonMap(0, Lists.of(1, 2)))));
		twice(() -> assertThat(_12345.map((l, r) -> Pair.of(r / 3, r)).toGroupedMap(createLinkedHashMap2),
		                       is(Maps.builder()
		                              .put(0, Lists.of(1, 2))
		                              .put(1, Lists.of(3, 4, 5))
		                              .build())));

		twice(() -> {
			Map<Integer, List<Integer>> map = _123456789.map((l, r) -> Pair.of(r % 3 == 0 ? null : r % 3, r))
			                                            .toGroupedMap(LinkedHashMap::new);

			assertThat(map, is(instanceOf(LinkedHashMap.class)));
			assertThat(map, is(equalTo(Maps.builder(LinkedHashMap::new)
			                               .put(1, new ArrayList<>(Lists.of(1, 4, 7)))
			                               .put(2, new ArrayList<>(Lists.of(2, 5, 8)))
			                               .put(null, new ArrayList<>(Lists.of(3, 6, 9)))
			                               .build())));

			// check order
			assertThat(map.keySet(), contains(1, 2, null));
			//noinspection unchecked
			assertThat(map.values(), contains(contains(1, 4, 7), contains(2, 5, 8), contains(3, 6, 9)));
		});
	}

	@Test
	public void toGroupedMapWithMapConstructorAndGroupConstructor() {
		Supplier<Map<String, List<Integer>>> createLinkedHashMap = LinkedHashMap::new;
		twice(() -> assertThat(empty.toGroupedMap(createLinkedHashMap, LinkedList::new), is(emptyMap())));
		twice(() -> assertThat(_1.toGroupedMap(createLinkedHashMap, LinkedList::new),
		                       is(singletonMap("1", Lists.of(1)))));
		twice(() -> assertThat(_12.toGroupedMap(createLinkedHashMap, LinkedList::new),
		                       is(Maps.builder()
		                              .put("1", Lists.of(1))
		                              .put("2", Lists.of(2))
		                              .build())));

		Supplier<Map<Integer, List<Integer>>> createLinkedHashMap2 = LinkedHashMap::new;
		twice(() -> assertThat(empty.map((l, r) -> Pair.of(r / 3, r)).toGroupedMap(createLinkedHashMap2,
		                                                                           LinkedList::new),
		                       is(emptyMap())));
		twice(() -> assertThat(_1.map((l, r) -> Pair.of(r / 3, r)).toGroupedMap(createLinkedHashMap2,
		                                                                        LinkedList::new),
		                       is(singletonMap(0, Lists.of(1)))));
		twice(() -> assertThat(_12.map((l, r) -> Pair.of(r / 3, r)).toGroupedMap(createLinkedHashMap2,
		                                                                         LinkedList::new),
		                       is(singletonMap(0, Lists.of(1, 2)))));
		twice(() -> assertThat(_12345.map((l, r) -> Pair.of(r / 3, r)).toGroupedMap(createLinkedHashMap2,
		                                                                            LinkedList::new),
		                       is(Maps.builder()
		                              .put(0, Lists.of(1, 2))
		                              .put(1, Lists.of(3, 4, 5))
		                              .build())));

		twice(() -> {
			Map<Integer, SortedSet<Integer>> map = _123456789.map((l, r) -> Pair.of(r % 3 == 0 ? null : r % 3, r))
			                                                 .toGroupedMap(LinkedHashMap::new, TreeSet::new);

			assertThat(map, is(instanceOf(LinkedHashMap.class)));
			assertThat(map, is(equalTo(Maps.builder(LinkedHashMap::new)
			                               .put(1, new TreeSet<>(Lists.of(1, 4, 7)))
			                               .put(2, new TreeSet<>(Lists.of(2, 5, 8)))
			                               .put(null, new TreeSet<>(Lists.of(3, 6, 9)))
			                               .build())));

			assertThat(map.get(1), is(instanceOf(TreeSet.class)));
			assertThat(map.get(2), is(instanceOf(TreeSet.class)));
			assertThat(map.get(null), is(instanceOf(TreeSet.class)));

			// check order
			assertThat(map.keySet(), contains(1, 2, null));
			//noinspection unchecked
			assertThat(map.values(), contains(contains(1, 4, 7), contains(2, 5, 8), contains(3, 6, 9)));
		});
	}

	@Test
	public void toGroupedMapWithMapConstructorAndCollector() {
		Collector<Integer, ?, List<Integer>> toLinkedList = Collectors.toCollection(LinkedList::new);

		Supplier<Map<String, List<Integer>>> createLinkedHashMap = LinkedHashMap::new;
		twice(() -> assertThat(empty.toGroupedMap(createLinkedHashMap, toLinkedList), is(emptyMap())));
		twice(() -> assertThat(_1.toGroupedMap(createLinkedHashMap, toLinkedList),
		                       is(singletonMap("1", Lists.of(1)))));
		twice(() -> assertThat(_12.toGroupedMap(createLinkedHashMap, toLinkedList),
		                       is(Maps.builder()
		                              .put("1", Lists.of(1))
		                              .put("2", Lists.of(2))
		                              .build())));

		Supplier<Map<Integer, List<Integer>>> createLinkedHashMap2 = LinkedHashMap::new;
		twice(() -> assertThat(
				empty.map((l, r) -> Pair.of(r / 3, r)).toGroupedMap(createLinkedHashMap2, toLinkedList),
				is(emptyMap())));
		twice(() -> assertThat(_1.map((l, r) -> Pair.of(r / 3, r)).toGroupedMap(createLinkedHashMap2, toLinkedList),
		                       is(singletonMap(0, Lists.of(1)))));
		twice(() -> assertThat(_12.map((l, r) -> Pair.of(r / 3, r)).toGroupedMap(createLinkedHashMap2,
		                                                                         toLinkedList),
		                       is(singletonMap(0, Lists.of(1, 2)))));
		twice(() -> assertThat(
				_12345.map((l, r) -> Pair.of(r / 3, r)).toGroupedMap(createLinkedHashMap2, toLinkedList),
				is(Maps.builder().put(0, Lists.of(1, 2)).put(1, Lists.of(3, 4, 5)).build())));

		twice(() -> {
			Map<Integer, List<Integer>> map = _123456789.map((l, r) -> Pair.of(r % 3 == 0 ? null : r % 3, r))
			                                            .toGroupedMap(LinkedHashMap::new, toLinkedList);

			assertThat(map, is(instanceOf(LinkedHashMap.class)));
			assertThat(map, is(equalTo(Maps.builder(LinkedHashMap::new)
			                               .put(1, new LinkedList<>(Lists.of(1, 4, 7)))
			                               .put(2, new LinkedList<>(Lists.of(2, 5, 8)))
			                               .put(null, new LinkedList<>(Lists.of(3, 6, 9)))
			                               .build())));

			assertThat(map.get(1), is(instanceOf(LinkedList.class)));
			assertThat(map.get(2), is(instanceOf(LinkedList.class)));
			assertThat(map.get(null), is(instanceOf(LinkedList.class)));

			// check order
			assertThat(map.keySet(), contains(1, 2, null));
			//noinspection unchecked
			assertThat(map.values(), contains(contains(1, 4, 7), contains(2, 5, 8), contains(3, 6, 9)));
		});
	}

	@Test
	public void toGroupedMapWithMapConstructorAndCollectorWithFinisher() {
		Collector<Integer, StringBuilder, String> toStringWithBuilder = new SequentialCollector<>(
				StringBuilder::new, StringBuilder::append, StringBuilder::toString);

		Supplier<Map<String, String>> createLinkedHashMap = LinkedHashMap::new;
		twice(() -> assertThat(empty.toGroupedMap(createLinkedHashMap, toStringWithBuilder), is(emptyMap())));
		twice(() -> assertThat(_1.toGroupedMap(createLinkedHashMap, toStringWithBuilder),
		                       is(singletonMap("1", "1"))));
		twice(() -> assertThat(_12.toGroupedMap(createLinkedHashMap, toStringWithBuilder),
		                       is(Maps.builder().put("1", "1").put("2", "2").build())));

		Supplier<Map<Integer, String>> createLinkedHashMap2 = LinkedHashMap::new;
		twice(() -> assertThat(empty.map((l, r) -> Pair.of(r / 3, r)).toGroupedMap(createLinkedHashMap2,
		                                                                           toStringWithBuilder),
		                       is(emptyMap())));
		twice(() -> assertThat(_1.map((l, r) -> Pair.of(r / 3, r)).toGroupedMap(createLinkedHashMap2,
		                                                                        toStringWithBuilder),
		                       is(singletonMap(0, "1"))));
		twice(() -> assertThat(_12.map((l, r) -> Pair.of(r / 3, r)).toGroupedMap(createLinkedHashMap2,
		                                                                         toStringWithBuilder),
		                       is(singletonMap(0, "12"))));
		twice(() -> assertThat(_12345.map((l, r) -> Pair.of(r / 3, r)).toGroupedMap(createLinkedHashMap2,
		                                                                            toStringWithBuilder),
		                       is(Maps.builder().put(0, "12").put(1, "345").build())));

		twice(() -> {
			Map<Integer, String> map = _123456789.map((l, r) -> Pair.of(r % 3 == 0 ? null : r % 3, r))
			                                     .toGroupedMap(LinkedHashMap::new, toStringWithBuilder);

			assertThat(map, is(instanceOf(LinkedHashMap.class)));
			assertThat(map, is(equalTo(Maps.builder(LinkedHashMap::new)
			                               .put(1, "147")
			                               .put(2, "258")
			                               .put(null, "369")
			                               .build())));

			// check order
			assertThat(map.keySet(), contains(1, 2, null));
			//noinspection unchecked
			assertThat(map.values(), contains("147", "258", "369"));
		});
	}

	@Test
	public void toSortedMap() {
		twice(() -> {
			SortedMap<String, Integer> sortedMap = random3.toSortedMap();

			assertThat(sortedMap, instanceOf(TreeMap.class));
			assertThat(sortedMap, is(equalTo(Maps.builder("2", 2).put("3", 3).put("4", 4).build())));
		});
	}

	@Test
	public void collect() {
		twice(() -> {
			Deque<Pair<String, Integer>> deque = _123.collect(ArrayDeque::new, ArrayDeque::add);

			assertThat(deque, instanceOf(ArrayDeque.class));
			assertThat(deque, contains(pairs123));
		});
	}

	@Test
	public void collectInto() {
		twice(() -> {
			ArrayDeque<Pair<String, Integer>> original = new ArrayDeque<>();
			Deque<Pair<String, Integer>> deque = _123.collectInto(original, ArrayDeque::add);

			assertThat(deque, is(sameInstance(original)));
			assertThat(deque, contains(pairs123));
		});
	}

	@Test
	public void toArray() {
		twice(() -> assertThat(_123.toArray(), is(arrayContaining(pairs123))));
	}

	@Test
	public void toArrayWithType() {
		twice(() -> assertThat(_123.toArray(Pair[]::new), arrayContaining(pairs123)));
	}

	@Test
	public void collector() {
		twice(() -> assertThat(_123.collect(Collectors.toList()), contains(pairs123)));
	}

	@Test
	public void join() {
		twice(() -> assertThat(_123.join(", "), is("(\"1\", 1), (\"2\", 2), (\"3\", 3)")));
	}

	@Test
	public void joinWithPrefixAndSuffix() {
		twice(() -> assertThat(_123.join("<", ", ", ">"), is("<(\"1\", 1), (\"2\", 2), (\"3\", 3)>")));
	}

	@Test
	public void reduce() {
		BinaryOperator<Pair<String, Integer>> sumPair =
				(r, e) -> Pair.of(r.getLeft() + e.getLeft(), r.getRight() + e.getRight());

		twice(() -> {
			assertThat(empty.reduce(sumPair), is(Optional.empty()));
			assertThat(_1.reduce(sumPair), is(Optional.of(Pair.of("1", 1))));
			assertThat(_12.reduce(sumPair), is(Optional.of(Pair.of("12", 3))));
			assertThat(_123.reduce(sumPair), is(Optional.of(Pair.of("123", 6))));
		});
	}

	@Test
	public void reduceQuaternary() {
		QuaternaryFunction<String, Integer, String, Integer, Pair<String, Integer>> sumPair =
				(rl, rr, l, r) -> Pair.of(rl + l, rr + r);

		twice(() -> {
			assertThat(empty.reduce(sumPair), is(Optional.empty()));
			assertThat(_1.reduce(sumPair), is(Optional.of(Pair.of("1", 1))));
			assertThat(_12.reduce(sumPair), is(Optional.of(Pair.of("12", 3))));
			assertThat(_123.reduce(sumPair), is(Optional.of(Pair.of("123", 6))));
		});
	}

	@Test
	public void reduceWithIdentity() {
		BinaryOperator<Pair<String, Integer>> sumPair =
				(r, p) -> Pair.of(r.getLeft() + p.getLeft(), r.getRight() + p.getRight());

		twice(() -> {
			assertThat(empty.reduce(Pair.of("17", 17), sumPair), is(Pair.of("17", 17)));
			assertThat(_1.reduce(Pair.of("17", 17), sumPair), is(Pair.of("171", 18)));
			assertThat(_12.reduce(Pair.of("17", 17), sumPair), is(Pair.of("1712", 20)));
			assertThat(_123.reduce(Pair.of("17", 17), sumPair), is(Pair.of("17123", 23)));
		});
	}

	@Test
	public void reduceQuaternaryWithIdentity() {
		QuaternaryFunction<String, Integer, String, Integer, Pair<String, Integer>> sumPair =
				(rl, rr, l, r) -> Pair.of(rl + l, rr + r);

		twice(() -> {
			assertThat(empty.reduce("17", 17, sumPair), is(Pair.of("17", 17)));
			assertThat(_1.reduce("17", 17, sumPair), is(Pair.of("171", 18)));
			assertThat(_12.reduce("17", 17, sumPair), is(Pair.of("1712", 20)));
			assertThat(_123.reduce("17", 17, sumPair), is(Pair.of("17123", 23)));
		});
	}

	@Test
	public void first() {
		twice(() -> {
			assertThat(empty.first(), is(Optional.empty()));
			assertThat(_1.first(), is(Optional.of(Pair.of("1", 1))));
			assertThat(_12.first(), is(Optional.of(Pair.of("1", 1))));
			assertThat(_123.first(), is(Optional.of(Pair.of("1", 1))));
		});
	}

	@Test
	public void last() {
		twice(() -> {
			assertThat(empty.last(), is(Optional.empty()));
			assertThat(_1.last(), is(Optional.of(Pair.of("1", 1))));
			assertThat(_12.last(), is(Optional.of(Pair.of("2", 2))));
			assertThat(_123.last(), is(Optional.of(Pair.of("3", 3))));
		});
	}

	@Test
	public void at() {
		twice(() -> {
			assertThat(empty.at(0), is(Optional.empty()));
			assertThat(empty.at(17), is(Optional.empty()));

			assertThat(_1.at(0), is(Optional.of(Pair.of("1", 1))));
			assertThat(_1.at(1), is(Optional.empty()));
			assertThat(_1.at(17), is(Optional.empty()));

			assertThat(_12345.at(0), is(Optional.of(Pair.of("1", 1))));
			assertThat(_12345.at(1), is(Optional.of(Pair.of("2", 2))));
			assertThat(_12345.at(4), is(Optional.of(Pair.of("5", 5))));
			assertThat(_12345.at(17), is(Optional.empty()));
		});
	}

	@Test
	public void firstByPredicate() {
		twice(() -> {
			assertThat(empty.first(p -> p.getRight() > 1), is(Optional.empty()));
			assertThat(_1.first(p -> p.getRight() > 1), is(Optional.empty()));
			assertThat(_12.first(p -> p.getRight() > 1), is(Optional.of(Pair.of("2", 2))));
			assertThat(_123.first(p -> p.getRight() > 1), is(Optional.of(Pair.of("2", 2))));
		});
	}

	@Test
	public void lastByPredicate() {
		twice(() -> {
			assertThat(empty.last(p -> p.getRight() > 1), is(Optional.empty()));
			assertThat(_1.last(p -> p.getRight() > 1), is(Optional.empty()));
			assertThat(_12.last(p -> p.getRight() > 1), is(Optional.of(Pair.of("2", 2))));
			assertThat(_123.last(p -> p.getRight() > 1), is(Optional.of(Pair.of("3", 3))));
		});
	}

	@Test
	public void atByPredicate() {
		twice(() -> {
			assertThat(empty.at(0, p -> p.getRight() > 1), is(Optional.empty()));
			assertThat(empty.at(17, p -> p.getRight() > 1), is(Optional.empty()));

			assertThat(_1.at(0, p -> p.getRight() > 1), is(Optional.empty()));
			assertThat(_1.at(17, p -> p.getRight() > 1), is(Optional.empty()));

			assertThat(_12.at(0, p -> p.getRight() > 1), is(Optional.of(Pair.of("2", 2))));
			assertThat(_12.at(1, p -> p.getRight() > 1), is(Optional.empty()));
			assertThat(_12.at(17, p -> p.getRight() > 1), is(Optional.empty()));

			assertThat(_12345.at(0, p -> p.getRight() > 1), is(Optional.of(Pair.of("2", 2))));
			assertThat(_12345.at(1, p -> p.getRight() > 1), is(Optional.of(Pair.of("3", 3))));
			assertThat(_12345.at(3, p -> p.getRight() > 1), is(Optional.of(Pair.of("5", 5))));
			assertThat(_12345.at(4, p -> p.getRight() > 1), is(Optional.empty()));
			assertThat(_12345.at(17, p -> p.getRight() > 1), is(Optional.empty()));
		});
	}

	@Test
	public void firstByBiPredicate() {
		twice(() -> {
			assertThat(empty.first((l, r) -> r > 1), is(Optional.empty()));
			assertThat(_1.first((l, r) -> r > 1), is(Optional.empty()));
			assertThat(_12.first((l, r) -> r > 1), is(Optional.of(Pair.of("2", 2))));
			assertThat(_123.first((l, r) -> r > 1), is(Optional.of(Pair.of("2", 2))));
		});
	}

	@Test
	public void lastByBiPredicate() {
		twice(() -> {
			assertThat(empty.last((l, r) -> r > 1), is(Optional.empty()));
			assertThat(_1.last((l, r) -> r > 1), is(Optional.empty()));
			assertThat(_12.last((l, r) -> r > 1), is(Optional.of(Pair.of("2", 2))));
			assertThat(_123.last((l, r) -> r > 1), is(Optional.of(Pair.of("3", 3))));
		});
	}

	@Test
	public void atByBiPredicate() {
		twice(() -> {
			assertThat(empty.at(0, (l, r) -> r > 1), is(Optional.empty()));
			assertThat(empty.at(17, (l, r) -> r > 1), is(Optional.empty()));

			assertThat(_1.at(0, (l, r) -> r > 1), is(Optional.empty()));
			assertThat(_1.at(17, (l, r) -> r > 1), is(Optional.empty()));

			assertThat(_12.at(0, (l, r) -> r > 1), is(Optional.of(Pair.of("2", 2))));
			assertThat(_12.at(1, (l, r) -> r > 1), is(Optional.empty()));
			assertThat(_12.at(17, (l, r) -> r > 1), is(Optional.empty()));

			assertThat(_12345.at(0, (l, r) -> r > 1), is(Optional.of(Pair.of("2", 2))));
			assertThat(_12345.at(1, (l, r) -> r > 1), is(Optional.of(Pair.of("3", 3))));
			assertThat(_12345.at(3, (l, r) -> r > 1), is(Optional.of(Pair.of("5", 5))));
			assertThat(_12345.at(4, (l, r) -> r > 1), is(Optional.empty()));
			assertThat(_12345.at(17, (l, r) -> r > 1), is(Optional.empty()));
		});
	}

	@Test
	public void window() {
		Sequence<BiSequence<String, Integer>> emptyWindowed = empty.window(3);
		twice(() -> assertThat(emptyWindowed, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyWindowed.iterator().next());

		Sequence<BiSequence<String, Integer>> windowed = _12345.window(3);
		twice(() -> assertThat(windowed,
		                       contains(contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3)),
		                                contains(Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4)),
		                                contains(Pair.of("3", 3), Pair.of("4", 4), Pair.of("5", 5)))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(windowed));
		twice(() -> assertThat(windowed,
		                       contains(contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3)),
		                                contains(Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4)),
		                                contains(Pair.of("3", 3), Pair.of("4", 4), Pair.of("5", 5)))));
	}

	@Test
	public void windowWithStep() {
		Sequence<BiSequence<String, Integer>> emptyWindowed = empty.window(3, 2);
		twice(() -> assertThat(emptyWindowed, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyWindowed.iterator().next());

		Sequence<BiSequence<String, Integer>> windowed = _12345.window(3, 2);
		twice(() -> assertThat(windowed,
		                       contains(contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3)),
		                                contains(Pair.of("3", 3), Pair.of("4", 4), Pair.of("5", 5)))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(windowed));
		twice(() -> assertThat(windowed,
		                       contains(contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3)),
		                                contains(Pair.of("3", 3), Pair.of("4", 4), Pair.of("5", 5)))));
	}

	@Test
	public void batch() {
		Sequence<BiSequence<String, Integer>> emptyBatched = empty.batch(3);
		twice(() -> assertThat(emptyBatched, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyBatched.iterator().next());

		Sequence<BiSequence<String, Integer>> batched = _12345.batch(3);
		twice(() -> assertThat(batched, contains(contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3)),
		                                         contains(Pair.of("4", 4), Pair.of("5", 5)))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(batched));
		twice(() -> assertThat(batched,
		                       contains(contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3)),
		                                contains(Pair.of("4", 4), Pair.of("5", 5)))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void batchOnPredicate() {
		Sequence<BiSequence<String, Integer>> emptyBatched = empty.batch((a, b) -> a.getRight() > b.getRight());
		twice(() -> assertThat(emptyBatched, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyBatched.iterator().next());

		Sequence<BiSequence<String, Integer>> oneBatched = _1.batch((a, b) -> a.getRight() > b.getRight());
		twice(() -> assertThat(oneBatched, contains(contains(Pair.of("1", 1)))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(oneBatched));
		twice(() -> assertThat(oneBatched, contains(contains(Pair.of("1", 1)))));

		Sequence<BiSequence<String, Integer>> twoBatched = _12.batch((a, b) -> a.getRight() > b.getRight());
		twice(() -> assertThat(twoBatched, contains(contains(Pair.of("1", 1), Pair.of("2", 2)))));

		Sequence<BiSequence<String, Integer>> threeBatched = _123.batch((a, b) -> a.getRight() > b.getRight());
		twice(() -> assertThat(threeBatched,
		                       contains(contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3)))));

		Sequence<BiSequence<String, Integer>> threeRandomBatched =
				random3.batch((a, b) -> a.getRight() > b.getRight());
		twice(() -> assertThat(threeRandomBatched,
		                       contains(contains(Pair.of("4", 4)), contains(Pair.of("2", 2), Pair.of("3", 3)))));

		Sequence<BiSequence<String, Integer>> nineRandomBatched =
				random9.batch((a, b) -> a.getRight() > b.getRight());
		twice(() -> assertThat(nineRandomBatched,
		                       contains(contains(Pair.of("67", 67)), contains(Pair.of("5", 5), Pair.of("43", 43)),
		                                contains(Pair.of("3", 3), Pair.of("5", 5), Pair.of("7", 7), Pair.of("24", 24)),
		                                contains(Pair.of("5", 5), Pair.of("67", 67)))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void batchOnQuaternaryPredicate() {
		Sequence<BiSequence<String, Integer>> emptyBatched = empty.batch((a, b, c, d) -> b > d);
		twice(() -> assertThat(emptyBatched, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyBatched.iterator().next());

		Sequence<BiSequence<String, Integer>> oneBatched = _1.batch((a, b, c, d) -> b > d);
		twice(() -> assertThat(oneBatched, contains(contains(Pair.of("1", 1)))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(oneBatched));
		twice(() -> assertThat(oneBatched, contains(contains(Pair.of("1", 1)))));

		Sequence<BiSequence<String, Integer>> twoBatched = _12.batch((a, b, c, d) -> b > d);
		twice(() -> assertThat(twoBatched, contains(contains(Pair.of("1", 1), Pair.of("2", 2)))));

		Sequence<BiSequence<String, Integer>> threeBatched = _123.batch((a, b, c, d) -> b > d);
		twice(() -> assertThat(threeBatched,
		                       contains(contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3)))));

		Sequence<BiSequence<String, Integer>> threeRandomBatched = random3.batch((a, b, c, d) -> b > d);
		twice(() -> assertThat(threeRandomBatched,
		                       contains(contains(Pair.of("4", 4)), contains(Pair.of("2", 2), Pair.of("3", 3)))));

		Sequence<BiSequence<String, Integer>> nineRandomBatched = random9.batch((a, b, c, d) -> b > d);
		twice(() -> assertThat(nineRandomBatched,
		                       contains(contains(Pair.of("67", 67)), contains(Pair.of("5", 5), Pair.of("43", 43)),
		                                contains(Pair.of("3", 3), Pair.of("5", 5), Pair.of("7", 7), Pair.of("24", 24)),
		                                contains(Pair.of("5", 5), Pair.of("67", 67)))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void split() {
		Sequence<BiSequence<String, Integer>> emptySplit = empty.split(Pair.of("3", 3));
		twice(() -> assertThat(emptySplit, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptySplit.iterator().next());

		Sequence<BiSequence<String, Integer>> oneSplit = _1.split(Pair.of("3", 3));
		twice(() -> assertThat(oneSplit, contains(contains(Pair.of("1", 1)))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(oneSplit));
		twice(() -> assertThat(oneSplit, contains(contains(Pair.of("1", 1)))));

		Sequence<BiSequence<String, Integer>> twoSplit = _12.split(Pair.of("3", 3));
		twice(() -> assertThat(twoSplit, contains(contains(Pair.of("1", 1), Pair.of("2", 2)))));

		Sequence<BiSequence<String, Integer>> threeSplit = _123.split(Pair.of("3", 3));
		twice(() -> assertThat(threeSplit, contains(contains(Pair.of("1", 1), Pair.of("2", 2)))));

		Sequence<BiSequence<String, Integer>> fiveSplit = _12345.split(Pair.of("3", 3));
		twice(() -> assertThat(fiveSplit, contains(contains(Pair.of("1", 1), Pair.of("2", 2)),
		                                           contains(Pair.of("4", 4), Pair.of("5", 5)))));

		Sequence<BiSequence<String, Integer>> nineSplit = _123456789.split(Pair.of("3", 3));
		twice(() -> assertThat(nineSplit, contains(contains(Pair.of("1", 1), Pair.of("2", 2)),
		                                           contains(Pair.of("4", 4), Pair.of("5", 5), Pair.of("6", 6),
		                                                    Pair.of("7", 7), Pair.of("8", 8), Pair.of("9", 9)))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void splitPredicate() {
		Sequence<BiSequence<String, Integer>> emptySplit = empty.split(x -> x.getRight() % 3 == 0);
		twice(() -> assertThat(emptySplit, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptySplit.iterator().next());

		Sequence<BiSequence<String, Integer>> oneSplit = _1.split(x -> x.getRight() % 3 == 0);
		twice(() -> assertThat(oneSplit, contains(contains(Pair.of("1", 1)))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(oneSplit));
		twice(() -> assertThat(oneSplit, contains(contains(Pair.of("1", 1)))));

		Sequence<BiSequence<String, Integer>> twoSplit = _12.split(x -> x.getRight() % 3 == 0);
		twice(() -> assertThat(twoSplit, contains(contains(Pair.of("1", 1), Pair.of("2", 2)))));

		Sequence<BiSequence<String, Integer>> threeSplit = _123.split(x -> x.getRight() % 3 == 0);
		twice(() -> assertThat(threeSplit, contains(contains(Pair.of("1", 1), Pair.of("2", 2)))));

		Sequence<BiSequence<String, Integer>> fiveSplit = _12345.split(x -> x.getRight() % 3 == 0);
		twice(() -> assertThat(fiveSplit, contains(contains(Pair.of("1", 1), Pair.of("2", 2)),
		                                           contains(Pair.of("4", 4), Pair.of("5", 5)))));

		Sequence<BiSequence<String, Integer>> nineSplit = _123456789.split(x -> x.getRight() % 3 == 0);
		twice(() -> assertThat(nineSplit, contains(contains(Pair.of("1", 1), Pair.of("2", 2)),
		                                           contains(Pair.of("4", 4), Pair.of("5", 5)),
		                                           contains(Pair.of("7", 7), Pair.of("8", 8)))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void splitLeftRight() {
		Sequence<BiSequence<String, Integer>> emptySplit = empty.split((l, r) -> r % 3 == 0);
		twice(() -> assertThat(emptySplit, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptySplit.iterator().next());

		Sequence<BiSequence<String, Integer>> oneSplit = _1.split((l, r) -> r % 3 == 0);
		twice(() -> assertThat(oneSplit, contains(contains(Pair.of("1", 1)))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(oneSplit));
		twice(() -> assertThat(oneSplit, contains(contains(Pair.of("1", 1)))));

		Sequence<BiSequence<String, Integer>> twoSplit = _12.split((l, r) -> r % 3 == 0);
		twice(() -> assertThat(twoSplit, contains(contains(Pair.of("1", 1), Pair.of("2", 2)))));

		Sequence<BiSequence<String, Integer>> threeSplit = _123.split((l, r) -> r % 3 == 0);
		twice(() -> assertThat(threeSplit, contains(contains(Pair.of("1", 1), Pair.of("2", 2)))));

		Sequence<BiSequence<String, Integer>> fiveSplit = _12345.split((l, r) -> r % 3 == 0);
		twice(() -> assertThat(fiveSplit, contains(contains(Pair.of("1", 1), Pair.of("2", 2)),
		                                           contains(Pair.of("4", 4), Pair.of("5", 5)))));

		Sequence<BiSequence<String, Integer>> nineSplit = _123456789.split((l, r) -> r % 3 == 0);
		twice(() -> assertThat(nineSplit, contains(contains(Pair.of("1", 1), Pair.of("2", 2)),
		                                           contains(Pair.of("4", 4), Pair.of("5", 5)),
		                                           contains(Pair.of("7", 7), Pair.of("8", 8)))));
	}

	@Test
	public void step() {
		BiSequence<String, Integer> emptyStep3 = empty.step(3);
		twice(() -> assertThat(emptyStep3, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyStep3.iterator().next());

		BiSequence<String, Integer> nineStep3 = _123456789.step(3);
		twice(() -> assertThat(nineStep3, contains(Pair.of("1", 1), Pair.of("4", 4), Pair.of("7", 7))));

		assertThat(removeFirst(nineStep3), is(Pair.of("1", 1)));
		twice(() -> assertThat(nineStep3, contains(Pair.of("2", 2), Pair.of("5", 5), Pair.of("8", 8))));
	}

	@Test
	public void distinct() {
		BiSequence<String, Integer> emptyDistinct = empty.distinct();
		twice(() -> assertThat(emptyDistinct, emptyIterable()));
		expecting(NoSuchElementException.class, () -> emptyDistinct.iterator().next());

		BiSequence<String, Integer> oneDistinct = random1.distinct();
		twice(() -> assertThat(oneDistinct, contains(Pair.of("17", 17))));

		assertThat(removeFirst(oneDistinct), is(Pair.of("17", 17)));
		twice(() -> assertThat(oneDistinct, is(emptyIterable())));

		BiSequence<String, Integer> twoDuplicatesDistinct =
				BiSequence.of(Pair.of("17", 17), Pair.of("17", 17)).distinct();
		twice(() -> assertThat(twoDuplicatesDistinct, contains(Pair.of("17", 17))));

		BiSequence<String, Integer> nineDistinct = random9.distinct();
		twice(() -> assertThat(nineDistinct,
		                       contains(Pair.of("67", 67), Pair.of("5", 5), Pair.of("43", 43), Pair.of("3", 3),
		                                Pair.of("7", 7), Pair.of("24", 24))));
	}

	@Test
	public void sorted() {
		BiSequence<String, Integer> emptySorted = empty.sorted();
		twice(() -> assertThat(emptySorted, emptyIterable()));
		expecting(NoSuchElementException.class, () -> emptySorted.iterator().next());

		BiSequence<String, Integer> oneSorted = random1.sorted();
		twice(() -> assertThat(oneSorted, contains(Pair.of("17", 17))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(oneSorted));
		twice(() -> assertThat(oneSorted, contains(Pair.of("17", 17))));

		BiSequence<String, Integer> twoSorted = random2.sorted();
		twice(() -> assertThat(twoSorted, contains(Pair.of("17", 17), Pair.of("32", 32))));

		BiSequence<String, Integer> nineSorted = random9.sorted();
		twice(() -> assertThat(nineSorted, // String sorting on first item
		                       contains(Pair.of("24", 24), Pair.of("3", 3), Pair.of("43", 43), Pair.of("5", 5),
		                                Pair.of("5", 5), Pair.of("5", 5), Pair.of("67", 67), Pair.of("67", 67),
		                                Pair.of("7", 7))));
	}

	@Test
	public void sortedComparator() {
		BiSequence<String, Integer> emptySorted = empty.sorted((Comparator) reverseOrder());
		twice(() -> assertThat(emptySorted, emptyIterable()));
		expecting(NoSuchElementException.class, () -> emptySorted.iterator().next());

		BiSequence<String, Integer> oneSorted = random1.sorted((Comparator) reverseOrder());
		twice(() -> assertThat(oneSorted, contains(Pair.of("17", 17))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(oneSorted));
		twice(() -> assertThat(oneSorted, contains(Pair.of("17", 17))));

		BiSequence<String, Integer> twoSorted = random2.sorted((Comparator) reverseOrder());
		twice(() -> assertThat(twoSorted, contains(Pair.of("32", 32), Pair.of("17", 17))));

		BiSequence<String, Integer> nineSorted = random9.sorted((Comparator) reverseOrder());
		twice(() -> assertThat(nineSorted, // String sorting on first item reverse
		                       contains(Pair.of("7", 7), Pair.of("67", 67), Pair.of("67", 67), Pair.of("5", 5),
		                                Pair.of("5", 5), Pair.of("5", 5), Pair.of("43", 43), Pair.of("3", 3),
		                                Pair.of("24", 24))));
	}

	@Test
	public void min() {
		twice(() -> assertThat(empty.min(), is(Optional.empty())));
		twice(() -> assertThat(random1.min(), is(Optional.of(Pair.of("17", 17)))));
		twice(() -> assertThat(random2.min(), is(Optional.of(Pair.of("17", 17)))));
		twice(() -> assertThat(random9.min(), is(Optional.of(Pair.of("24", 24)))));
	}

	@Test
	public void max() {
		twice(() -> assertThat(empty.max(), is(Optional.empty())));
		twice(() -> assertThat(random1.max(), is(Optional.of(Pair.of("17", 17)))));
		twice(() -> assertThat(random2.max(), is(Optional.of(Pair.of("32", 32)))));
		twice(() -> assertThat(random9.max(), is(Optional.of(Pair.of("7", 7)))));
	}

	@Test
	public void minByComparator() {
		twice(() -> assertThat(empty.min(reverseOrder()), is(Optional.empty())));
		twice(() -> assertThat(random1.min(reverseOrder()), is(Optional.of(Pair.of("17", 17)))));
		twice(() -> assertThat(random2.min(reverseOrder()), is(Optional.of(Pair.of("32", 32)))));
		twice(() -> assertThat(random9.min(reverseOrder()), is(Optional.of(Pair.of("7", 7)))));
	}

	@Test
	public void maxByComparator() {
		twice(() -> assertThat(empty.max(reverseOrder()), is(Optional.empty())));
		twice(() -> assertThat(random1.max(reverseOrder()), is(Optional.of(Pair.of("17", 17)))));
		twice(() -> assertThat(random2.max(reverseOrder()), is(Optional.of(Pair.of("17", 17)))));
		twice(() -> assertThat(random9.max(reverseOrder()), is(Optional.of(Pair.of("24", 24)))));
	}

	@Test
	public void size() {
		twice(() -> assertThat(empty.size(), is(0)));
		twice(() -> assertThat(_1.size(), is(1)));
		twice(() -> assertThat(_12.size(), is(2)));
		twice(() -> assertThat(_123456789.size(), is(9)));
	}

	@Test
	public void any() {
		twice(() -> assertThat(_123.any((s, x) -> x > 0 && x == parseInt(s)), is(true)));
		twice(() -> assertThat(_123.any((s, x) -> x > 2 && x == parseInt(s)), is(true)));
		twice(() -> assertThat(_123.any((s, x) -> x > 4 && x == parseInt(s)), is(false)));
	}

	@Test
	public void all() {
		twice(() -> assertThat(_123.all((s, x) -> x > 0 && x == parseInt(s)), is(true)));
		twice(() -> assertThat(_123.all((s, x) -> x > 2 && x == parseInt(s)), is(false)));
		twice(() -> assertThat(_123.all((s, x) -> x > 4 && x == parseInt(s)), is(false)));
	}

	@Test
	public void none() {
		twice(() -> assertThat(_123.none((s, x) -> x > 0 && x == parseInt(s)), is(false)));
		twice(() -> assertThat(_123.none((s, x) -> x > 2 && x == parseInt(s)), is(false)));
		twice(() -> assertThat(_123.none((s, x) -> x > 4 && x == parseInt(s)), is(true)));
	}

	@Test
	public void peek() {
		BiSequence<String, Integer> emptyPeeked = empty.peek((l, r) -> {
			throw new IllegalStateException("Should not get called");
		});
		twice(() -> assertThat(emptyPeeked, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyPeeked.iterator().next());

		AtomicInteger index = new AtomicInteger();
		BiSequence<String, Integer> onePeeked = _1.peek((l, r) -> {
			index.getAndIncrement();
			assertThat(l, is(String.valueOf(index.get())));
			assertThat(r, is(index.get()));
		});
		twiceIndexed(index, 1, () -> assertThat(onePeeked, contains(Pair.of("1", 1))));

		assertThat(removeFirst(onePeeked), is(Pair.of("1", 1)));
		index.set(0);
		twice(() -> assertThat(onePeeked, is(emptyIterable())));

		BiSequence<String, Integer> twoPeeked = _12.peek((l, r) -> {
			index.getAndIncrement();
			assertThat(l, is(String.valueOf(index.get())));
			assertThat(r, is(index.get()));
		});
		twiceIndexed(index, 2, () -> assertThat(twoPeeked, contains(Pair.of("1", 1), Pair.of("2", 2))));

		BiSequence<String, Integer> fivePeeked = _12345.peek((l, r) -> {
			index.getAndIncrement();
			assertThat(l, is(String.valueOf(index.get())));
			assertThat(r, is(index.get()));
		});
		twiceIndexed(index, 5, () -> assertThat(fivePeeked, contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3),
		                                                             Pair.of("4", 4), Pair.of("5", 5))));
	}

	@Test
	public void peekIndexed() {
		BiSequence<String, Integer> emptyPeeked = empty.peekIndexed((l, r, i) -> {
			throw new IllegalStateException("Should not get called");
		});
		twice(() -> assertThat(emptyPeeked, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyPeeked.iterator().next());

		AtomicInteger index = new AtomicInteger();
		BiSequence<String, Integer> onePeeked = _1.peekIndexed((l, r, i) -> {
			assertThat(i, is(index.getAndIncrement()));
			assertThat(l, is(String.valueOf(index.get())));
			assertThat(r, is(index.get()));
		});
		twiceIndexed(index, 1, () -> assertThat(onePeeked, contains(Pair.of("1", 1))));

		assertThat(removeFirst(onePeeked), is(Pair.of("1", 1)));
		index.set(0);
		twice(() -> assertThat(onePeeked, is(emptyIterable())));

		BiSequence<String, Integer> twoPeeked = _12.peekIndexed((l, r, i) -> {
			assertThat(i, is(index.getAndIncrement()));
			assertThat(l, is(String.valueOf(index.get())));
			assertThat(r, is(index.get()));
		});
		twiceIndexed(index, 2, () -> assertThat(twoPeeked, contains(Pair.of("1", 1), Pair.of("2", 2))));

		BiSequence<String, Integer> fivePeeked = _12345.peekIndexed((l, r, i) -> {
			assertThat(i, is(index.getAndIncrement()));
			assertThat(l, is(String.valueOf(index.get())));
			assertThat(r, is(index.get()));
		});
		twiceIndexed(index, 5, () -> assertThat(fivePeeked, contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3),
		                                                             Pair.of("4", 4), Pair.of("5", 5))));
	}

	@Test
	public void peekPair() {
		BiSequence<String, Integer> emptyPeeked = empty.peek(p -> {
			throw new IllegalStateException("Should not get called");
		});
		twice(() -> assertThat(emptyPeeked, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyPeeked.iterator().next());

		AtomicInteger value = new AtomicInteger(1);
		BiSequence<String, Integer> onePeeked = _1.peek(
				p -> assertThat(p, is(Pair.of(String.valueOf(value.get()), value.getAndIncrement()))));
		twiceIndexed(value, 1, () -> assertThat(onePeeked, contains(Pair.of("1", 1))));

		assertThat(removeFirst(onePeeked), is(Pair.of("1", 1)));
		value.set(1);
		twice(() -> assertThat(onePeeked, is(emptyIterable())));

		BiSequence<String, Integer> twoPeeked = _12.peek(
				p -> assertThat(p, is(Pair.of(String.valueOf(value.get()), value.getAndIncrement()))));
		twiceIndexed(value, 2, () -> assertThat(twoPeeked, contains(Pair.of("1", 1), Pair.of("2", 2))));

		BiSequence<String, Integer> fivePeeked = _12345.peek(
				p -> assertThat(p, is(Pair.of(String.valueOf(value.get()), value.getAndIncrement()))));
		twiceIndexed(value, 5, () -> assertThat(fivePeeked, contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3),
		                                                             Pair.of("4", 4), Pair.of("5", 5))));
	}

	@Test
	public void peekIndexedPair() {
		BiSequence<String, Integer> emptyPeeked = empty.peekIndexed((p, i) -> {
			throw new IllegalStateException("Should not get called");
		});
		twice(() -> assertThat(emptyPeeked, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyPeeked.iterator().next());

		AtomicInteger index = new AtomicInteger();
		BiSequence<String, Integer> onePeeked = _1.peekIndexed((p, i) -> {
			assertThat(i, is(index.getAndIncrement()));
			assertThat(p, is(Pair.of(String.valueOf(index.get()), index.get())));
		});
		twiceIndexed(index, 1, () -> assertThat(onePeeked, contains(Pair.of("1", 1))));

		assertThat(removeFirst(onePeeked), is(Pair.of("1", 1)));
		index.set(0);
		twice(() -> assertThat(onePeeked, is(emptyIterable())));

		BiSequence<String, Integer> twoPeeked = _12.peekIndexed((p, i) -> {
			assertThat(i, is(index.getAndIncrement()));
			assertThat(p, is(Pair.of(String.valueOf(index.get()), index.get())));
		});
		twiceIndexed(index, 2, () -> assertThat(twoPeeked, contains(Pair.of("1", 1), Pair.of("2", 2))));

		BiSequence<String, Integer> fivePeeked = _12345.peekIndexed((p, i) -> {
			assertThat(i, is(index.getAndIncrement()));
			assertThat(p, is(Pair.of(String.valueOf(index.get()), index.get())));
		});
		twiceIndexed(index, 5, () -> assertThat(fivePeeked, contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3),
		                                                             Pair.of("4", 4), Pair.of("5", 5))));
	}

	@Test
	public void stream() {
		twice(() -> assertThat(empty.stream().collect(Collectors.toList()), is(emptyIterable())));
		twice(() -> assertThat(empty, is(emptyIterable())));

		twice(() -> assertThat(_12345.stream().collect(Collectors.toList()),
		                       contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4),
		                                Pair.of("5", 5))));
		twice(() -> assertThat(_12345, contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4),
		                                        Pair.of("5", 5))));
	}

	@Test
	public void streamFromOnce() {
		BiSequence<String, Integer> empty = BiSequence.once(Iterators.empty());
		assertThat(empty.stream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(empty.stream().collect(Collectors.toList()), is(emptyIterable()));

		BiSequence<String, Integer> sequence = BiSequence.once(
				Iterators.of(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4), Pair.of("5", 5)));
		assertThat(sequence.stream().collect(Collectors.toList()),
		           contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4), Pair.of("5", 5)));
		assertThat(sequence.stream().collect(Collectors.toList()), is(emptyIterable()));
	}

	@Test
	public void toChars() {
		CharSeq emptyChars = empty.toChars((l, r) -> (char) (r + 'a' - 1));
		twice(() -> assertThat(emptyChars, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyChars.iterator().nextChar());

		CharSeq charSeq = _12345.toChars((l, r) -> (char) (r + 'a' - 1));
		twice(() -> assertThat(charSeq, containsChars('a', 'b', 'c', 'd', 'e')));

		assertThat(removeFirst(charSeq), is('a'));
		twice(() -> assertThat(charSeq, containsChars('b', 'c', 'd', 'e')));
	}

	@Test
	public void toInts() {
		IntSequence emptyInts = empty.toInts((l, r) -> r + 1);
		twice(() -> assertThat(emptyInts, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyInts.iterator().nextInt());

		IntSequence intSequence = _12345.toInts((l, r) -> r + 1);
		twice(() -> assertThat(intSequence, containsInts(2, 3, 4, 5, 6)));

		assertThat(removeFirst(intSequence), is(2));
		twice(() -> assertThat(intSequence, containsInts(3, 4, 5, 6)));
	}

	@Test
	public void toLongs() {
		LongSequence emptyLongs = empty.toLongs((l, r) -> r + 1);
		twice(() -> assertThat(emptyLongs, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyLongs.iterator().nextLong());

		LongSequence longSequence = _12345.toLongs((l, r) -> r + 1);
		twice(() -> assertThat(longSequence, containsLongs(2, 3, 4, 5, 6)));

		assertThat(removeFirst(longSequence), is(2L));
		twice(() -> assertThat(longSequence, containsLongs(3, 4, 5, 6)));
	}

	@Test
	public void toDoubles() {
		DoubleSequence emptyDoubles = empty.toDoubles((l, r) -> r + 1);
		twice(() -> assertThat(emptyDoubles, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyDoubles.iterator().nextDouble());

		DoubleSequence doubleSequence = _12345.toDoubles((l, r) -> r + 1);
		twice(() -> assertThat(doubleSequence, containsDoubles(2, 3, 4, 5, 6)));

		assertThat(removeFirst(doubleSequence), is(2.0));
		twice(() -> assertThat(doubleSequence, containsDoubles(3, 4, 5, 6)));
	}

	@Test
	public void toCharsMapped() {
		CharSeq emptyChars = empty.toChars(p -> (char) (p.getRight() + 'a' - 1));
		twice(() -> assertThat(emptyChars, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyChars.iterator().nextChar());

		CharSeq charSeq = _12345.toChars(p -> (char) (p.getRight() + 'a' - 1));
		twice(() -> assertThat(charSeq, containsChars('a', 'b', 'c', 'd', 'e')));

		assertThat(removeFirst(charSeq), is('a'));
		twice(() -> assertThat(charSeq, containsChars('b', 'c', 'd', 'e')));
	}

	@Test
	public void toIntsMapped() {
		IntSequence emptyInts = empty.toInts(p -> p.getRight() + 1);
		twice(() -> assertThat(emptyInts, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyInts.iterator().nextInt());

		IntSequence intSequence = _12345.toInts(p -> p.getRight() + 1);
		twice(() -> assertThat(intSequence, containsInts(2, 3, 4, 5, 6)));

		assertThat(removeFirst(intSequence), is(2));
		twice(() -> assertThat(intSequence, containsInts(3, 4, 5, 6)));
	}

	@Test
	public void toLongsMapped() {
		LongSequence emptyLongs = empty.toLongs(p -> p.getRight() + 1);
		twice(() -> assertThat(emptyLongs, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyLongs.iterator().nextLong());

		LongSequence longSequence = _12345.toLongs(p -> p.getRight() + 1);
		twice(() -> assertThat(longSequence, containsLongs(2, 3, 4, 5, 6)));

		assertThat(removeFirst(longSequence), is(2L));
		twice(() -> assertThat(longSequence, containsLongs(3, 4, 5, 6)));
	}

	@Test
	public void toDoublesMapped() {
		DoubleSequence emptyDoubles = empty.toDoubles(p -> p.getRight() + 1);
		twice(() -> assertThat(emptyDoubles, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyDoubles.iterator().nextDouble());

		DoubleSequence doubleSequence = _12345.toDoubles(p -> p.getRight() + 1);
		twice(() -> assertThat(doubleSequence, containsDoubles(2, 3, 4, 5, 6)));

		assertThat(removeFirst(doubleSequence), is(2.0));
		twice(() -> assertThat(doubleSequence, containsDoubles(3, 4, 5, 6)));
	}

	@Test
	public void repeat() {
		BiSequence<String, Integer> emptyRepeated = empty.repeat();
		twice(() -> assertThat(emptyRepeated, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyRepeated.iterator().next());

		BiSequence<String, Integer> oneRepeated = _1.repeat();
		twice(() -> assertThat(oneRepeated.limit(3), contains(Pair.of("1", 1), Pair.of("1", 1), Pair.of("1", 1))));

		BiSequence<String, Integer> twoRepeated = _12.repeat();
		twice(() -> assertThat(twoRepeated.limit(5),
		                       contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("1", 1), Pair.of("2", 2),
		                                Pair.of("1", 1))));

		BiSequence<String, Integer> threeRepeated = _123.repeat();
		twice(() -> assertThat(threeRepeated.limit(8),
		                       contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("1", 1),
		                                Pair.of("2", 2), Pair.of("3", 3), Pair.of("1", 1), Pair.of("2", 2))));

		assertThat(removeFirst(threeRepeated), is(Pair.of("1", 1)));
		twice(() -> assertThat(threeRepeated,
		                       beginsWith(Pair.of("2", 2), Pair.of("3", 3), Pair.of("2", 2), Pair.of("3", 3),
		                                  Pair.of("2", 2), Pair.of("3", 3))));
		twice(() -> assertThat(_123, contains(Pair.of("2", 2), Pair.of("3", 3))));

		BiSequence<String, Integer> varyingLengthRepeated = BiSequence.from(new Iterable<Pair<String, Integer>>() {
			private List<Pair<String, Integer>> list = Lists.of(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3));
			int end = list.size();

			@Override
			public Iterator<Pair<String, Integer>> iterator() {
				List<Pair<String, Integer>> subList = list.subList(0, end);
				end = end > 0 ? end - 1 : 0;
				return subList.iterator();
			}
		}).repeat();
		assertThat(varyingLengthRepeated,
		           contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("1", 1), Pair.of("2", 2),
		                    Pair.of("1", 1)));
	}

	@Test
	public void repeatTwice() {
		BiSequence<String, Integer> emptyRepeatedTwice = empty.repeat(2);
		twice(() -> assertThat(emptyRepeatedTwice, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyRepeatedTwice.iterator().next());

		BiSequence<String, Integer> oneRepeatedTwice = _1.repeat(2);
		twice(() -> assertThat(oneRepeatedTwice, contains(Pair.of("1", 1), Pair.of("1", 1))));

		BiSequence<String, Integer> twoRepeatedTwice = _12.repeat(2);
		twice(() -> assertThat(twoRepeatedTwice,
		                       contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("1", 1), Pair.of("2", 2))));

		BiSequence<String, Integer> threeRepeatedTwice = _123.repeat(2);
		twice(() -> assertThat(threeRepeatedTwice,
		                       contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("1", 1),
		                                Pair.of("2", 2), Pair.of("3", 3))));

		assertThat(removeFirst(threeRepeatedTwice), is(Pair.of("1", 1)));
		twice(() -> assertThat(threeRepeatedTwice,
		                       beginsWith(Pair.of("2", 2), Pair.of("3", 3), Pair.of("2", 2), Pair.of("3", 3))));
		twice(() -> assertThat(_123, contains(Pair.of("2", 2), Pair.of("3", 3))));

		BiSequence<String, Integer> varyingLengthRepeatedTwice = BiSequence.from(
				new Iterable<Pair<String, Integer>>() {
					private List<Pair<String, Integer>> list = Lists.of(Pair.of("1", 1), Pair.of("2", 2),
					                                                    Pair.of("3", 3));
					int end = list.size();

					@Override
					public Iterator<Pair<String, Integer>> iterator() {
						List<Pair<String, Integer>> subList = list.subList(0, end);
						end = end > 0 ? end - 1 : 0;
						return subList.iterator();
					}
				}).repeat(2);
		assertThat(varyingLengthRepeatedTwice,
		           contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("1", 1), Pair.of("2", 2)));
	}

	@Test
	public void repeatZero() {
		BiSequence<String, Integer> emptyRepeatedZero = empty.repeat(0);
		twice(() -> assertThat(emptyRepeatedZero, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyRepeatedZero.iterator().next());

		BiSequence<String, Integer> oneRepeatedZero = _1.repeat(0);
		twice(() -> assertThat(oneRepeatedZero, is(emptyIterable())));

		BiSequence<String, Integer> twoRepeatedZero = _12.repeat(0);
		twice(() -> assertThat(twoRepeatedZero, is(emptyIterable())));

		BiSequence<String, Integer> threeRepeatedZero = _123.repeat(0);
		twice(() -> assertThat(threeRepeatedZero, is(emptyIterable())));
	}

	@Test
	public void generate() {
		Queue<Pair<String, Integer>> queue = new ArrayDeque<>(
				Lists.of(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3),
				         Pair.of("4", 4), Pair.of("5", 5)));
		BiSequence<String, Integer> sequence = BiSequence.generate(queue::poll);

		assertThat(sequence,
		           beginsWith(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4), Pair.of("5", 5),
		                      null));
		assertThat(sequence, beginsWith((Pair<String, Integer>) null));
	}

	@Test
	public void multiGenerate() {
		BiSequence<String, Integer> sequence = BiSequence.multiGenerate(() -> {
			Queue<Pair<String, Integer>> queue = new ArrayDeque<>(
					Lists.of(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4), Pair.of("5", 5)));
			return queue::poll;
		});

		twice(() -> assertThat(sequence, beginsWith(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4),
		                                            Pair.of("5", 5), null)));
	}

	@Test
	public void reverse() {
		BiSequence<String, Integer> emptyReversed = empty.reverse();
		twice(() -> assertThat(emptyReversed, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyReversed.iterator().next());

		BiSequence<String, Integer> oneReversed = _1.reverse();
		twice(() -> assertThat(oneReversed, contains(Pair.of("1", 1))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(oneReversed));
		twice(() -> assertThat(oneReversed, contains(Pair.of("1", 1))));

		BiSequence<String, Integer> twoReversed = _12.reverse();
		twice(() -> assertThat(twoReversed, contains(Pair.of("2", 2), Pair.of("1", 1))));

		BiSequence<String, Integer> threeReversed = _123.reverse();
		twice(() -> assertThat(threeReversed, contains(Pair.of("3", 3), Pair.of("2", 2), Pair.of("1", 1))));

		BiSequence<String, Integer> nineReversed = _123456789.reverse();
		twice(() -> assertThat(nineReversed,
		                       contains(Pair.of("9", 9), Pair.of("8", 8), Pair.of("7", 7), Pair.of("6", 6),
		                                Pair.of("5", 5), Pair.of("4", 4), Pair.of("3", 3), Pair.of("2", 2),
		                                Pair.of("1", 1))));
	}

	@Test
	public void shuffle() {
		BiSequence<String, Integer> emptyShuffled = empty.shuffle();
		twice(() -> assertThat(emptyShuffled, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyShuffled.iterator().next());

		BiSequence<String, Integer> oneShuffled = _1.shuffle();
		twice(() -> assertThat(oneShuffled, contains(Pair.of("1", 1))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(oneShuffled));
		twice(() -> assertThat(oneShuffled, contains(Pair.of("1", 1))));

		BiSequence<String, Integer> twoShuffled = _12.shuffle();
		twice(() -> assertThat(twoShuffled, containsInAnyOrder(Pair.of("1", 1), Pair.of("2", 2))));

		BiSequence<String, Integer> threeShuffled = _123.shuffle();
		twice(() -> assertThat(threeShuffled, containsInAnyOrder(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3))));

		BiSequence<String, Integer> nineShuffled = _123456789.shuffle();
		twice(() -> assertThat(nineShuffled,
		                       containsInAnyOrder(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4),
		                                          Pair.of("5", 5), Pair.of("6", 6), Pair.of("7", 7), Pair.of("8", 8),
		                                          Pair.of("9", 9))));
	}

	@Test
	public void shuffleWithRandomSource() {
		BiSequence<String, Integer> emptyShuffled = empty.shuffle(new Random(17));
		twice(() -> assertThat(emptyShuffled, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyShuffled.iterator().next());

		BiSequence<String, Integer> oneShuffled = _1.shuffle(new Random(17));
		twice(() -> assertThat(oneShuffled, contains(Pair.of("1", 1))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(oneShuffled));
		twice(() -> assertThat(oneShuffled, contains(Pair.of("1", 1))));

		BiSequence<String, Integer> twoShuffled = _12.shuffle(new Random(17));
		assertThat(twoShuffled, contains(Pair.of("1", 1), Pair.of("2", 2)));
		assertThat(twoShuffled, contains(Pair.of("1", 1), Pair.of("2", 2)));
		assertThat(twoShuffled, contains(Pair.of("1", 1), Pair.of("2", 2)));
		assertThat(twoShuffled, contains(Pair.of("1", 1), Pair.of("2", 2)));
		assertThat(twoShuffled, contains(Pair.of("2", 2), Pair.of("1", 1)));
		assertThat(twoShuffled, contains(Pair.of("2", 2), Pair.of("1", 1)));
		assertThat(twoShuffled, contains(Pair.of("1", 1), Pair.of("2", 2)));
		assertThat(twoShuffled, contains(Pair.of("1", 1), Pair.of("2", 2)));

		BiSequence<String, Integer> threeShuffled = _123.shuffle(new Random(17));
		assertThat(threeShuffled, contains(Pair.of("3", 3), Pair.of("2", 2), Pair.of("1", 1)));
		assertThat(threeShuffled, contains(Pair.of("1", 1), Pair.of("3", 3), Pair.of("2", 2)));

		BiSequence<String, Integer> nineShuffled = _123456789.shuffle(new Random(17));
		assertThat(nineShuffled,
		           contains(Pair.of("1", 1), Pair.of("8", 8), Pair.of("4", 4), Pair.of("2", 2), Pair.of("6", 6),
		                    Pair.of("3", 3), Pair.of("5", 5), Pair.of("9", 9), Pair.of("7", 7)));
		assertThat(nineShuffled,
		           contains(Pair.of("6", 6), Pair.of("3", 3), Pair.of("5", 5), Pair.of("2", 2), Pair.of("9", 9),
		                    Pair.of("4", 4), Pair.of("1", 1), Pair.of("7", 7), Pair.of("8", 8)));
	}

	@Test
	public void shuffleWithRandomSupplier() {
		BiSequence<String, Integer> emptyShuffled = empty.shuffle(() -> new Random(17));
		twice(() -> assertThat(emptyShuffled, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyShuffled.iterator().next());

		BiSequence<String, Integer> oneShuffled = _1.shuffle(() -> new Random(17));
		twice(() -> assertThat(oneShuffled, contains(Pair.of("1", 1))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(oneShuffled));
		twice(() -> assertThat(oneShuffled, contains(Pair.of("1", 1))));

		BiSequence<String, Integer> twoShuffled = _12.shuffle(() -> new Random(17));
		twice(() -> assertThat(twoShuffled, contains(Pair.of("1", 1), Pair.of("2", 2))));

		BiSequence<String, Integer> threeShuffled = _123.shuffle(() -> new Random(17));
		twice(() -> assertThat(threeShuffled, contains(Pair.of("3", 3), Pair.of("2", 2), Pair.of("1", 1))));

		BiSequence<String, Integer> nineShuffled = _123456789.shuffle(() -> new Random(17));
		twice(() -> assertThat(nineShuffled,
		                       contains(Pair.of("1", 1), Pair.of("8", 8), Pair.of("4", 4), Pair.of("2", 2),
		                                Pair.of("6", 6), Pair.of("3", 3), Pair.of("5", 5), Pair.of("9", 9),
		                                Pair.of("7", 7))));
	}

	@Test
	public void flatten() {
		Sequence<?> emptyFlattened = empty.flatten();
		twice(() -> assertThat(emptyFlattened, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyFlattened.iterator().next());

		Sequence<?> oneFlattened = _1.flatten();
		twice(() -> assertThat(oneFlattened, contains("1", 1)));

		expecting(UnsupportedOperationException.class, () -> removeFirst(oneFlattened));
		twice(() -> assertThat(oneFlattened, contains("1", 1)));

		Sequence<?> twoFlattened = _12.flatten();
		twice(() -> assertThat(twoFlattened, contains("1", 1, "2", 2)));

		Sequence<?> fiveFlattened = _12345.flatten();
		twice(() -> assertThat(fiveFlattened, contains("1", 1, "2", 2, "3", 3, "4", 4, "5", 5)));
	}

	@Test
	public void flattenFunction() {
		BiSequence<String, Integer> emptyFlattened = empty.flatten(pair -> Iterables.of(pair, Pair.of("0", 0)));
		twice(() -> assertThat(emptyFlattened, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyFlattened.iterator().next());

		BiSequence<String, Integer> threeFlattened = _123.flatten(pair -> Iterables.of(pair, Pair.of("0", 0)));
		twice(() -> assertThat(threeFlattened,
		                       contains(Pair.of("1", 1), Pair.of("0", 0), Pair.of("2", 2), Pair.of("0", 0),
		                                Pair.of("3", 3), Pair.of("0", 0))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(threeFlattened));
		twice(() -> assertThat(threeFlattened,
		                       contains(Pair.of("1", 1), Pair.of("0", 0), Pair.of("2", 2), Pair.of("0", 0),
		                                Pair.of("3", 3), Pair.of("0", 0))));
	}

	@Test
	public void flattenBiFunction() {
		BiSequence<String, Integer> emptyFlattened = empty.flatten(
				(l, r) -> Iterables.of(Pair.of(l, r), Pair.of("0", 0)));
		twice(() -> assertThat(emptyFlattened, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyFlattened.iterator().next());

		BiSequence<String, Integer> threeFlattened = _123.flatten(
				(l, r) -> Iterables.of(Pair.of(l, r), Pair.of("0", 0)));
		twice(() -> assertThat(threeFlattened,
		                       contains(Pair.of("1", 1), Pair.of("0", 0), Pair.of("2", 2), Pair.of("0", 0),
		                                Pair.of("3", 3), Pair.of("0", 0))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(threeFlattened));
		twice(() -> assertThat(threeFlattened,
		                       contains(Pair.of("1", 1), Pair.of("0", 0), Pair.of("2", 2), Pair.of("0", 0),
		                                Pair.of("3", 3), Pair.of("0", 0))));
	}

	@Test
	public void flattenLeft() {
		BiSequence<String, Integer> emptyFlattened = BiSequence.<Iterable<String>, Integer>of()
				.flattenLeft(Pair::getLeft);
		twice(() -> assertThat(emptyFlattened, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyFlattened.iterator().next());

		BiSequence<String, Integer> flattened =
				BiSequence.<Iterable<String>, Integer>ofPairs(Iterables.of("1", "2", "3"), 1, Iterables.empty(), 2,
				                                              Iterables.of("5", "6", "7"), 3)
						.flattenLeft(Pair::getLeft);
		twice(() -> assertThat(flattened, contains(Pair.of("1", 1), Pair.of("2", 1), Pair.of("3", 1), Pair.of("5", 3),
		                                           Pair.of("6", 3), Pair.of("7", 3))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(flattened));
		twice(() -> assertThat(flattened, contains(Pair.of("1", 1), Pair.of("2", 1), Pair.of("3", 1), Pair.of("5", 3),
		                                           Pair.of("6", 3), Pair.of("7", 3))));
	}

	@Test
	public void flattenRight() {
		BiSequence<String, Integer> emptyFlattened = BiSequence.<String, Iterable<Integer>>of()
				.flattenRight(Pair::getRight);
		twice(() -> assertThat(emptyFlattened, is(emptyIterable())));
		expecting(NoSuchElementException.class, () -> emptyFlattened.iterator().next());

		BiSequence<String, Integer> flattened =
				BiSequence.<String, Iterable<Integer>>ofPairs("1", Iterables.of(1, 2, 3), "2", Iterables.empty(),
				                                              "3", Iterables.of(2, 3, 4))
						.flattenRight(Pair::getRight);
		twice(() -> assertThat(flattened, contains(Pair.of("1", 1), Pair.of("1", 2), Pair.of("1", 3), Pair.of("3", 2),
		                                           Pair.of("3", 3), Pair.of("3", 4))));

		expecting(UnsupportedOperationException.class, () -> removeFirst(flattened));
		twice(() -> assertThat(flattened, contains(Pair.of("1", 1), Pair.of("1", 2), Pair.of("1", 3), Pair.of("3", 2),
		                                           Pair.of("3", 3), Pair.of("3", 4))));
	}

	@Test
	public void clear() {
		List<Pair<String, Integer>> original = Lists.create(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3),
		                                                    Pair.of("4", 4));

		BiSequence<String, Integer> filtered = BiSequence.from(original).filter((l, r) -> r % 2 != 0);
		filtered.clear();

		twice(() -> assertThat(filtered, is(emptyIterable())));
		twice(() -> assertThat(original, contains(Pair.of("2", 2), Pair.of("4", 4))));
	}

	@Test
	public void isEmpty() {
		twice(() -> assertThat(empty.isEmpty(), is(true)));
		twice(() -> assertThat(_1.isEmpty(), is(false)));
		twice(() -> assertThat(_12.isEmpty(), is(false)));
		twice(() -> assertThat(_12345.isEmpty(), is(false)));
	}

	@Test
	public void containsPair() {
		assertThat(empty.contains(Pair.of("17", 17)), is(false));

		assertThat(_12345.contains(Pair.of("1", 1)), is(true));
		assertThat(_12345.contains(Pair.of("3", 3)), is(true));
		assertThat(_12345.contains(Pair.of("5", 5)), is(true));
		assertThat(_12345.contains(Pair.of("1", 2)), is(false));
		assertThat(_12345.contains(Pair.of("2", 1)), is(false));
		assertThat(_12345.contains(Pair.of("17", 17)), is(false));
	}

	@Test
	public void containsPairComponents() {
		assertThat(empty.contains("17", 17), is(false));

		assertThat(_12345.contains("1", 1), is(true));
		assertThat(_12345.contains("3", 3), is(true));
		assertThat(_12345.contains("5", 5), is(true));
		assertThat(_12345.contains("1", 2), is(false));
		assertThat(_12345.contains("2", 1), is(false));
		assertThat(_12345.contains("17", 17), is(false));
	}

	@Test
	public void containsAll() {
		assertThat(empty.containsAll(), is(true));
		assertThat(empty.containsAll(Pair.of("17", 17), Pair.of("18", 18), Pair.of("19", 19)), is(false));

		assertThat(_12345.containsAll(), is(true));
		assertThat(_12345.containsAll(Pair.of("1", 1)), is(true));
		assertThat(_12345.containsAll(Pair.of("1", 1), Pair.of("3", 3), Pair.of("5", 5)), is(true));
		assertThat(_12345.containsAll(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4),
		                              Pair.of("5", 5)), is(true));
		assertThat(_12345.containsAll(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4),
		                              Pair.of("5", 5), Pair.of("17", 17)), is(false));
		assertThat(_12345.containsAll(Pair.of("17", 17), Pair.of("18", 18), Pair.of("19", 19)), is(false));
	}

	@Test
	public void containsAny() {
		assertThat(empty.containsAny(), is(false));
		assertThat(empty.containsAny(Pair.of("17", 17), Pair.of("18", 18), Pair.of("19", 19)), is(false));

		assertThat(_12345.containsAny(), is(false));
		assertThat(_12345.containsAny(Pair.of("1", 1)), is(true));
		assertThat(_12345.containsAny(Pair.of("1", 1), Pair.of("3", 3), Pair.of("5", 5)), is(true));
		assertThat(_12345.containsAny(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4),
		                              Pair.of("5", 5)), is(true));
		assertThat(_12345.containsAny(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4),
		                              Pair.of("5", 5), Pair.of("17", 17)), is(true));
		assertThat(_12345.containsAny(Pair.of("17", 17), Pair.of("18", 18), Pair.of("19", 19)), is(false));
	}

	@Test
	public void containsAllIterable() {
		assertThat(empty.containsAll(Iterables.of()), is(true));
		assertThat(empty.containsAll(Iterables.of(Pair.of("17", 17), Pair.of("18", 18), Pair.of("19", 19))), is
				(false));

		assertThat(_12345.containsAll(Iterables.of()), is(true));
		assertThat(_12345.containsAll(Iterables.of(Pair.of("1", 1))), is(true));
		assertThat(_12345.containsAll(Iterables.of(Pair.of("1", 1), Pair.of("3", 3), Pair.of("5", 5))), is(true));
		assertThat(_12345.containsAll(Iterables.of(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4),
		                                           Pair.of("5", 5))), is(true));
		assertThat(_12345.containsAll(Iterables.of(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4),
		                                           Pair.of("5", 5), Pair.of("17", 17))), is(false));
		assertThat(_12345.containsAll(Iterables.of(Pair.of("17", 17), Pair.of("18", 18), Pair.of("19", 19))),
		           is(false));
	}

	@Test
	public void containsAnyIterable() {
		assertThat(empty.containsAny(Iterables.of()), is(false));
		assertThat(empty.containsAny(Iterables.of(Pair.of("17", 17), Pair.of("18", 18), Pair.of("19", 19))), is
				(false));

		assertThat(_12345.containsAny(Iterables.of()), is(false));
		assertThat(_12345.containsAny(Iterables.of(Pair.of("1", 1))), is(true));
		assertThat(_12345.containsAny(Iterables.of(Pair.of("1", 1), Pair.of("3", 3), Pair.of("5", 5))), is(true));
		assertThat(_12345.containsAny(Iterables.of(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4),
		                                           Pair.of("5", 5))), is(true));
		assertThat(_12345.containsAny(Iterables.of(Pair.of("1", 1), Pair.of("2", 2), Pair.of("3", 3), Pair.of("4", 4),
		                                           Pair.of("5", 5), Pair.of("17", 17))), is(true));
		assertThat(_12345.containsAny(Iterables.of(Pair.of("17", 17), Pair.of("18", 18), Pair.of("19", 19))),
		           is(false));
	}

	@Test
	public void iteratorRemove() {
		Iterator<Pair<String, Integer>> iterator = _12345.iterator();
		iterator.next();
		iterator.remove();
		iterator.next();
		iterator.next();
		iterator.remove();

		twice(() -> assertThat(_12345, contains(Pair.of("2", 2), Pair.of("4", 4), Pair.of("5", 5))));
	}

	@Test
	public void remove() {
		assertThat(empty.remove(Pair.of("3", 3)), is(false));
		twice(() -> assertThat(empty, is(emptyIterable())));

		assertThat(_12345.remove(Pair.of("3", 3)), is(true));
		twice(() -> assertThat(_12345, contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("4", 4), Pair.of("5", 5))));

		assertThat(_12345.remove(Pair.of("7", 7)), is(false));
		twice(() -> assertThat(_12345, contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("4", 4), Pair.of("5", 5))));
	}

	@Test
	public void removeAllVarargs() {
		assertThat(empty.removeAll(Pair.of("3", 3), Pair.of("4", 4)), is(false));
		twice(() -> assertThat(empty, is(emptyIterable())));

		assertThat(_12345.removeAll(Pair.of("3", 3), Pair.of("4", 4), Pair.of("7", 7)), is(true));
		twice(() -> assertThat(_12345, contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("5", 5))));
	}

	@Test
	public void removeAllIterable() {
		assertThat(empty.removeAll(Iterables.of(Pair.of("3", 3), Pair.of("4", 4))), is(false));
		twice(() -> assertThat(empty, is(emptyIterable())));

		assertThat(_12345.removeAll(Iterables.of(Pair.of("3", 3), Pair.of("4", 4), Pair.of("7", 7))), is(true));
		twice(() -> assertThat(_12345, contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("5", 5))));
	}

	@Test
	public void removeAllCollection() {
		assertThat(empty.removeAll(Lists.of(Pair.of("3", 3), Pair.of("4", 4))), is(false));
		twice(() -> assertThat(empty, is(emptyIterable())));

		assertThat(_12345.removeAll(Lists.of(Pair.of("3", 3), Pair.of("4", 4), Pair.of("7", 7))), is(true));
		twice(() -> assertThat(_12345, contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("5", 5))));
	}

	@Test
	public void retainAllVarargs() {
		assertThat(empty.retainAll(Pair.of("3", 3), Pair.of("4", 4)), is(false));
		twice(() -> assertThat(empty, is(emptyIterable())));

		assertThat(_12345.retainAll(Pair.of("3", 3), Pair.of("4", 4), Pair.of("7", 7)), is(true));
		twice(() -> assertThat(_12345, contains(Pair.of("3", 3), Pair.of("4", 4))));
	}

	@Test
	public void retainAllIterable() {
		assertThat(empty.retainAll(Iterables.of(Pair.of("3", 3), Pair.of("4", 4))), is(false));
		twice(() -> assertThat(empty, is(emptyIterable())));

		assertThat(_12345.retainAll(Iterables.of(Pair.of("3", 3), Pair.of("4", 4), Pair.of("7", 7))), is(true));
		twice(() -> assertThat(_12345, contains(Pair.of("3", 3), Pair.of("4", 4))));
	}

	@Test
	public void retainAllCollection() {
		assertThat(empty.retainAll(Lists.of(Pair.of("3", 3), Pair.of("4", 4))), is(false));
		twice(() -> assertThat(empty, is(emptyIterable())));

		assertThat(_12345.retainAll(Lists.of(Pair.of("3", 3), Pair.of("4", 4), Pair.of("7", 7))), is(true));
		twice(() -> assertThat(_12345, contains(Pair.of("3", 3), Pair.of("4", 4))));
	}

	@Test
	public void removeIf() {
		assertThat(empty.removeIf(x -> x.equals(Pair.of("3", 3))), is(false));
		twice(() -> assertThat(empty, is(emptyIterable())));

		assertThat(_12345.removeIf(x -> x.equals(Pair.of("3", 3))), is(true));
		twice(() -> assertThat(_12345, contains(Pair.of("1", 1), Pair.of("2", 2), Pair.of("4", 4), Pair.of("5", 5))));
	}

	@Test
	public void retainIf() {
		assertThat(empty.retainIf(x -> x.equals(Pair.of("3", 3))), is(false));
		twice(() -> assertThat(empty, is(emptyIterable())));

		assertThat(_12345.retainIf(x -> x.equals(Pair.of("3", 3))), is(true));
		twice(() -> assertThat(_12345, contains(Pair.of("3", 3))));
	}

	@Test
	public void testAsList() {
		assertThat(empty.asList(), is(equalTo(Lists.of())));
		assertThat(_12345.asList(), is(equalTo(Lists.of(pairs12345))));
	}
}
