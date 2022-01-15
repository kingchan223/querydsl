package study.querydsl.entity;

import lombok.*;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;

@ToString(of={"id", "username", "age"})
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@Getter @Setter
@Entity
public class Member {
    @Id @GeneratedValue
    @Column(name="member_id")
    private Long id;
    private String username;
    private int age;

    @JoinColumn(name="team_id")
    @ManyToOne(fetch = LAZY)
    private Team team;

    public void changeTeam(Team team) {
        this.team = team;
        team.getMembers().add(this);
    }

    public Member(String username) {
        this.username = username;
    }

    public Member(String username, int age, Team team) {
        this.username = username;
        this.age = age;
        if(team!=null) changeTeam(team);
        this.team = team;
    }

    public Member(String username, int age) {
        this.username = username;
        this.age = age;
    }

}
