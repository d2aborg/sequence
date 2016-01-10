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
package org.d2ab.primitive.chars;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * An {@link Iterator} that interleaves the streams of two other {@link Iterator}s with each other.
 */
public class InterleavingCharIterator implements CharIterator {
	@Nonnull
	private final List<CharIterator> iterators = new ArrayList<>();
	private int current;

	public InterleavingCharIterator(@Nonnull CharIterable... iterables) {
		for (CharIterable iterable : iterables) {
			CharIterator iterator = Objects.requireNonNull(iterable).iterator();
			iterators.add(Objects.requireNonNull(iterator));
		}
	}

	@Override
	public char nextChar() {
		if (!hasNext())
			throw new NoSuchElementException();

		while (!iterators.get(current).hasNext())
			advance();

		CharIterator iterator = iterators.get(current);
		advance();
		return iterator.nextChar();
	}

	@Override
	public boolean hasNext() {
		for (Iterator iterator : iterators)
			if (iterator.hasNext())
				return true;
		return false;
	}

	private void advance() {
		current = (current + 1) % iterators.size();
	}
}