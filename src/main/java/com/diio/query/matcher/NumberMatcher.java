/*
   Copyright (c) 2022 Cirium

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.diio.query.matcher;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.number.BigDecimalCloseTo;

/**
 * Matches two numbers of possibly different types. Matching can either be in "loose" or "strict" mode. In strict mode,
 * a floating-point Number (e.g. Double, Float, BigDecimal) can never match an integral Number (Byte, Integer, BigInteger, etc). In
 * loose mode, a floating-point Number can match an integral Number if the floating point number is within 0.0001 of the integral Number.
 * 
 * @author kkoster
 *
 */
public class NumberMatcher<T extends Number> extends TypeSafeMatcher<T> {
    private final T number;
    private final boolean strict;
    private final Double error;
    
    public NumberMatcher(boolean strict, T value) {
        this(strict, value, null);
    }

    public NumberMatcher(boolean strict, T value, Double error) {
        if (value == null) {
            throw new NullPointerException(getClass().getName() + " can never match null");
        }
        if (!strict) {
            throw new UnsupportedOperationException("Loose mode is still TODO");
        }
        this.number = value;
        this.strict = strict;
        this.error = error; 
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(isIntegral(number) ? "an integral" : "a floating point");
        description.appendText(" number of value " + number);
    }

    @Override
    protected boolean matchesSafely(T item) {
        if (isIntegral(number) && isIntegral(item)) {
            //a bit sloppy to always create a BigInteger per match attempt.
            //TODO: do direct comparison if the same type. if different types,
            //check if the wider one's value would overflow the narrower type; if not, then check equality
            return convertToBigInteger(number).equals(convertToBigInteger(item));
        } else if (isIntegral(number)) {
            if (strict) {
                return false;
            }
            //TODO loose float/integral comparison
        } else if (isIntegral(item)) {
            if (strict) {
                return false;
            }
            //TODO loose float/integral comparison            
        } else {
            return new BigDecimalCloseTo(convertToBigDecimal(number), convertToBigDecimal(error)).matches(convertToBigDecimal(item));
        }
        return number.equals(item);
    }

    private static <T extends Number> boolean isIntegral(T t) {
        return t instanceof Integer ||
                t instanceof Long ||
                t instanceof Byte ||
                t instanceof BigInteger ||
                t instanceof Short ||
                t instanceof AtomicLong ||
                t instanceof AtomicInteger;
    }
    
    private BigInteger convertToBigInteger(T t) {
        if (t instanceof BigInteger) {
            return (BigInteger) t;
        }
        return BigInteger.valueOf(t.longValue());
    }
    
    private BigDecimal convertToBigDecimal(Number t) {
        if (t instanceof BigDecimal) {
            return (BigDecimal) t;
        }
        return BigDecimal.valueOf(t.doubleValue());
    }
    
    public static <T extends Number> Matcher<Number> strictIntegral(T i) {
        if (!isIntegral(i)) {
            throw new IllegalArgumentException("Strict floating-point " + NumberMatcher.class.getName() + " should use strictAndClose()");
        }
        return new NumberMatcher<Number>(true, i);
    }
    
    public static <T extends Number> Matcher<Number> strictAndClose(T i, double error) {
        return new NumberMatcher<Number>(true, i, error);
    }
    
    public static Matcher<Number> strict(String containsNumericValue) {
        if (containsNumericValue.contains(".")) {
            return new NumberMatcher<Number>(true, Double.parseDouble(containsNumericValue));
        } else {
            return new NumberMatcher<Number>(true, Long.parseLong(containsNumericValue));
        }
    }
}
