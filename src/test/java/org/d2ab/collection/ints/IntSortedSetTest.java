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

package org.d2ab.collection.ints;

import org.d2ab.collection.Arrayz;
import org.d2ab.collection.chars.CharSet;
import org.d2ab.iterator.ints.IntIterator;
import org.junit.Test;

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.d2ab.test.IsCharIterableContainingInOrder.containsChars;
import static org.d2ab.test.IsIntIterableContainingInOrder.containsInts;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class IntSortedSetTest {
	private final IntSortedSet empty = IntSortedSet.Base.create();
	private final IntSortedSet set = IntSortedSet.Base.create(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4);

	@Test
	public void create() {
		assertThat(IntSortedSet.create(), is(emptyIterable()));
		assertThat(IntSortedSet.create(-2, -1, 0, 1), containsInts(-2, -1, 0, 1));
	}

	@Test
	public void size() {
		assertThat(empty.size(), is(0));
		assertThat(set.size(), is(10));
	}

	@Test
	public void iterator() {
		assertThat(empty, is(emptyIterable()));
		assertThat(set, containsInts(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4));
	}

	@Test
	public void iteratorFailFast() {
		IntIterator it1 = set.iterator();
		set.addInt(17);
		expecting(ConcurrentModificationException.class, it1::nextInt);

		IntIterator it2 = set.iterator();
		set.removeInt(17);
		expecting(ConcurrentModificationException.class, it2::nextInt);
	}

	@Test
	public void isEmpty() {
		assertThat(empty.isEmpty(), is(true));
		assertThat(set.isEmpty(), is(false));
	}

	@Test
	public void clear() {
		empty.clear();
		assertThat(empty.isEmpty(), is(true));

		set.clear();
		assertThat(set.isEmpty(), is(true));
	}

	@Test
	public void addInt() {
		empty.addInt(17);
		assertThat(empty, containsInts(17));

		set.addInt(17);
		assertThat(set, containsInts(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 17));
	}

	@Test
	public void containsInt() {
		assertThat(empty.containsInt(17), is(false));

		assertThat(set.containsInt(17), is(false));
		for (int x = -5; x <= 4; x++)
			assertThat(set.containsInt(x), is(true));
	}

	@Test
	public void removeInt() {
		assertThat(empty.removeInt(17), is(false));

		assertThat(set.removeInt(17), is(false));
		for (int x = -5; x <= 4; x++)
			assertThat(set.removeInt(x), is(true));
		assertThat(set.isEmpty(), is(true));
	}

	@Test
	public void testToString() {
		assertThat(empty.toString(), is("[]"));
		assertThat(set.toString(), is("[-5, -4, -3, -2, -1, 0, 1, 2, 3, 4]"));
	}

	@Test
	public void testEqualsHashCodeAgainstIntSet() {
		IntSet set2 = IntSortedSet.Base.create(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 17);
		assertThat(set, is(not(equalTo(set2))));
		assertThat(set.hashCode(), is(not(set2.hashCode())));

		set2.removeInt(17);

		assertThat(set, is(equalTo(set2)));
		assertThat(set.hashCode(), is(set2.hashCode()));
	}

	@Test
	public void subSet() {
		IntSortedSet subSet = set.subSet(-3, 3);
		assertThat(subSet, containsInts(-3, -2, -1, 0, 1, 2));
		assertThat(subSet.size(), is(6));
		assertThat(subSet.firstInt(), is(-3));
		assertThat(subSet.lastInt(), is(2));
		assertThat(subSet.containsInt(1), is(true));
		assertThat(subSet.containsInt(3), is(false));
		assertThat(subSet.toString(), is("[-3, -2, -1, 0, 1, 2]"));

		IntSet equivalentSet = IntSet.create(-3, -2, -1, 0, 1, 2);
		assertThat(subSet, is(equalTo(equivalentSet)));
		assertThat(subSet.hashCode(), is(equivalentSet.hashCode()));

		assertThat(subSet.removeInt(0), is(true));
		assertThat(subSet, containsInts(-3, -2, -1, 1, 2));
		assertThat(subSet.size(), is(5));
		assertThat(set, containsInts(-5, -4, -3, -2, -1, 1, 2, 3, 4));

		assertThat(subSet.removeInt(0), is(false));
		assertThat(subSet, containsInts(-3, -2, -1, 1, 2));
		assertThat(subSet.size(), is(5));
		assertThat(set, containsInts(-5, -4, -3, -2, -1, 1, 2, 3, 4));

		assertThat(subSet.addInt(0), is(true));
		assertThat(subSet, containsInts(-3, -2, -1, 0, 1, 2));
		assertThat(subSet.size(), is(6));
		assertThat(set, containsInts(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4));

		assertThat(subSet.addInt(0), is(false));
		assertThat(subSet, containsInts(-3, -2, -1, 0, 1, 2));
		assertThat(subSet.size(), is(6));
		assertThat(set, containsInts(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4));

		expecting(IllegalArgumentException.class, () -> subSet.addInt(-17));
		assertThat(subSet, containsInts(-3, -2, -1, 0, 1, 2));
		assertThat(subSet.size(), is(6));
		assertThat(set, containsInts(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4));

		subSet.clear();
		assertThat(subSet, is(emptyIterable()));
		assertThat(subSet.size(), is(0));
		assertThat(set, containsInts(-5, -4, 3, 4));
	}

	@Test
	public void sparseSubSet() {
		IntSortedSet subSet = IntSortedSet.Base.create(-5, -3, -1, 1, 3, 5).subSet(-2, 2);
		assertThat(subSet, containsInts(-1, 1));
		assertThat(subSet.size(), is(2));
		assertThat(subSet.firstInt(), is(-1));
		assertThat(subSet.lastInt(), is(1));
		assertThat(subSet.containsInt(-3), is(false));
		assertThat(subSet.containsInt(-2), is(false));
		assertThat(subSet.containsInt(0), is(false));
		assertThat(subSet.containsInt(1), is(true));
		assertThat(subSet.containsInt(2), is(false));
		assertThat(subSet.toString(), is("[-1, 1]"));

		IntSet equivalentSet = IntSet.create(-1, 1);
		assertThat(subSet, is(equalTo(equivalentSet)));
		assertThat(subSet.hashCode(), is(equivalentSet.hashCode()));

		assertThat(subSet.removeInt(3), is(false));
		assertThat(subSet.removeInt(2), is(false));
		assertThat(subSet.removeInt(1), is(true));
		assertThat(subSet, containsInts(-1));
		assertThat(subSet.size(), is(1));
		assertThat(subSet.firstInt(), is(-1));
		assertThat(subSet.lastInt(), is(-1));
		assertThat(subSet.containsInt(-3), is(false));
		assertThat(subSet.containsInt(-2), is(false));
		assertThat(subSet.containsInt(0), is(false));
		assertThat(subSet.containsInt(1), is(false));
		assertThat(subSet.containsInt(2), is(false));
		assertThat(subSet.toString(), is("[-1]"));

		assertThat(subSet.addInt(-2), is(true));
		assertThat(subSet, containsInts(-2, -1));
		assertThat(subSet.size(), is(2));
		assertThat(subSet.firstInt(), is(-2));
		assertThat(subSet.lastInt(), is(-1));
		assertThat(subSet.containsInt(-3), is(false));
		assertThat(subSet.containsInt(-2), is(true));
		assertThat(subSet.containsInt(0), is(false));
		assertThat(subSet.containsInt(1), is(false));
		assertThat(subSet.containsInt(2), is(false));
		assertThat(subSet.toString(), is("[-2, -1]"));
	}

	@Test
	public void headSet() {
		IntSortedSet headSet = set.headSet(0);
		assertThat(headSet, containsInts(-5, -4, -3, -2, -1));
		assertThat(headSet.size(), is(5));
		assertThat(headSet.firstInt(), is(-5));
		assertThat(headSet.lastInt(), is(-1));
		assertThat(headSet.containsInt(-3), is(true));
		assertThat(headSet.containsInt(0), is(false));
		assertThat(headSet.toString(), is("[-5, -4, -3, -2, -1]"));

		IntSet equivalentSet = IntSet.create(-5, -4, -3, -2, -1);
		assertThat(headSet, is(equalTo(equivalentSet)));
		assertThat(headSet.hashCode(), is(equivalentSet.hashCode()));

		assertThat(headSet.removeInt(-3), is(true));
		assertThat(headSet, containsInts(-5, -4, -2, -1));
		assertThat(headSet.size(), is(4));
		assertThat(set, containsInts(-5, -4, -2, -1, 0, 1, 2, 3, 4));

		assertThat(headSet.removeInt(-3), is(false));
		assertThat(headSet, containsInts(-5, -4, -2, -1));
		assertThat(headSet.size(), is(4));
		assertThat(set, containsInts(-5, -4, -2, -1, 0, 1, 2, 3, 4));

		assertThat(headSet.addInt(-17), is(true));
		assertThat(headSet, containsInts(-17, -5, -4, -2, -1));
		assertThat(headSet.size(), is(5));
		assertThat(set, containsInts(-17, -5, -4, -2, -1, 0, 1, 2, 3, 4));

		assertThat(headSet.addInt(-17), is(false));
		assertThat(headSet, containsInts(-17, -5, -4, -2, -1));
		assertThat(headSet.size(), is(5));
		assertThat(set, containsInts(-17, -5, -4, -2, -1, 0, 1, 2, 3, 4));

		expecting(IllegalArgumentException.class, () -> headSet.addInt(17));
		assertThat(headSet, containsInts(-17, -5, -4, -2, -1));
		assertThat(headSet.size(), is(5));
		assertThat(set, containsInts(-17, -5, -4, -2, -1, 0, 1, 2, 3, 4));

		assertThat(set.addInt(-6), is(true));
		assertThat(headSet, containsInts(-17, -6, -5, -4, -2, -1));
		assertThat(headSet.size(), is(6));
		assertThat(set, containsInts(-17, -6, -5, -4, -2, -1, 0, 1, 2, 3, 4));

		headSet.clear();
		assertThat(headSet, is(emptyIterable()));
		assertThat(headSet.size(), is(0));
		assertThat(set, containsInts(0, 1, 2, 3, 4));
	}

	@Test
	public void sparseHeadSet() {
		IntSortedSet set = IntSortedSet.Base.create(-5, -3, -1, 1, 3, 5);
		IntSortedSet headSet = set.headSet(0);
		assertThat(headSet, containsInts(-5, -3, -1));
		assertThat(headSet.size(), is(3));
		assertThat(headSet.firstInt(), is(-5));
		assertThat(headSet.lastInt(), is(-1));
		assertThat(headSet.containsInt(-3), is(true));
		assertThat(headSet.containsInt(1), is(false));
		assertThat(headSet.toString(), is("[-5, -3, -1]"));

		IntSet equivalentSet = IntSet.create(-5, -3, -1);
		assertThat(headSet, is(equalTo(equivalentSet)));
		assertThat(headSet.hashCode(), is(equivalentSet.hashCode()));
	}

	@Test
	public void tailSet() {
		IntSortedSet tailSet = set.tailSet(0);
		assertThat(tailSet, containsInts(0, 1, 2, 3, 4));
		assertThat(tailSet.size(), is(5));
		assertThat(tailSet.firstInt(), is(0));
		assertThat(tailSet.lastInt(), is(4));
		assertThat(tailSet.containsInt(3), is(true));
		assertThat(tailSet.containsInt(-1), is(false));
		assertThat(tailSet.toString(), is("[0, 1, 2, 3, 4]"));

		IntSet equivalentSet = IntSet.create(0, 1, 2, 3, 4);
		assertThat(tailSet, is(equalTo(equivalentSet)));
		assertThat(tailSet.hashCode(), is(equivalentSet.hashCode()));

		assertThat(tailSet.removeInt(2), is(true));
		assertThat(tailSet, containsInts(0, 1, 3, 4));
		assertThat(tailSet.size(), is(4));
		assertThat(set, containsInts(-5, -4, -3, -2, -1, 0, 1, 3, 4));

		assertThat(tailSet.removeInt(2), is(false));
		assertThat(tailSet, containsInts(0, 1, 3, 4));
		assertThat(tailSet.size(), is(4));
		assertThat(set, containsInts(-5, -4, -3, -2, -1, 0, 1, 3, 4));

		assertThat(tailSet.addInt(17), is(true));
		assertThat(tailSet, containsInts(0, 1, 3, 4, 17));
		assertThat(tailSet.size(), is(5));
		assertThat(set, containsInts(-5, -4, -3, -2, -1, 0, 1, 3, 4, 17));

		assertThat(tailSet.addInt(17), is(false));
		assertThat(tailSet, containsInts(0, 1, 3, 4, 17));
		assertThat(tailSet.size(), is(5));
		assertThat(set, containsInts(-5, -4, -3, -2, -1, 0, 1, 3, 4, 17));

		expecting(IllegalArgumentException.class, () -> tailSet.addInt(-17));
		assertThat(tailSet, containsInts(0, 1, 3, 4, 17));
		assertThat(tailSet.size(), is(5));
		assertThat(set, containsInts(-5, -4, -3, -2, -1, 0, 1, 3, 4, 17));

		assertThat(set.addInt(5), is(true));
		assertThat(tailSet, containsInts(0, 1, 3, 4, 5, 17));
		assertThat(tailSet.size(), is(6));
		assertThat(set, containsInts(-5, -4, -3, -2, -1, 0, 1, 3, 4, 5, 17));

		tailSet.clear();
		assertThat(tailSet, is(emptyIterable()));
		assertThat(tailSet.size(), is(0));
		assertThat(set, containsInts(-5, -4, -3, -2, -1));
	}

	@Test
	public void sparseTailSet() {
		IntSortedSet set = IntSortedSet.Base.create(-5, -3, -1, 1, 3, 5);
		IntSortedSet tailSet = set.tailSet(0);
		assertThat(tailSet, containsInts(1, 3, 5));
		assertThat(tailSet.size(), is(3));
		assertThat(tailSet.firstInt(), is(1));
		assertThat(tailSet.lastInt(), is(5));
		assertThat(tailSet.containsInt(3), is(true));
		assertThat(tailSet.containsInt(-1), is(false));
		assertThat(tailSet.toString(), is("[1, 3, 5]"));

		IntSet equivalentSet = IntSet.create(1, 3, 5);
		assertThat(tailSet, is(equalTo(equivalentSet)));
		assertThat(tailSet.hashCode(), is(equivalentSet.hashCode()));
	}

	@Test
	public void addAllIntArray() {
		assertThat(empty.addAllInts(1, 2, 3), is(true));
		assertThat(empty, containsInts(1, 2, 3));

		assertThat(set.addAllInts(3, 4, 5, 6, 7), is(true));
		assertThat(set, containsInts(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7));
	}

	@Test
	public void addAllIntCollection() {
		assertThat(empty.addAllInts(IntList.create(1, 2, 3)), is(true));
		assertThat(empty, containsInts(1, 2, 3));

		assertThat(set.addAllInts(IntList.create(3, 4, 5, 6, 7)), is(true));
		assertThat(set, containsInts(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7));
	}

	@Test
	public void stream() {
		assertThat(empty.stream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(set.stream().collect(Collectors.toList()), contains(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4));
	}

	@Test
	public void parallelStream() {
		assertThat(empty.parallelStream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(set.parallelStream().collect(Collectors.toList()), contains(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4));
	}

	@Test
	public void intStream() {
		assertThat(empty.intStream().collect(IntList::create, IntList::addInt, IntList::addAllInts),
		           is(emptyIterable()));

		assertThat(set.intStream().collect(IntList::create, IntList::addInt, IntList::addAllInts),
		           containsInts(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4));
	}

	@Test
	public void parallelIntStream() {
		assertThat(empty.parallelIntStream().collect(IntList::create, IntList::addInt, IntList::addAllInts),
		           is(emptyIterable()));

		assertThat(set.parallelIntStream().collect(IntList::create, IntList::addInt, IntList::addAllInts),
		           containsInts(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4));
	}

	@Test
	public void sequence() {
		assertThat(empty.sequence(), is(emptyIterable()));
		assertThat(set.sequence(), containsInts(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4));
	}

	@Test
	public void firstInt() {
		expecting(NoSuchElementException.class, empty::firstInt);
		assertThat(set.firstInt(), is(-5));
	}

	@Test
	public void lastInt() {
		expecting(NoSuchElementException.class, empty::lastInt);
		assertThat(set.lastInt(), is(4));
	}

	@Test
	public void iteratorRemoveAll() {
		IntIterator iterator = set.iterator();
		int value = -5;
		while (iterator.hasNext()) {
			assertThat(iterator.nextInt(), is(value));
			iterator.remove();
			value++;
		}
		assertThat(value, is(5));
		assertThat(set, is(emptyIterable()));
	}

	@Test
	public void removeAllIntArray() {
		assertThat(empty.removeAllInts(1, 2, 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.removeAllInts(1, 2, 3), is(true));
		assertThat(set, containsInts(-5, -4, -3, -2, -1, 0, 4));
	}

	@Test
	public void removeAllIntCollection() {
		assertThat(empty.removeAll(IntList.create(1, 2, 3)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.removeAll(IntList.create(1, 2, 3)), is(true));
		assertThat(set, containsInts(-5, -4, -3, -2, -1, 0, 4));
	}

	@Test
	public void retainAllIntArray() {
		assertThat(empty.retainAllInts(1, 2, 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.retainAllInts(1, 2, 3), is(true));
		assertThat(set, containsInts(1, 2, 3));
	}

	@Test
	public void retainAllIntCollection() {
		assertThat(empty.retainAll(IntList.create(1, 2, 3)), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.retainAll(IntList.create(1, 2, 3)), is(true));
		assertThat(set, containsInts(1, 2, 3));
	}

	@Test
	public void removeIntsIf() {
		assertThat(empty.removeIntsIf(x -> x > 3), is(false));
		assertThat(empty, is(emptyIterable()));

		assertThat(set.removeIntsIf(x -> x > 3), is(true));
		assertThat(set, containsInts(-5, -4, -3, -2, -1, 0, 1, 2, 3));
	}

	@Test
	public void containsAllIntArray() {
		assertThat(empty.containsAllInts(1, 2, 3), is(false));
		assertThat(set.containsAllInts(1, 2, 3), is(true));
		assertThat(set.containsAllInts(1, 2, 3, 17), is(false));
	}

	@Test
	public void containsAllIntCollection() {
		assertThat(empty.containsAll(IntList.create(1, 2, 3)), is(false));
		assertThat(set.containsAll(IntList.create(1, 2, 3)), is(true));
		assertThat(set.containsAll(IntList.create(1, 2, 3, 17)), is(false));
	}

	@Test
	public void forEachInt() {
		empty.forEachInt(x -> {
			throw new IllegalStateException("should not get called");
		});

		AtomicInteger value = new AtomicInteger(-5);
		set.forEachInt(x -> assertThat(x, is(value.getAndIncrement())));
		assertThat(value.get(), is(5));
	}

	@Test
	public void asChars() {
		CharSet emptyAsChars = empty.asChars();
		twice(() -> assertThat(emptyAsChars, is(emptyIterable())));
		assertThat(emptyAsChars.size(), is(0));

		CharSet intSetAsChars = IntSortedSet.Base.create('a', 'b', 'c', 'd', 'e').asChars();
		twice(() -> assertThat(intSetAsChars, containsChars('a', 'b', 'c', 'd', 'e')));
		assertThat(intSetAsChars.size(), is(5));
	}

	@Test
	public void boundaries() {
		assertThat(empty.addInt(Integer.MIN_VALUE), is(true));
		assertThat(empty.addInt(0), is(true));
		assertThat(empty.addInt(Integer.MAX_VALUE), is(true));

		assertThat(empty, containsInts(Integer.MIN_VALUE, 0, Integer.MAX_VALUE));

		assertThat(empty.containsInt(Integer.MIN_VALUE), is(true));
		assertThat(empty.containsInt(0), is(true));
		assertThat(empty.containsInt(Integer.MAX_VALUE), is(true));

		assertThat(empty.removeInt(Integer.MIN_VALUE), is(true));
		assertThat(empty.removeInt(0), is(true));
		assertThat(empty.removeInt(Integer.MAX_VALUE), is(true));

		assertThat(empty, is(emptyIterable()));
	}

	@Test
	public void fuzz() {
		int[] randomValues = new int[1000];
		Random random = new Random();
		for (int i = 0; i < randomValues.length; i++) {
			int randomValue;
			do
				randomValue = random.nextInt();
			while (Arrayz.contains(randomValues, randomValue));
			randomValues[i] = randomValue;
		}

		// Adding
		for (int randomValue : randomValues)
			assertThat(empty.addInt(randomValue), is(true));
		assertThat(empty.size(), is(randomValues.length));

		for (int randomValue : randomValues)
			assertThat(empty.addInt(randomValue), is(false));
		assertThat(empty.size(), is(randomValues.length));

		// Containment checks
		assertThat(empty.containsAllInts(randomValues), is(true));

		for (int randomValue : randomValues)
			assertThat(empty.containsInt(randomValue), is(true));

		// toString
		Arrays.sort(randomValues);
		StringBuilder expectedToString = new StringBuilder("[");
		for (int i = 0; i < randomValues.length; i++)
			expectedToString.append(i > 0 ? ", " : "").append(randomValues[i]);
		expectedToString.append("]");
		assertThat(empty.toString(), is(expectedToString.toString()));

		// Removing
		for (int randomValue : randomValues)
			assertThat(empty.removeInt(randomValue), is(true));
		assertThat(empty.toString(), is("[]"));
		assertThat(empty.size(), is(0));

		for (int randomValue : randomValues)
			assertThat(empty.removeInt(randomValue), is(false));
	}
}
