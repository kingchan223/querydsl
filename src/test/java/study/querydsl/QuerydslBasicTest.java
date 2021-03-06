package study.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.QMemberDto;
import study.querydsl.dto.UserDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

@Transactional
@SpringBootTest
public class QuerydslBasicTest {

    @Autowired EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before(){
        queryFactory = new JPAQueryFactory(em);

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 10, teamB);
        Member member4 = new Member("member4", 20, teamB);
        Member member5 = new Member("member5", 55, teamB);
        em.persist(member5);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    void startJPQL() {
        //member1??? ?????????.
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
        /*1??? ??????*/ QMember m = new QMember("m");
        /*2??? ??????*/ m = QMember.member;//??? ?????? ??????
        /*3??? ??????*/ m = member; //static import. ?????? ????????? ??????

        Member findMember = queryFactory
                .select(m)
                .from(m)
                .where(m.username.eq("member1"))
                .fetchOne();
    }

    @Test
    void search1() {
        Member findMember = queryFactory
                .selectFrom(member)//????????? select + from ????????? ??????
                .where(member.username.eq("member1")
                        .and(member.age.eq(10)))//and ?????? or??? ????????????
                .fetchOne();

        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    //????????? ????????? ????????????.
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
        //?????? ??????
        List<Member> fetch = queryFactory.selectFrom(member)
                                         .fetch();
    }

    @Test
    void resultFetch2() {
        //?????? ??????
        Member fetchOne = queryFactory.selectFrom(member).where(member.username.eq("member1"))
                .fetchOne();
    }

    @Test
    void resultFetch3() {
        //?????? ??? ??? ??????
        Member fetchFirst = queryFactory.selectFrom(QMember.member)
                .fetchFirst();
    }
    /*
       *?????? ?????? ??????
       1. ?????? ?????? ????????????(desc)
       2. ?????? ?????? ????????????(asc)
       3. ???, 2?????? ?????? ????????? ????????? ???????????? ??????(null last)
     */
    @Test
    void sort(){
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> result = queryFactory.selectFrom(member)
                .where(member.age.eq(100))//100?????? ??????
                .orderBy(member.age.desc(),//?????? ????????????
                        member.username.desc(),
                        member.username.asc().nullsLast())//username null?????? ??? ?????????
                .fetch();
        Member member6 = result.get(0);
        Member member5 = result.get(1);
        Member memberNull = result.get(2);
        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
        for (Member m : result) System.out.println("m = " + m);
    }

    @Test
    void paging1() {


        List<Member> result = queryFactory.selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)//?????? ???????????????
                .limit(2)//?????? ???????????????
                .fetch();
        assertThat(result.size()).isEqualTo(2);
        for (Member m : result) System.out.println("m = " + m);
    }

    @Test
    void aggregation() {
        //querydsl?????? ???????????? Tuple
        List<Tuple> result = queryFactory.select
                        (member.count(),
                            member.age.sum(),
                            member.age.avg(),
                            member.age.max(),
                            member.age.min())
                .from(member).fetch();

        Tuple tuple = result.get(0);

        Long memberCnt = tuple.get(member.count());
        Integer sumAge = tuple.get(member.age.sum());
        Double avgAge = tuple.get(member.age.avg());
        Integer maxAge = tuple.get(member.age.max());
        Integer minAge = tuple.get(member.age.min());

        assertThat(memberCnt).isEqualTo(4);
        assertThat(sumAge).isEqualTo(40);
        assertThat(avgAge).isEqualTo(10);
        assertThat(maxAge).isEqualTo(10);
        assertThat(minAge).isEqualTo(10);
    }

    /*
    * ?????? ????????? ??? ?????? ?????? ????????? ?????????
    * */
    @Test
    void aggregation2() {
        List<Tuple> result = queryFactory.select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();
        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        System.out.println("teamA = " + teamA);
        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);

        System.out.println("teamB = " + teamB);
        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(15);
    }

    /*
    * ???A??? ????????? ?????? ?????? ??????
    * */
    @Test
    void join() {
        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        for (Member m : result) System.out.println("m = " + m);
    }

    /*
    * ????????????
    * ????????? ????????? ?????? ????????? ?????? ?????? ?????? =
    * */
    @Test
    void theta_join() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamA"));
        List<Member> result = queryFactory
                .select(member)
                .from(member, team)//from?????? ??????
                .where(member.username.eq(team.name))
                .fetch();

        for (Member m : result) System.out.println("m = " + m);
    }

    /*
    * ????????? ?????? ???????????????, ??? ????????? teamA??? ?????? ??????, ????????? ?????? ??????
    * JPQL : select m, t from Member m left join m.team t on t.name = 'teamA'
    * */
    @Test
    void join_on_filtering() {
        List<Tuple> result = queryFactory.select(member, team)
                .from(member)
                .join(member.team, team).on(team.name.eq("teamA"))//?????? join??????
                .fetch();

        for (Tuple tuple : result)
            System.out.println("tuple = " + tuple);

    }

    @Test
    void join_where() {
        List<Tuple> result = queryFactory.select(member, team)
                .from(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        for (Tuple tuple : result)
            System.out.println("tuple = " + tuple);

    }

    /*
     * ??????????????? ?????? ????????? ?????? ??????
     * ????????? ????????? ?????? ????????? ?????? ?????? ?????? ??????
     * */
    @Test
    void theta_on_no_relation() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name))
                .fetch();

        for (Tuple t : result) System.out.println("t = " + t);
    }

    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    void no_fetchJoin() {
        em.flush();
        em.clear();
        Member findMember = queryFactory.selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        //?????? ????????? ???????????? ??????????????? team??? lazy ???????????? ?????? load?????? ?????? ????????????.
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("???????????? ?????????").isFalse();
    }

    @Test
    void yes_fetchJoin() {
        em.flush();
        em.clear();
        Member findMember = queryFactory.selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();

        //?????? ????????? ????????????????????? Team??? ?????? ???????????? team??? load???
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("???????????? ?????????").isTrue();
    }

    /*????????? ?????? ?????? ?????? ??????*/
    @Test
    void subQuery(){
        // ?????????????????? ????????? QMember??? ?????? ??? ??????????????? ??????.
        QMember memberSub = new QMember("memberSub");
        List<Member> result = queryFactory.selectFrom(member)
                .where(member.age.eq(
                        //????????????
                        JPAExpressions.select(memberSub.age.max()).from(memberSub)
                ))
                .fetch();

        for (Member member : result) System.out.println("member = " + member);
    }

    /*????????? ?????? ????????? ?????? ??????*/
    @Test
    void subQueryGoe(){

        QMember memberSub = new QMember("memberSub");
        List<Member> result = queryFactory.selectFrom(member)
                .where(member.age.goe(
                        JPAExpressions.select(memberSub.age.avg()).from(memberSub)
                ))
                .fetch();
        for (Member member : result) System.out.println("member = " + member);
    }

    /* in subquery */
    @Test
    void subQueryIn(){

        QMember memberSub = new QMember("memberSub");
        List<Member> result = queryFactory.selectFrom(member)
                .where(member.age.in(
                        JPAExpressions.select(memberSub.age).from(memberSub).where(memberSub.age.gt(10))
                ))
                .fetch();

        for (Member member : result) System.out.println("member = " + member);
    }

    @Test
    void selectSubQuery(){
        QMember memberSub = new QMember("memberSub");

        List<Tuple> result = queryFactory.select(
                member.username,
                JPAExpressions.select(memberSub.age.avg()).from(memberSub)
            ).from(member).fetch();
        for (Tuple tuple : result) System.out.println("tuple = " + tuple);
    }

    /*????????? ??????*/
    @Test
    void basicCase() {
        List<String> result = queryFactory.select(
                member.age
                    .when(10).then("??????")
                    .when(20).then("?????????")
                    .otherwise("??????"))
                .from(member)
                .fetch();

        for (String s : result) System.out.println("s = " + s);
    }

    /*????????? ??????*/
    @Test
    void complexCase() {
        List<String> result = queryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(0, 10)).then("0~20???")
                        .when(member.age.between(21, 30)).then("21???~30???")
                        .otherwise("??????")
                )
                .from(member)
                .fetch();

        for (String s : result) System.out.println("s = " + s);
    }

    @Test
    void constant() {
        List<Tuple> result = queryFactory.select(member.username, Expressions.constant("A"))
                .from(member)
                .fetch();

        for (Tuple tuple : result) System.out.println("tuple = " + tuple);
    }

    @Test
    void concat() {
        //username_age ??? ????????????
        List<String> result = queryFactory
                .select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .fetch();

        for (String s : result) System.out.println("s = " + s);
    }

    @Test
    void simpleProjection(){
        List<String> result = queryFactory.select(member.username).from(member).fetch();
        for (String username : result) System.out.println("username = " + username);

        List<Member> result2 = queryFactory.select(member).from(member).fetch();
        for (Member m : result2) System.out.println("m = " + m);
    }

    @Test
    void tupleProjection() {
        List<Tuple> result = queryFactory.select(member.username, member.age).from(member).fetch();
        for (Tuple tuple : result) {
            String username = tuple.get(member.username);
            Integer age = tuple.get(member.age);
            System.out.println("-----------");
            System.out.println(username);
            System.out.println(age);
        }
    }

    @Test
    void findDtoByJPQL() {
        List<MemberDto> resultList =
                em.createQuery("select new study.querydsl.dto.MemberDto(m.username, m.age) from Member m", MemberDto.class)
                        .getResultList();
        for (MemberDto memberDto : resultList) System.out.println("memberDto = " + memberDto);
    }

    @Test
    void findDtoBySetter() {
        List<MemberDto> result = queryFactory
                .select(Projections.bean(MemberDto.class, member.username, member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) System.out.println("memberDto = " + memberDto);
    }

    @Test
    void findDtoByFields() {
        List<MemberDto> result = queryFactory
                .select(Projections.fields(MemberDto.class, member.username, member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) System.out.println("memberDto = " + memberDto);
    }

    @Test
    void findDtoByConstructor() {
        List<MemberDto> result = queryFactory
                .select(Projections.constructor(MemberDto.class, member.username, member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) System.out.println("memberDto = " + memberDto);
    }

    @Test
    void findUserDto1() {
        List<UserDto> result = queryFactory
                .select(Projections.constructor(UserDto.class, member.username.as("name"), member.age))
                .from(member)
                .fetch();

        for (UserDto userDto : result) System.out.println("userDto = " + userDto);
    }

    @Test
    void findUserDto2() {
        List<UserDto> result = queryFactory
                .select(Projections.constructor(UserDto.class, member.username.as("name"), member.age))
                .from(member)
                .fetch();

        for (UserDto userDto : result) System.out.println("userDto = " + userDto);
    }

    @Test
    void findUserDto3() {

        QMember memberSub = new QMember("memberSub");
        List<UserDto> result = queryFactory
                .select(Projections
                        .constructor(
                                UserDto.class, member.username.as("name"),
                                ExpressionUtils.as(//??????????????? ????????? ??????
                                        JPAExpressions.select(memberSub.age.max())
                                                        .from(memberSub), "age"
                                )
                        )
                )
                .from(member)
                .fetch();

        for (UserDto userDto : result) System.out.println("userDto = " + userDto);
    }

    @Test
    void findDtoByQueryProjection() {
        List<MemberDto> result = queryFactory
                .select(new QMemberDto(member.username, member.age))//????????? ?????? ??????
                .from(member)
                .fetch();
        for (MemberDto memberDto : result) System.out.println("memberDto = " + memberDto);
    }

    @Test
    void distinct() {
        List<String> result = queryFactory
                .select(member.username).distinct()
                .from(member)
                .fetch();
    }

    @Test
    void dynamicQuery_BooleanBuilder() {
        String usernameParam = null;
        Integer ageParam = 10;
        List<Member> result = searchMember1(usernameParam, ageParam);
//        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember1(String usernameCond, Integer ageCond) {
        BooleanBuilder builder = new BooleanBuilder();
        if(usernameCond!=null) builder.and(member.username.eq(usernameCond));
        if(ageCond!=null) builder.and(member.age.eq(ageCond));

        return queryFactory
                .selectFrom(member)
                .where(builder)
                .fetch();
    }

    @Test
    void dynamicQuery_whereParam() {
        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember2(usernameParam, ageParam);
        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember2(String usernameParam, Integer ageParam) {
        return queryFactory.selectFrom(member)
                .where(allEq(usernameParam,ageParam))/*allEq??????*/
                .fetch();
    }

    private BooleanExpression allEq(String usernameParam, Integer ageParam) {
        return usernameEq(usernameParam).and(ageEq(ageParam));
    }

    private BooleanExpression usernameEq(String usernameParam) {
        if(usernameParam==null) return null;
        else return member.username.eq(usernameParam);
    }

    private BooleanExpression ageEq(Integer ageParam) {
        return ageParam == null ? null : member.age.eq(ageParam);
    }
    //?????? ?????? isValid, ????????? In --> isServiceable ?????? ?????? ????????????


    //??????
    @Test
    void bulkUpdate() {
        //count??? ????????? ?????? ?????? ???
        long count = queryFactory.update(member)
                .set(member.username, "?????????")
                .where(member.age.lt(28)).execute();

        //????????? ???????????? ?????????
        em.flush();
        em.clear();
    }

    //?????????
    @Test
    void bulkAdd() {
        queryFactory.update(member)
                .set(member.age, member.age.add(1))
                .execute();

        //????????? ???????????? ?????????
        em.flush();
        em.clear();
    }

    //??????
    @Test
    void bulkDelete() {
        queryFactory.delete(member)
                .where(member.age.gt(18))
                .execute();

        //????????? ???????????? ?????????
        em.flush();
        em.clear();
    }

    @Test
    void sqlFunction() {
        List<String> result = queryFactory
                .select(Expressions.stringTemplate(
                        "function('replace', {0}, {1}, {2})",
                        member.username, "member", "M"))
                .from(member)
                .fetch();

        for (String s : result) System.out.println("s = " + s);
    }

    @Test
    void sqlFunctionLower() {
        JPAQuery<String> result = queryFactory
                .select(member.username)
                .from(member)
                .where(member.username.eq(Expressions.stringTemplate("function('lower', {0})",
                        member.username)));
    }
}
