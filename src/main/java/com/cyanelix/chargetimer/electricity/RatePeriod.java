package com.cyanelix.chargetimer.electricity;

import java.time.Clock;

public interface RatePeriod {
    RatePeriod NULL_RATE_PERIOD = new RatePeriod() {
        @Override
        public boolean chargeNow(Clock clock) {
            return false;
        }

        @Override
        public long getSeconds() {
            return 0;
        }

        @Override
        public String toString() {
            return "NULL_RATE_PERIOD";
        }
    };

    boolean chargeNow(Clock clock);

    long getSeconds();
}
