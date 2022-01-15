package study.querydsl.entity;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@ToString(of={"id", "name"})
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@Getter @Setter
@Entity
public class Team {
    @Id @GeneratedValue
    @Column(name="member_id")
    private Long id;
    private String name;

    @OneToMany(mappedBy = "team", cascade=CascadeType.ALL)
    private List<Member> members = new ArrayList<>();

    public Team(String name) {
        this.name = name;
    }
}
