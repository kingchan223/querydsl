package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;

import java.util.List;

import static study.querydsl.entity.QMember.member;

@Transactional
@SpringBootTest
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;
    JPAQueryFactory queryFactory;
    @BeforeEach
    public void before(){
        queryFactory = new JPAQueryFactory(em);

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 10, teamA);
        Member member3 = new Member("member3", 10, teamB);
        Member member4 = new Member("member4", 10, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    void startJPQL() {
        //member1을 찾아라.
        Member findMember =
                em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", "member1")
                .getSingleResult();
        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    void startQuerydsl() {
        QMember m = new QMember("m");
        Member findMember = queryFactory
                                .select(m)
                                .from(m)
                                .where(m.username.eq("member1"))
                                .fetchOne();
        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    void qEntityTest() {
        /*1번 방법*/ QMember m = new QMember("m");
        /*2번 방법*/ m = QMember.member;//더 짧은 방법
        /*3번 방법*/ m = member; //static import. 제일 깔끔한 방법

        Member findMember = queryFactory
                .select(m)
                .from(m)
                .where(m.username.eq("member1"))
                .fetchOne();
    }

    @Test
    void search1() {
        Member findMember = queryFactory
                .selectFrom(member)//요렇게 select + from 합치기 가능
                .where(member.username.eq("member1")
                        .and(member.age.eq(10)))//and 또는 or로 체인걸기
                .fetchOne();

        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    //아래와 같이도 가능하다.
    @Test
    void searchAndParam() {
        Member findMember = queryFactory
                .selectFrom(member)
                .where(
                        member.username.eq("member1"), member.age.eq(10)
                )
                .fetchOne();

        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    void resultFetch1() {
        //다수 조회
        List<Member> fetch = queryFactory.selectFrom(member)
                                         .fetch();
    }

    @Test
    void resultFetch2() {
        //단건 조회
        Member fetchOne = queryFactory.selectFrom(member).where(member.username.eq("member1"))
                .fetchOne();
    }

    @Test
    void resultFetch3() {
        //처음 한 건 조회
        Member fetchFirst = queryFactory.selectFrom(QMember.member)
                .fetchFirst();
    }
}
