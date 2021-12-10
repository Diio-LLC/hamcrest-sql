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

import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsAnything;

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.CastNode;
import com.akiban.sql.parser.QueryTreeNode;
import com.akiban.sql.types.TypeId;

/**
 * Matches SQL casts, including the ones you might think of as literals, not casts, such as TIME '11:34:00'.
 * 
 * @author kkoster
 *
 */
public class CastMatcher extends QueryTreeNodeMatcher {
    private final Matcher<QueryTreeNode> operandMatcher;

    private final Matcher<TypeId> typeMatcher;

    public CastMatcher(Matcher<TypeId> destTypeMatcher, Matcher<QueryTreeNode> operandMatcher) {
        this.operandMatcher = operandMatcher;
        this.typeMatcher = destTypeMatcher;
    }
    
    @Override
    public void describeTo(Description description) {
        description.appendText("CAST of ").
            appendDescriptionOf(operandMatcher).
            appendText(" to type ").
            appendDescriptionOf(typeMatcher);
    }
    
    @Override
    protected boolean matchesSafely(QueryTreeNode item) {
        if (item instanceof CastNode) {
            CastNode node = (CastNode) item;
            try {
                return operandMatcher.matches(node.getCastOperand()) &&
                        typeMatcher.matches(node.getTypeId());
            } catch (StandardException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    /**
     * Syntactic sugar for the case where you are merely trying to match a casted value.
     *
     * For example, assertThat(query, hasInQuery(castedValue(literal("11:23:45"))));
     */
    @Factory
    public static Matcher<QueryTreeNode> castedValue(Matcher<QueryTreeNode> subMatcher) {
        return new CastMatcher(new IsAnything<TypeId>(), subMatcher);
    }
    
    /**
     * Syntactic sugar for the case where you matching a casted value to a particular type.
     *
     * For example, assertThat(query, hasInQuery(cast(TypeId.TIME_ID, literal("11:23:45"))));
     */
    @Factory
    public static Matcher<QueryTreeNode> cast(TypeId destType, Matcher<QueryTreeNode> subMatcher) {
        return new CastMatcher(CoreMatchers.equalTo(destType), subMatcher);
    }
}
