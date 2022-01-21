package study.querydsl.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
class MemberJpaReposiotoryTest {

    @Autowired EntityManager em;
    @Autowired MemberJpaReposiotory repository;


    @Test
    void save() {
        repository.save(new Member("member1", 10));
    }

    @Test
    void findById() {
        Member member = new Member("member1", 10);
        repository.save(member);
        Member findMember = repository.findById(member.getId()).get();
        Assertions.assertThat(member.getId()).isEqualTo(findMember.getId());
        Assertions.assertThat(member.getUsername()).isEqualTo(findMember.getUsername());
        Assertions.assertThat(member).isEqualTo(findMember);
    }

    @Test
    void findAll() {
        Member member1 = new Member("member1", 10);
        Member member2 = new Member("member2", 10);
        Member member3 = new Member("member3", 10);
        repository.save(member1);
        repository.save(member2);
        repository.save(member3);

        List<Member> result = repository.findAll();
        Assertions.assertThat(result).contains(member1, member2, member3);
    }


    @Test
    void findByUsername() {
        Member member1 = new Member("member1", 10);
        repository.save(member1);
        List<Member> result = repository.findByUsername(member1.getUsername());
        Member findMember = result.get(0);
        Assertions.assertThat(findMember).isEqualTo(member1);
    }

    @Test
    void findAll_Querydsl() {
        Member member1 = new Member("member1", 10);
        Member member2 = new Member("member2", 10);
        Member member3 = new Member("member3", 10);
        repository.save(member1);
        repository.save(member2);
        repository.save(member3);

        List<Member> result = repository.findAll_Querydsl();
        Assertions.assertThat(result).contains(member1, member2, member3);
    }


    @Test
    void findByUsername_Querydsl() {
        Member member1 = new Member("member1", 10);
        repository.save(member1);
        List<Member> result = repository.findByUsername_Querydsl(member1.getUsername());
        Member findMember = result.get(0);
        Assertions.assertThat(findMember).isEqualTo(member1);
    }
    // -----------------------------------------------------------------------------------------------------

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

        List<MemberTeamDto> result = repository.searchByBuilder(condition);
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getUsername()).isEqualTo("member4");
    }

    @Test
    public void searchTest2(){
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

        List<MemberTeamDto> result = repository.search(condition);
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getUsername()).isEqualTo("member4");
    }
}