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

import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

/**
 * Chooses how long to wait between requests, from the rate limiting the site actually applies.
 *
 * The right pacing depends on the site's max_admin_api_reqs_per_minute setting, which this tool
 * cannot read. Rather than requiring that it be measured before a long run, start at the
 * configured pace and let the run find the ceiling: each rate limited request doubles the wait,
 * and a run of clean requests eases it back down.
 *
 * The configured pace is a floor. Probing below it would mean deliberately provoking rate
 * limiting, and every trip costs a server dictated wait - which is a bad trade when the run is
 * already thousands of requests long.
 *
 * Deliberately holds no clock and does no sleeping: the caller does that. This is arithmetic, so
 * it can be tested exhaustively without the tests taking any time.
 */
class AdaptivePacer {

    static final long MAX_NAP_MILLISECONDS = TimeUnit.SECONDS.toMillis(30);
    // Doubling never escapes a floor of zero, and from a floor of 1ms it would take 15 rate
    // limited requests to reach a useful wait. Back off to at least this on the first one.
    static final long MINIMUM_BACKOFF_MILLISECONDS = 250;
    // How long the requests have to stay clean before easing back toward the floor.
    //
    // Deliberately a duration and not a request count. Discourse's admin limiter is a fixed one
    // minute window, and easing after a count - it was 50 requests - takes under a minute at any
    // useful pace, so the pace eased straight back into the same window and got limited again.
    // Longer than the window means a clean stretch actually demonstrates headroom.
    static final long CLEAN_PERIOD_MILLISECONDS = TimeUnit.SECONDS.toMillis(90);
    private static final double EASE_FACTOR = 0.8;

    private final long floorMillis;
    private final LongSupplier clock;
    private long napMillis;
    private long lastChangeMillis;
    private int backoffs = 0;

    /**
     * @param floorMillis the starting pace, and the fastest this will ever pace. Zero disables
     *                    pacing entirely - the operator, or a test, asked for no waiting at all,
     *                    and backing off must not reintroduce it against that instruction.
     */
    AdaptivePacer(long floorMillis) {
        this(floorMillis, System::currentTimeMillis);
    }

    /** Test support - lets the easing be exercised without any waiting. */
    AdaptivePacer(long floorMillis, final LongSupplier clock) {
        this.floorMillis = Math.max(0, floorMillis);
        this.napMillis = this.floorMillis;
        this.clock = clock;
        this.lastChangeMillis = clock.getAsLong();
    }

    /** How long to wait before the next request. */
    long napMillis() {
        return napMillis;
    }

    /** How many times the pace has been backed off, for the end of run summary. */
    int backoffs() {
        return backoffs;
    }

    /**
     * Feed in whether the request just made was rate limited.
     *
     * @return the new pace
     */
    long observe(boolean rateLimited) {

        if (floorMillis == 0) {
            return 0;
        }

        long now = clock.getAsLong();

        if (rateLimited) {
            backoffs++;
            lastChangeMillis = now;
            napMillis = Math.min(MAX_NAP_MILLISECONDS,
                    Math.max(MINIMUM_BACKOFF_MILLISECONDS, napMillis * 2));
        } else if ((napMillis > floorMillis)
                && ((now - lastChangeMillis) >= CLEAN_PERIOD_MILLISECONDS)) {
            lastChangeMillis = now;
            napMillis = Math.max(floorMillis, (long) (napMillis * EASE_FACTOR));
        }

        return napMillis;
    }
}
