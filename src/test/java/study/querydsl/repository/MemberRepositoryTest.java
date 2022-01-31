package study.querydsl.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Transactional
@SpringBootTest
class MemberRepositoryTest {

    @Autowired EntityManager em;
    @Autowired MemberRepository memberRepository;

    @Test
    void basicTest() {
        Member member = new Member("member1", 10);
        memberRepository.save(member);

        Member findMember = memberRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        List<Member> result1 = memberRepository.findAll();

        List<Member> result2 = memberRepository.findByUsername("member1");
//        assertThat(result2).containsExactly(member);
    }

    @Test
    public void searchTest(){
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

        // -- 컨디션 --//
        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(15);
        condition.setAgeLoe(35); // -> 15~35살
        condition.setTeamName("teamB"); // teamName은 teamB이어야 한다.
        //username 컨디션은 추가하지 않음. 즉 username은 어찌되어도 상관없다는 것이다.
        // -----------

        List<MemberTeamDto> result = memberRepository.search(condition);
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getUsername()).isEqualTo("member4");
    }

    @Test
    public void searchPageSimpleTest(){
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

        MemberSearchCondition condition = new MemberSearchCondition();
        PageRequest pageRequest = PageRequest.of(0, 3);
        Page<MemberTeamDto> result = memberRepository.searchPageSimple(condition, pageRequest);
        assertThat(result.getSize()).isEqualTo(3);
        List<MemberTeamDto> content = result.getContent();
        for (MemberTeamDto memberTeamDto : content) System.out.println("memberTeamDto = " + memberTeamDto);
    }

    @Test
    void querydslPredicateExecute() {

//        Team teamA = new Team("teamA");
//        Team teamB = new Team("teamB");
//        em.persist(teamA);
//        em.persist(teamB);
//
//        Member member1 = new Member("member1", 10, teamA);
//        Member member2 = new Member("member2", 20, teamA);
//        Member member3 = new Member("member3", 10, teamB);
//        Member member4 = new Member("member4", 20, teamB);
//        Member member5 = new Member("member5", 55, teamB);
//        em.persist(member5);
//        em.persist(member1);
//        em.persist(member2);
//        em.persist(member3);
//        em.persist(member4);

        QMember member = QMember.member;
        Iterable<Member> result = memberRepository.findAll(member.age.between(10, 40).and(member.username.eq("member1")));
        for (Member m : result) System.out.println("m = " + m);
    }
}