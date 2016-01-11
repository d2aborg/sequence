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

import java.util.NoSuchElementException;

public class LimitingIterator<T> extends UnaryReferenceIterator<T> {
	private final long limit;

	long count;

	public LimitingIterator(long limit) {
		this.limit = limit;
	}

	@Override
	public T next() {
		if (!hasNext())
			throw new NoSuchElementException();
		T next = iterator.next();
		count++;
		return next;
	}

	@Override
	public boolean hasNext() {
		return count < limit && iterator.hasNext();
	}
}
