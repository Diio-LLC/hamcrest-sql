package com.diio.query.matcher;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;

import java.math.BigDecimal;

import org.junit.Test;

public class NumberMatcherTest {

    @Test
    public void matchTwoIntegralNumbersStrictly() {
        assertThat(new Integer(5), NumberMatcher.strictIntegral(5));
        assertThat(Byte.valueOf((byte)5), NumberMatcher.strictIntegral(5L));
        assertThat(new Long(5), NumberMatcher.strictIntegral(5));
    }
    
    @Test
    public void dontMatchTwoIntegralUnequalNumbers() {
        assertThat(Byte.valueOf((byte)5), not(NumberMatcher.strictIntegral(3L)));        
    }
    
    @Test
    public void matchTwoFloatingPointNumbersStrictly() {
        assertThat(3.3f, NumberMatcher.strictAndClose(3.3f, 0.01));
        assertThat(3.3f, NumberMatcher.strictAndClose(3.3, 0.01));
        assertThat(3.3f, NumberMatcher.strictAndClose(new BigDecimal(3.3), 0.001));
    }

    @Test
    public void matchTwoFloatingPointNumbersWithSufficientlyLargeError() {
        assertThat(3.335f, NumberMatcher.strictAndClose(3.334f, 0.01));        
    }    
    
    @Test
    public void dontMatchTwoFloatingPointNumbersWithTooSmallError() {
        assertThat(3.335f, not(NumberMatcher.strictAndClose(3.334f, 0.00001)));        
    }
    
    @Test
    public void dontMatchAnIntegralNumberWithAFloatingPointNumberInStrictModeEvenWithLargeError() {
        assertThat(3, not(NumberMatcher.strictAndClose(3.334f, 1)));        
        assertThat(3.0, not(NumberMatcher.strictIntegral(3)));        
    }
    
}
