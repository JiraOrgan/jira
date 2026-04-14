package com.pch.mng.jql;

import com.pch.mng.jql.ast.JqlExpression;
import com.pch.mng.jql.ast.JqlField;
import com.pch.mng.jql.ast.JqlOperator;
import com.pch.mng.jql.ast.JqlOrderField;
import com.pch.mng.jql.ast.JqlQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JqlParserTest {

    @Test
    @DisplayName("단순 등식")
    void simpleEq() {
        JqlQuery q = JqlParser.parse("status = BACKLOG");
        assertThat(q.matchesAll()).isFalse();
        JqlExpression.Clause c = (JqlExpression.Clause) q.where();
        assertThat(c.field()).isEqualTo(JqlField.STATUS);
        assertThat(c.operator()).isEqualTo(JqlOperator.EQ);
        assertThat(c.values()).containsExactly("BACKLOG");
    }

    @Test
    @DisplayName("문자열·!=·괄호·AND/OR 우선순위")
    void compound() {
        JqlQuery q = JqlParser.parse("(type = TASK OR type = BUG) AND project != \"X\"");
        assertThat(q.where()).isInstanceOf(JqlExpression.And.class);
        JqlExpression.And and = (JqlExpression.And) q.where();
        assertThat(and.left()).isInstanceOf(JqlExpression.Or.class);
        assertThat(and.right()).isInstanceOf(JqlExpression.Clause.class);
        JqlExpression.Clause ne = (JqlExpression.Clause) and.right();
        assertThat(ne.operator()).isEqualTo(JqlOperator.NE);
        assertThat(ne.values()).containsExactly("X");
    }

    @Test
    @DisplayName("IS EMPTY")
    void isEmpty() {
        JqlQuery q = JqlParser.parse("assignee IS EMPTY");
        JqlExpression.Clause c = (JqlExpression.Clause) q.where();
        assertThat(c.operator()).isEqualTo(JqlOperator.IS_EMPTY);
        assertThat(c.values()).isEmpty();
    }

    @Test
    @DisplayName("IN 목록")
    void inList() {
        JqlQuery q = JqlParser.parse("priority IN (HIGH, MEDIUM)");
        JqlExpression.Clause c = (JqlExpression.Clause) q.where();
        assertThat(c.operator()).isEqualTo(JqlOperator.IN);
        assertThat(c.values()).containsExactly("HIGH", "MEDIUM");
    }

    @Test
    @DisplayName("archived 불리언")
    void archivedEq() {
        JqlQuery q = JqlParser.parse("archived = true");
        JqlExpression.Clause c = (JqlExpression.Clause) q.where();
        assertThat(c.field()).isEqualTo(JqlField.ARCHIVED);
        assertThat(c.operator()).isEqualTo(JqlOperator.EQ);
        assertThat(c.values()).containsExactly("true");
    }

    @Test
    @DisplayName("text contains ~")
    void textContains() {
        JqlQuery q = JqlParser.parse("text ~ \"login\"");
        JqlExpression.Clause c = (JqlExpression.Clause) q.where();
        assertThat(c.field()).isEqualTo(JqlField.TEXT);
        assertThat(c.operator()).isEqualTo(JqlOperator.CONTAINS);
        assertThat(c.values()).containsExactly("login");
    }

    @Test
    @DisplayName("스프린트 id 숫자")
    void sprintNumber() {
        JqlQuery q = JqlParser.parse("sprint = 42");
        JqlExpression.Clause c = (JqlExpression.Clause) q.where();
        assertThat(c.values()).containsExactly("42");
    }

    @Test
    @DisplayName("ORDER BY 복합")
    void orderBy() {
        JqlQuery q = JqlParser.parse("status = DONE ORDER BY priority DESC, key ASC");
        assertThat(q.orderBy()).hasSize(2);
        assertThat(q.orderBy().get(0).field()).isEqualTo(JqlOrderField.PRIORITY);
        assertThat(q.orderBy().get(0).ascending()).isFalse();
        assertThat(q.orderBy().get(1).field()).isEqualTo(JqlOrderField.KEY);
        assertThat(q.orderBy().get(1).ascending()).isTrue();
    }

    @Test
    @DisplayName("WHERE 없이 ORDER BY 만")
    void orderOnly() {
        JqlQuery q = JqlParser.parse("ORDER BY created DESC");
        assertThat(q.matchesAll()).isTrue();
        assertThat(q.orderBy()).hasSize(1);
        assertThat(q.orderBy().get(0).field()).isEqualTo(JqlOrderField.CREATED);
    }

    @Test
    @DisplayName("문자열 이스케이프")
    void stringEscape() {
        JqlQuery q = JqlParser.parse("text ~ \"a\\\"b\"");
        JqlExpression.Clause c = (JqlExpression.Clause) q.where();
        assertThat(c.values().get(0)).isEqualTo("a\"b");
    }

    @Test
    @DisplayName("빈 입력")
    void emptyFails() {
        assertThatThrownBy(() -> JqlParser.parse("   "))
                .isInstanceOf(JqlParseException.class);
    }

    @Test
    @DisplayName("잘못된 필드")
    void badField() {
        assertThatThrownBy(() -> JqlParser.parse("foo = 1"))
                .isInstanceOf(JqlParseException.class);
    }

    @Test
    @DisplayName("IN 빈 목록")
    void emptyInFails() {
        assertThatThrownBy(() -> JqlParser.parse("status IN ()"))
                .isInstanceOf(JqlParseException.class);
    }
}
