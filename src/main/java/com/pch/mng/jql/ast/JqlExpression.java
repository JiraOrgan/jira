package com.pch.mng.jql.ast;

import java.util.List;

public sealed interface JqlExpression permits JqlExpression.And, JqlExpression.Or, JqlExpression.Clause {

    record And(JqlExpression left, JqlExpression right) implements JqlExpression {}

    record Or(JqlExpression left, JqlExpression right) implements JqlExpression {}

    record Clause(JqlField field, JqlOperator operator, List<String> values) implements JqlExpression {
        public Clause {
            values = List.copyOf(values);
        }
    }
}
