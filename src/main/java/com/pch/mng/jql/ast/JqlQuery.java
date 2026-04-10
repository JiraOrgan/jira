package com.pch.mng.jql.ast;

import java.util.List;

public record JqlQuery(JqlExpression where, List<JqlOrderBy> orderBy) {

    public JqlQuery {
        orderBy = List.copyOf(orderBy);
    }

    /** WHERE 없이 ORDER BY 만 있는 경우 */
    public boolean matchesAll() {
        return where == null;
    }
}
