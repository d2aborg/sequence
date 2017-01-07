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

import org.d2ab.collection.Collectionz;
import org.d2ab.collection.chars.CharList;
import org.d2ab.iterator.chars.CharIterator;
import org.d2ab.iterator.ints.*;
import org.d2ab.util.Strict;

import java.util.*;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * A primitive specialization of {@link List} for {@code int} values.
 */
public interface IntList extends List<Integer>, IntCollection {
	// TODO: Extract out relevant parts to IterableIntList
	// TODO: Enable Strict checking

	/**
	 * Returns an immutable {@code IntList} of the given elements. The returned {@code IntList}'s
	 * {@link IntListIterator} supports forward iteration only.
	 */
	static IntList of(int... xs) {
		return new Base() {
			@Override
			public IntIterator iterator() {
				return new ArrayIntIterator(xs);
			}

			@Override
			public int size() {
				return xs.length;
			}
		};
	}

	/**
	 * @return a new empty mutable {@code IntList}.
	 *
	 * @since 2.1
	 */
	static IntList create() {
		return ArrayIntList.create();
	}

	/**
	 * @return a new mutable {@code IntList} with a copy of the given elements.
	 *
	 * @since 2.1
	 */
	static IntList create(int... xs) {
		return ArrayIntList.create(xs);
	}

	/**
	 * @return an {@code IntList} initialized with the members of the given {@link PrimitiveIterator.OfInt}.
	 */
	static IntList copy(PrimitiveIterator.OfInt iterator) {
		IntList copy = create();
		while (iterator.hasNext())
			copy.addInt(iterator.nextInt());
		return copy;
	}

	default void clear() {
		iterator().removeAll();
	}

	@Override
	default boolean isEmpty() {
		return size() == 0;
	}

	@Override
	default IntList asList() {
		return this;
	}

	@Override
	default boolean contains(Object o) {
		assert Strict.LENIENT : "IntList.contains(Object)";

		return o instanceof Integer && containsInt((int) o);
	}

	@Override
	default Integer[] toArray() {
		assert Strict.LENIENT : "IntList.toArray()";

		return toArray(new Integer[size()]);
	}

	@Override
	default <T> T[] toArray(T[] a) {
		assert Strict.LENIENT : "IntList.toArray(Object[])";

		return Collectionz.toArray(this, a);
	}

	@Override
	default boolean remove(Object o) {
		assert Strict.LENIENT : "IntList.remove(Object)";

		return o instanceof Integer && removeInt((int) o);
	}

	@Override
	default boolean add(Integer x) {
		assert Strict.LENIENT : "IntList.add(Integer)";

		return addInt(x);
	}

	@Override
	default boolean addAll(int index, Collection<? extends Integer> c) {
		if (c instanceof IntCollection)
			return addAllIntsAt(index, (IntCollection) c);

		assert Strict.LENIENT : "IntList.addAll(int, Collection)";

		if (c.isEmpty())
			return false;

		IntListIterator listIterator = listIterator(index);
		for (int t : c)
			listIterator.add(t);

		return true;
	}

	default boolean addAllIntsAt(int index, int... xs) {
		if (xs.length == 0)
			return false;

		IntListIterator listIterator = listIterator(index);
		for (int x : xs)
			listIterator.add(x);

		return true;
	}

	default boolean addAllIntsAt(int index, IntCollection xs) {
		IntListIterator listIterator = listIterator(index);

		boolean modified = false;
		for (IntIterator iterator = xs.iterator(); iterator.hasNext(); ) {
			listIterator.add(iterator.nextInt());
			modified = true;
		}
		return modified;
	}

	@Override
	default void replaceAll(UnaryOperator<Integer> operator) {
		assert Strict.LENIENT : "IntList.replaceAll(UnaryOperator)";

		replaceAllInts(operator::apply);
	}

	default void replaceAllInts(IntUnaryOperator operator) {
		IntListIterator listIterator = listIterator();
		while (listIterator.hasNext())
			listIterator.set(operator.applyAsInt(listIterator.nextInt()));
	}

	default void sortInts() {
		throw new UnsupportedOperationException();
	}

	@Override
	default void sort(Comparator<? super Integer> c) {
		assert Strict.LENIENT : "IntList.sort(Comparator)";

		throw new UnsupportedOperationException();
	}

	default int binarySearch(int x) {
		throw new UnsupportedOperationException();
	}

	@Override
	default IntList subList(int from, int to) {
		return new SubList(this, from, to);
	}

	@Override
	default boolean addAll(Collection<? extends Integer> c) {
		if (c instanceof IntCollection)
			return addAllInts((IntCollection) c);

		assert Strict.LENIENT : "IntList.addAll(Collection)";

		return IntCollections.addAll(this, c);
	}

	@Override
	default boolean addInt(int x) {
		listIterator(size()).add(x);
		return true;
	}

	@Override
	default boolean addAllInts(int... xs) {
		if (xs.length == 0)
			return false;

		IntListIterator listIterator = listIterator(size());
		for (int x : xs)
			listIterator.add(x);

		return true;
	}

	@Override
	default boolean addAllInts(IntCollection xs) {
		IntListIterator listIterator = listIterator(size());

		boolean modified = false;
		for (IntIterator iterator = xs.iterator(); iterator.hasNext(); ) {
			listIterator.add(iterator.nextInt());
			modified = true;
		}
		return modified;
	}

	@Override
	default boolean containsAll(Collection<?> c) {
		if (c instanceof IntIterable)
			return containsAllInts((IntIterable) c);

		assert Strict.LENIENT : "IntList.containsAll(Collection)";

		return IntCollections.containsAll(this, c);
	}

	@Override
	default boolean removeAll(Collection<?> c) {
		if (c instanceof IntIterable)
			return removeAllInts((IntIterable) c);

		assert Strict.LENIENT : "IntList.removeAll(Collection)";

		boolean modified = false;
		for (IntIterator iterator = iterator(); iterator.hasNext(); ) {
			if (c.contains(iterator.nextInt())) {
				iterator.remove();
				modified = true;
			}
		}
		return modified;
	}

	@Override
	default boolean removeIf(Predicate<? super Integer> filter) {
		assert Strict.LENIENT : "IntList.removeIf(Predicate)";

		return removeIntsIf(filter::test);
	}

	@Override
	default boolean retainAll(Collection<?> c) {
		if (c instanceof IntIterable)
			return retainAllInts((IntIterable) c);

		assert Strict.LENIENT : "IntList.retainAll(Collection)";

		boolean modified = false;
		for (IntIterator iterator = iterator(); iterator.hasNext(); ) {
			if (!c.contains(iterator.nextInt())) {
				iterator.remove();
				modified = true;
			}
		}
		return modified;
	}

	@Override
	default Integer get(int index) {
		assert Strict.LENIENT : "IntList.get(int)";

		return getInt(index);
	}

	default int getInt(int index) {
		IntListIterator iterator = listIterator(index);
		if (!iterator.hasNext())
			throw new IndexOutOfBoundsException(String.valueOf(index));

		return iterator.nextInt();
	}

	@Override
	default Integer set(int index, Integer x) {
		assert Strict.LENIENT : "IntList.set(int, Integer)";

		return setInt(index, x);
	}

	default int setInt(int index, int x) {
		IntListIterator listIterator = listIterator(index);
		if (!listIterator.hasNext())
			throw new IndexOutOfBoundsException(String.valueOf(index));
		int previous = listIterator.nextInt();
		listIterator.set(x);
		return previous;
	}

	@Override
	default void add(int index, Integer x) {
		assert Strict.LENIENT : "IntList.add(int, Integer)";

		addIntAt(index, x);
	}

	default void addIntAt(int index, int x) {
		listIterator(index).add(x);
	}

	@Override
	default Integer remove(int index) {
		assert Strict.LENIENT : "IntList.remove(int)";

		return removeIntAt(index);
	}

	default int removeIntAt(int index) {
		IntListIterator listIterator = listIterator(index);
		int previous = listIterator.nextInt();
		listIterator.remove();
		return previous;
	}

	@Override
	default int lastIndexOf(Object o) {
		assert Strict.LENIENT : "IntList.lastIndexOf(Object)";

		return o instanceof Integer ? lastIndexOfInt((int) o) : -1;
	}

	default int lastIndexOfInt(int x) {
		int lastIndex = -1;

		int index = 0;
		for (IntIterator iterator = iterator(); iterator.hasNext(); index++)
			if (iterator.nextInt() == x)
				lastIndex = index;

		return lastIndex;
	}

	@Override
	default int indexOf(Object o) {
		assert Strict.LENIENT : "IntList.indexOf(Object)";

		return o instanceof Integer ? indexOfInt((int) o) : -1;
	}

	default int indexOfInt(int x) {
		int index = 0;
		for (IntIterator iterator = iterator(); iterator.hasNext(); index++)
			if (iterator.nextInt() == x)
				return index;

		return -1;
	}

	@Override
	default IntListIterator listIterator() {
		return listIterator(0);
	}

	@Override
	default IntListIterator listIterator(int index) {
		return IntListIterator.forwardOnly(iterator(), index);
	}

	@Override
	default Spliterator.OfInt spliterator() {
		return Spliterators.spliterator(iterator(), size(), Spliterator.ORDERED | Spliterator.NONNULL);
	}

	@Override
	default CharList asChars() {
		return new CharList() {
			@Override
			public CharIterator iterator() {
				return CharIterator.from(IntList.this.iterator());
			}

			@Override
			public int size() {
				return IntList.this.size();
			}
		};
	}

	/**
	 * Base class for {@link IntList} implementations.
	 */
	abstract class Base extends IntCollection.Base implements IntList {
		public static IntList create(int... ints) {
			return from(IntList.create(ints));
		}

		public static IntList from(final IntCollection collection) {
			return new IntList.Base() {
				@Override
				public IntIterator iterator() {
					return collection.iterator();
				}

				@Override
				public int size() {
					return collection.size();
				}

				@Override
				public boolean addInt(int x) {
					return collection.addInt(x);
				}
			};
		}

		public static IntList from(final IntList list) {
			return new IntList.Base() {
				@Override
				public IntIterator iterator() {
					return list.iterator();
				}

				@Override
				public IntListIterator listIterator(int index) {
					return list.listIterator(index);
				}

				@Override
				public int size() {
					return list.size();
				}

				@Override
				public boolean addInt(int x) {
					return list.addInt(x);
				}
			};
		}

		public boolean equals(Object o) {
			if (o == this)
				return true;
			if (!(o instanceof List))
				return false;

			IntListIterator it = listIterator();
			if (o instanceof IntList) {
				IntListIterator that = ((IntList) o).listIterator();
				while (it.hasNext()) {
					if (!that.hasNext())
						return false;
					if (it.nextInt() != that.nextInt())
						return false;
				}
				return !that.hasNext();
			} else {
				ListIterator<?> that = ((List<?>) o).listIterator();
				while (it.hasNext()) {
					if (!that.hasNext())
						return false;
					int x = it.nextInt();
					Object y = that.next();
					if (!(y instanceof Integer && x == (Integer) y))
						return false;
				}
				return !that.hasNext();
			}
		}

		public int hashCode() {
			int hashCode = 1;
			for (IntIterator iterator = iterator(); iterator.hasNext(); )
				hashCode = 31 * hashCode + Integer.hashCode(iterator.nextInt());
			return hashCode;
		}
	}

	class SubList implements IntList {
		private final IntList list;

		protected int from;
		protected int to;

		public SubList(IntList list, int from, int to) {
			if (from < 0)
				throw new ArrayIndexOutOfBoundsException(from);
			if (to > list.size())
				throw new ArrayIndexOutOfBoundsException(to);
			this.list = list;
			this.from = from;
			this.to = to;
		}

		public IntIterator iterator() {
			return new DelegatingUnaryIntIterator(
					new LimitingIntIterator(new SkippingIntIterator(list.iterator(), from), to - from)) {
				@Override
				public int nextInt() {
					return iterator.nextInt();
				}

				@Override
				public void remove() {
					super.remove();
					to--;
				}
			};
		}

		public int size() {
			return to - from;
		}
	}
}
