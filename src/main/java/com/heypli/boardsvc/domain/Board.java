package com.heypli.boardsvc.domain;


import com.heypli.boardsvc.domain.enums.BoardType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table
public class Board implements Serializable {

    @Id
    @Column(name = "id", nullable = false)
    // 기본키가 자동으로 할당됨(키생성을 DB에 위임하는 전략 - IDENTITY)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column
    private String title;

    @Column
    private String subTitle;

    @Column
    private String content;

    @Column
    // Enum타입 매핑용 어노테이션
    // 자바 enum형과 DB변환을 지원
    // DB에는 String으로 저장하겠다고 선언
    @Enumerated(EnumType.STRING)
    private BoardType boardType;

    @Column
    private LocalDateTime createdDate;

    @Column
    private LocalDateTime updatedDate;

    // Board와 User 1:1 관계로 설정
    // User 객체가 저장되는 것이 아니라 User의 PK가 저장됨
    // fetch eager : 처음 Board 도메인 조회시 User객체를 함께 조회한다는뜻
    // fetch lazy : User객체를 사용할 때 조회한다는 뜻
    @OneToOne(fetch = FetchType.LAZY)
    private User user;

    @Builder
    public Board(String title, String subTitle, String content, BoardType boardType,
                LocalDateTime createdDate, LocalDateTime updatedDate, User user) {
        this.title = title;
        this.subTitle = subTitle;
        this.content = content;
        this.boardType = boardType;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
        this.user = user;
    }
}
