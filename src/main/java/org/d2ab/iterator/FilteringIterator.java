/*
 * Copyright 2015 Daniel Skogquist Åborg
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
package org.d2ab.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class FilteringIterator<T> implements Iterator<T> {
	private final Iterator<? extends T> iterator;
	private final Predicate<? super T> predicate;
	T foundValue;
	private boolean foundNext;

	public FilteringIterator(Iterator<? extends T> iterator, Predicate<? super T> predicate) {
		this.iterator = iterator;
		this.predicate = predicate;
	}

	@Override
	public boolean hasNext() {
		if (foundNext) { // already checked
			return true;
		}

		do { // find next matching, bail out if EOF
			foundNext = iterator.hasNext();
			if (!foundNext)
				return false;
		} while (!predicate.test(foundValue = iterator.next()));

		// found matching value
		return true;
	}

	@Override
	public T next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		T nextValue = foundValue;
		foundNext = false;
		foundValue = null;
		return nextValue;
	}
}