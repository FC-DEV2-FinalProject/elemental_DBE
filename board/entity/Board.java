package ipat.admin.board.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "board") // ERD 상 테이블명이 '게시판'
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "board_seq_gen")
    @SequenceGenerator(name = "board_seq_gen", sequenceName = "board_seq", allocationSize = 1)
    private Long boardNum;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "manager_num")
    private BoardManager managerNum;

    @Column(name = "board_idx")
    private Long boardIdx = boardNum;

    @Column(name = "thread")
    private Long thread = 0L;

    @Column(name = "order_num")
    private Long orderNum;

    @Column(name = "subject", length = 250)
    private String subject;

    @Column(name = "contents", columnDefinition = "TEXT")
    private String contents;

    @Column(name = "pwd", length = 64)
    private String pwd;

    @Column(name = "is_secret", length = 6)
    private Boolean isSecret;

    @Column(name = "read_cnt", precision = 30, scale = 0)
    private Long readCnt;

    @Column(name = "user_name", length = 50)
    private String userName;

    @Column(name = "user_id", length = 20)
    private String userId;

    @Column(name = "email", length = 50)
    private String email;

    @Column(name = "write_date")
    private LocalDateTime createdAt;

    @Column(name = "modify_date")
    private LocalDateTime updatedAt;

    @Column(name = "user_ip", length = 15)
    private String userIp;

    @Column(name = "update_ip", length = 15)
    private String updateIp;

    @Column(name = "token", length = 80)
    private String token = null;

    // '1' = 중요, '2' = 공고, 기본값은 null
    @Column(name = "post_type", columnDefinition = "CHAR(1)", nullable = true)
    private Character postType;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now();}
}
