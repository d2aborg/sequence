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

package org.d2ab.primitive.ints;

import java.util.function.IntUnaryOperator;

public class RecursiveIntIterator implements IntIterator {
	private final int seed;
	private final IntUnaryOperator op;
	private int previous;
	private boolean hasPrevious;

	public RecursiveIntIterator(int seed, IntUnaryOperator op) {
		this.seed = seed;
		this.op = op;
	}

	@Override
	public boolean hasNext() {
		return true;
	}

	@Override
	public int nextInt() {
		previous = hasPrevious ? op.applyAsInt(previous) : seed;
		hasPrevious = true;
		return previous;
	}
}