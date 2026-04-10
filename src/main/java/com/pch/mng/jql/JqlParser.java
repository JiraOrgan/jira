package com.pch.mng.jql;

import com.pch.mng.jql.ast.JqlExpression;
import com.pch.mng.jql.ast.JqlField;
import com.pch.mng.jql.ast.JqlOperator;
import com.pch.mng.jql.ast.JqlOrderBy;
import com.pch.mng.jql.ast.JqlOrderField;
import com.pch.mng.jql.ast.JqlQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * PCH JQL MVP 파서 (SPIKE-JQL-PARSER.md Phase 4 범위).
 * AND &gt; OR 우선순위, 괄호, IS EMPTY, =, !=, IN, ~, ORDER BY.
 */
public final class JqlParser {

    private JqlParser() {}

    public static JqlQuery parse(String input) {
        if (input == null || input.isBlank()) {
            throw new JqlParseException("JQL이 비어 있습니다", 0);
        }
        Lexer lex = new Lexer(input.strip());
        if (lex.peek().kind == Kind.EOF) {
            throw new JqlParseException("JQL이 비어 있습니다", 0);
        }
        if (isKeyword(lex.peek(), "order")) {
            consumeOrderBy(lex);
            List<JqlOrderBy> order = parseOrderList(lex);
            lex.expectEof();
            return new JqlQuery(null, order);
        }
        JqlExpression where = parseOr(lex);
        List<JqlOrderBy> order = List.of();
        if (lex.peek().kind != Kind.EOF && isKeyword(lex.peek(), "order")) {
            consumeOrderBy(lex);
            order = parseOrderList(lex);
        }
        lex.expectEof();
        return new JqlQuery(where, order);
    }

    private static JqlExpression parseOr(Lexer lex) {
        JqlExpression left = parseAnd(lex);
        while (isKeyword(lex.peek(), "or")) {
            lex.next();
            left = new JqlExpression.Or(left, parseAnd(lex));
        }
        return left;
    }

    private static JqlExpression parseAnd(Lexer lex) {
        JqlExpression left = parsePrimary(lex);
        while (isKeyword(lex.peek(), "and")) {
            lex.next();
            left = new JqlExpression.And(left, parsePrimary(lex));
        }
        return left;
    }

    private static JqlExpression parsePrimary(Lexer lex) {
        if (lex.peek().kind == Kind.LPAREN) {
            lex.next();
            JqlExpression inner = parseOr(lex);
            if (lex.peek().kind != Kind.RPAREN) {
                throw new JqlParseException("')' 가 필요합니다", lex.peek().pos);
            }
            lex.next();
            return inner;
        }
        return parseClause(lex);
    }

    private static JqlExpression.Clause parseClause(Lexer lex) {
        Token fieldTok = lex.next();
        if (fieldTok.kind != Kind.IDENT) {
            throw new JqlParseException("필드 이름이 필요합니다", fieldTok.pos);
        }
        JqlField field = JqlField.fromLexeme(fieldTok.text, fieldTok.pos);

        if (isKeyword(lex.peek(), "is")) {
            lex.next();
            if (!isKeyword(lex.peek(), "empty")) {
                throw new JqlParseException("'EMPTY' 가 필요합니다", lex.peek().pos);
            }
            lex.next();
            return new JqlExpression.Clause(field, JqlOperator.IS_EMPTY, List.of());
        }

        JqlOperator op = parseOperator(lex);
        if (op == JqlOperator.IN) {
            if (lex.peek().kind != Kind.LPAREN) {
                throw new JqlParseException("'IN' 뒤에 '(' 가 필요합니다", lex.peek().pos);
            }
            lex.next();
            List<String> vals = new ArrayList<>();
            if (lex.peek().kind == Kind.RPAREN) {
                throw new JqlParseException("IN 목록에 값이 최소 1개 필요합니다", lex.peek().pos);
            }
            vals.add(parseAtomicValue(lex));
            while (lex.peek().kind == Kind.COMMA) {
                lex.next();
                vals.add(parseAtomicValue(lex));
            }
            Token close = lex.next();
            if (close.kind != Kind.RPAREN) {
                throw new JqlParseException("')' 가 필요합니다", close.pos);
            }
            return new JqlExpression.Clause(field, JqlOperator.IN, vals);
        }

        String atom = parseAtomicValue(lex);
        return new JqlExpression.Clause(field, op, List.of(atom));
    }

    private static JqlOperator parseOperator(Lexer lex) {
        Token t = lex.peek();
        if (t.kind == Kind.EQ) {
            lex.next();
            return JqlOperator.EQ;
        }
        if (t.kind == Kind.NE) {
            lex.next();
            return JqlOperator.NE;
        }
        if (t.kind == Kind.TILDE) {
            lex.next();
            return JqlOperator.CONTAINS;
        }
        if (isKeyword(t, "in")) {
            lex.next();
            return JqlOperator.IN;
        }
        throw new JqlParseException("연산자(=, !=, ~, IN)가 필요합니다", t.pos);
    }

    private static String parseAtomicValue(Lexer lex) {
        Token t = lex.next();
        return switch (t.kind) {
            case STRING -> unquote(t.text, t.pos);
            case NUMBER -> t.text;
            case IDENT -> t.text;
            default -> throw new JqlParseException("값(문자열·식별자·숫자)이 필요합니다", t.pos);
        };
    }

    private static void consumeOrderBy(Lexer lex) {
        Token o = lex.next();
        if (!isKeyword(o, "order")) {
            throw new JqlParseException("'ORDER' 가 필요합니다", o.pos);
        }
        Token b = lex.next();
        if (!isKeyword(b, "by")) {
            throw new JqlParseException("'BY' 가 필요합니다", b.pos);
        }
    }

    private static List<JqlOrderBy> parseOrderList(Lexer lex) {
        List<JqlOrderBy> list = new ArrayList<>();
        list.add(parseOneOrder(lex));
        while (lex.peek().kind == Kind.COMMA) {
            lex.next();
            list.add(parseOneOrder(lex));
        }
        return list;
    }

    private static JqlOrderBy parseOneOrder(Lexer lex) {
        Token t = lex.next();
        if (t.kind != Kind.IDENT) {
            throw new JqlParseException("ORDER BY 필드명이 필요합니다", t.pos);
        }
        JqlOrderField f = JqlOrderField.fromLexeme(t.text, t.pos);
        boolean asc = true;
        if (isKeyword(lex.peek(), "asc")) {
            lex.next();
            asc = true;
        } else if (isKeyword(lex.peek(), "desc")) {
            lex.next();
            asc = false;
        }
        return new JqlOrderBy(f, asc);
    }

    private static boolean isKeyword(Token t, String kw) {
        return t.kind == Kind.IDENT && t.text.equalsIgnoreCase(kw);
    }

    private static String unquote(String raw, int pos) {
        if (raw.length() < 2 || raw.charAt(0) != '"' || raw.charAt(raw.length() - 1) != '"') {
            throw new JqlParseException("잘못된 문자열 리터럴", pos);
        }
        String inner = raw.substring(1, raw.length() - 1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < inner.length(); i++) {
            char c = inner.charAt(i);
            if (c == '\\' && i + 1 < inner.length()) {
                char n = inner.charAt(i + 1);
                if (n == '"' || n == '\\') {
                    sb.append(n);
                    i++;
                    continue;
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }

    private enum Kind {
        EOF,
        IDENT,
        STRING,
        NUMBER,
        LPAREN,
        RPAREN,
        COMMA,
        EQ,
        NE,
        TILDE
    }

    private record Token(Kind kind, String text, int pos) {}

    private static final class Lexer {

        private final String src;
        private final int len;
        private int pos;
        private Token buffer;

        Lexer(String src) {
            this.src = src;
            this.len = src.length();
        }

        Token peek() {
            if (buffer == null) {
                buffer = read();
            }
            return buffer;
        }

        Token next() {
            if (buffer != null) {
                Token t = buffer;
                buffer = null;
                return t;
            }
            return read();
        }

        void expectEof() {
            Token t = peek();
            if (t.kind != Kind.EOF) {
                throw new JqlParseException("불필요한 토큰: " + t.text, t.pos);
            }
        }

        private void skipWs() {
            while (pos < len && Character.isWhitespace(src.charAt(pos))) {
                pos++;
            }
        }

        private Token read() {
            skipWs();
            if (pos >= len) {
                return new Token(Kind.EOF, "", pos);
            }
            int start = pos;
            char c = src.charAt(pos);
            if (c == '(') {
                pos++;
                return new Token(Kind.LPAREN, "(", start);
            }
            if (c == ')') {
                pos++;
                return new Token(Kind.RPAREN, ")", start);
            }
            if (c == ',') {
                pos++;
                return new Token(Kind.COMMA, ",", start);
            }
            if (c == '=') {
                pos++;
                return new Token(Kind.EQ, "=", start);
            }
            if (c == '!') {
                if (pos + 1 < len && src.charAt(pos + 1) == '=') {
                    pos += 2;
                    return new Token(Kind.NE, "!=", start);
                }
                throw new JqlParseException("'!' 뒤에는 '=' 가 와야 합니다", start);
            }
            if (c == '~') {
                pos++;
                return new Token(Kind.TILDE, "~", start);
            }
            if (c == '"') {
                return readString(start);
            }
            if (Character.isDigit(c)) {
                return readNumber(start);
            }
            if (Character.isLetter(c) || c == '_') {
                return readIdent(start);
            }
            throw new JqlParseException("예상치 못한 문자: '" + c + "'", start);
        }

        private Token readIdent(int start) {
            int p = pos;
            while (p < len) {
                char ch = src.charAt(p);
                if (Character.isLetterOrDigit(ch) || ch == '_') {
                    p++;
                } else {
                    break;
                }
            }
            pos = p;
            return new Token(Kind.IDENT, src.substring(start, p), start);
        }

        private Token readNumber(int start) {
            int p = pos;
            while (p < len && Character.isDigit(src.charAt(p))) {
                p++;
            }
            pos = p;
            return new Token(Kind.NUMBER, src.substring(start, p), start);
        }

        private Token readString(int start) {
            int p = pos + 1;
            while (p < len) {
                char ch = src.charAt(p);
                if (ch == '\\' && p + 1 < len) {
                    p += 2;
                    continue;
                }
                if (ch == '"') {
                    String raw = src.substring(start, p + 1);
                    pos = p + 1;
                    return new Token(Kind.STRING, raw, start);
                }
                p++;
            }
            throw new JqlParseException("닫히지 않은 문자열", start);
        }
    }
}
