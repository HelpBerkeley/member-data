//
// Copyright (c) 2024 helpberkeley.org
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//
package org.helpberkeley.memberdata;

import org.junit.Test;

import java.util.function.LongSupplier;

import static org.assertj.core.api.Assertions.assertThat;

public class AdaptivePacerTest {

    /** A clock the test advances by hand, so easing is exercised without any waiting. */
    private static class FakeClock implements LongSupplier {
        private long millis = 1_000_000;

        @Override
        public long getAsLong() {
            return millis;
        }

        void advance(long amount) {
            millis += amount;
        }
    }

    private final FakeClock clock = new FakeClock();

    private AdaptivePacer pacer(long floorMillis) {
        return new AdaptivePacer(floorMillis, clock);
    }

    private static void observeClean(AdaptivePacer pacer, int count) {
        for (int index = 0; index < count; index++) {
            pacer.observe(false);
        }
    }

    @Test
    public void startsAtTheFloorTest() {
        AdaptivePacer pacer = pacer(500);

        assertThat(pacer.napMillis()).isEqualTo(500);
        assertThat(pacer.backoffs()).isEqualTo(0);
    }

    @Test
    public void rateLimitedDoublesTest() {
        AdaptivePacer pacer = pacer(500);

        assertThat(pacer.observe(true)).isEqualTo(1000);
        assertThat(pacer.observe(true)).isEqualTo(2000);
        assertThat(pacer.backoffs()).isEqualTo(2);
    }

    @Test
    public void tinyFloorJumpsToMinimumBackoffTest() {
        // Doubling from 1ms would take 15 rate limited requests to reach a useful wait.
        AdaptivePacer pacer = pacer(1);

        assertThat(pacer.observe(true)).isEqualTo(AdaptivePacer.MINIMUM_BACKOFF_MILLISECONDS);
    }

    @Test
    public void backoffIsCappedTest() {
        AdaptivePacer pacer = pacer(500);

        for (int index = 0; index < 20; index++) {
            pacer.observe(true);
        }

        assertThat(pacer.napMillis()).isEqualTo(AdaptivePacer.MAX_NAP_MILLISECONDS);
    }

    @Test
    public void zeroFloorDisablesPacingTest() {
        // Zero means no waiting at all was asked for. Backing off must not reintroduce it.
        AdaptivePacer pacer = pacer(0);

        assertThat(pacer.observe(true)).isEqualTo(0);
        assertThat(pacer.observe(true)).isEqualTo(0);
        assertThat(pacer.napMillis()).isEqualTo(0);
    }

    @Test
    public void negativeFloorTreatedAsZeroTest() {
        AdaptivePacer pacer = pacer(-1);

        assertThat(pacer.napMillis()).isEqualTo(0);
        assertThat(pacer.observe(true)).isEqualTo(0);
    }

    @Test
    public void easingNeedsATimeIntervalNotARequestCountTest() {
        AdaptivePacer pacer = pacer(500);
        pacer.observe(true);

        // Any number of clean requests inside the period changes nothing. This is the point of
        // the change: hundreds of clean requests can pass inside one rate limiting window.
        observeClean(pacer, 500);
        assertThat(pacer.napMillis()).isEqualTo(1000);

        clock.advance(AdaptivePacer.CLEAN_PERIOD_MILLISECONDS - 1);
        pacer.observe(false);
        assertThat(pacer.napMillis()).isEqualTo(1000);

        clock.advance(1);
        pacer.observe(false);
        assertThat(pacer.napMillis()).isEqualTo(800);
    }

    @Test
    public void cleanPeriodOutlastsDiscoursesRateLimitWindowTest() {
        // Discourse's admin limiter is a fixed one minute window. Easing sooner than that eases
        // back into the same window, which is exactly what happened on the live run.
        assertThat(AdaptivePacer.CLEAN_PERIOD_MILLISECONDS).isGreaterThan(60_000);
    }

    @Test
    public void rateLimitedRestartsTheCleanPeriodTest() {
        AdaptivePacer pacer = pacer(500);
        pacer.observe(true);

        clock.advance(AdaptivePacer.CLEAN_PERIOD_MILLISECONDS - 1);
        pacer.observe(true);
        assertThat(pacer.napMillis()).isEqualTo(2000);

        // The nearly complete clean period before the backoff must not count toward the next ease.
        clock.advance(AdaptivePacer.CLEAN_PERIOD_MILLISECONDS - 1);
        pacer.observe(false);
        assertThat(pacer.napMillis()).isEqualTo(2000);
    }

    @Test
    public void easingStopsAtTheFloorTest() {
        AdaptivePacer pacer = pacer(500);
        pacer.observe(true);

        // Far longer than it takes to ease all the way back down.
        for (int index = 0; index < 20; index++) {
            clock.advance(AdaptivePacer.CLEAN_PERIOD_MILLISECONDS);
            pacer.observe(false);
        }

        assertThat(pacer.napMillis()).isEqualTo(500);
    }

    @Test
    public void cleanRunAtTheFloorNeverMovesTest() {
        AdaptivePacer pacer = pacer(500);

        for (int index = 0; index < 5; index++) {
            clock.advance(AdaptivePacer.CLEAN_PERIOD_MILLISECONDS);
            pacer.observe(false);
        }

        assertThat(pacer.napMillis()).isEqualTo(500);
        assertThat(pacer.backoffs()).isEqualTo(0);
    }
}
