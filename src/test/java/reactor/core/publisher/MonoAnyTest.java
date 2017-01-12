/*
 * Copyright (c) 2011-2016 Pivotal Software Inc, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package reactor.core.publisher;

import org.junit.Assert;
import org.junit.Test;
import reactor.test.StepVerifier;
import reactor.test.subscriber.AssertSubscriber;

public class MonoAnyTest {

	@Test(expected = NullPointerException.class)
	public void sourceNull() {
		new MonoAny<>(null, v -> true);
	}

	@Test(expected = NullPointerException.class)
	public void predicateNull() {
		new MonoAny<>(null, null);
	}

	@Test(expected = NullPointerException.class)
	public void elementNull() {
		Flux.never().hasElement(null);
	}

	@Test
	public void normal() {
		AssertSubscriber<Boolean> ts = AssertSubscriber.create();

		Flux.range(1, 10).any(v -> true).subscribe(ts);

		ts.assertValues(true)
		  .assertComplete()
		  .assertNoError();
	}

	@Test
	public void normal2() {
		StepVerifier.create(Flux.range(1, 10).hasElement(4))
		            .expectNext(true)
		            .verifyComplete();
	}
	@Test
	public void error2() {
		StepVerifier.create(Flux.range(1, 10).hasElement(-4))
		            .expectNext(false)
		            .verifyComplete();
	}

	@Test
	public void normalBackpressured() {
		AssertSubscriber<Boolean> ts = AssertSubscriber.create(0);

		Flux.range(1, 10).any(v -> true).subscribe(ts);

		ts.assertNoValues()
		  .assertNotComplete()
		  .assertNoError();

		ts.request(1);

		ts.assertValues(true)
		  .assertComplete()
		  .assertNoError();
	}

	@Test
	public void none() {
		AssertSubscriber<Boolean> ts = AssertSubscriber.create();

		Flux.range(1, 10).any(v -> false).subscribe(ts);

		ts.assertValues(false)
		  .assertComplete()
		  .assertNoError();
	}

	@Test
	public void noneBackpressured() {
		AssertSubscriber<Boolean> ts = AssertSubscriber.create(0);

		Flux.range(1, 10).any(v -> false).subscribe(ts);

		ts.assertNoValues()
		  .assertNotComplete()
		  .assertNoError();

		ts.request(1);

		ts.assertValues(false)
		  .assertComplete()
		  .assertNoError();
	}

	@Test
	public void someMatch() {
		AssertSubscriber<Boolean> ts = AssertSubscriber.create();

		Flux.range(1, 10).any(v -> v < 6).subscribe(ts);

		ts.assertValues(true)
		  .assertComplete()
		  .assertNoError();
	}

	@Test
	public void someMatchBackpressured() {
		AssertSubscriber<Boolean> ts = AssertSubscriber.create(0);

		Flux.range(1, 10).any(v -> v < 6).subscribe(ts);

		ts.assertNoValues()
		  .assertNotComplete()
		  .assertNoError();

		ts.request(1);

		ts.assertValues(true)
		  .assertComplete()
		  .assertNoError();
	}

	@Test
	public void predicateThrows() {
		AssertSubscriber<Boolean> ts = AssertSubscriber.create();

		Flux.range(1, 10).any(v -> {
			throw new RuntimeException("forced failure");
		}).subscribe(ts);

		ts.assertNoValues()
		  .assertNotComplete()
		  .assertError(RuntimeException.class)
		  .assertErrorWith( e -> {
			  e.printStackTrace();
			  Assert.assertTrue(e.getMessage().contains("forced failure"));
		  });
	}

}
