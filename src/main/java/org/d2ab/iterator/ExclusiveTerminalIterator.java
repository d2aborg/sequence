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

import javax.annotation.Nullable;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Predicate;

public class ExclusiveTerminalIterator<T> extends UnaryReferenceIterator<T> {
	private final Predicate<T> terminalPredicate;

	@Nullable
	private T next;
	private boolean hasNext;

	public ExclusiveTerminalIterator(@Nullable T terminal) {
		this(o -> Objects.equals(o, terminal));
	}

	public ExclusiveTerminalIterator(Predicate<T> terminalPredicate) {
		this.terminalPredicate = terminalPredicate;
	}

	@Override
	public boolean hasNext() {
		if (!hasNext && iterator.hasNext()) {
			next = iterator.next();
			hasNext = true;
		}
		return hasNext && !terminalPredicate.test(next);
	}

	@Override
	@Nullable
	public T next() {
		if (!hasNext())
			throw new NoSuchElementException();

		T result = next;
		hasNext = false;
		next = null;
		return result;
	}
}
