package com.pch.mng.jql;

import com.pch.mng.issue.QIssue;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class JqlQueryFactorySmokeTest {

    @Autowired
    JPAQueryFactory jpaQueryFactory;

    @Test
    void countIssues() {
        QIssue issue = QIssue.issue;
        jpaQueryFactory.select(issue.id.count()).from(issue).fetchOne();
    }
}
