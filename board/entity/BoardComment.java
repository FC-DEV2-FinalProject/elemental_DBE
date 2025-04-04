package ipat.admin.board.entity;

import ipat.admin.board.entity.Board;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "board_comment")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BoardComment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "board_comment_seq_gen")
    @SequenceGenerator(name = "board_comment_seq_gen", sequenceName = "board_comment_seq", allocationSize = 1)
    private Long commentNum;  // 답글 번호

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Board board;  // 게시판과의 FK 관계

    @Column(nullable = false, name = "commentIdx")
    private Long commentIdx = commentNum;  // 댓글 인덱스

    @Column(nullable = false)
    private Long thread = 0L;  // 스레드

    @Column(nullable = false, name = "orderNum")
    private Long orderNum = 0L;  // 승인번호

    @Column(length = 1500, nullable = false)
    private String comments;  // 답글 내용

    @Column(length = 50, nullable = false, name = "user_name")
    private String userName;  // 회원 이름

    @Column(length = 20, nullable = false, name = "user_id")
    private String userId;  // 회원 ID

    @Column(nullable = false, name = "write_date")
    private LocalDateTime writeDate;  // 등록 일시

    @Column(name = "modify_date")
    private LocalDateTime modifyDate;  // 수정 일시

    @Column(length = 64)
    private String pwd;  // 비밀번호

    @Column(length = 15, nullable = false, name = "user_ip")
    private String userIp;  // 회원 IP 주소

    @Column(length = 15, name = "update_ip")
    private String updateIp;  // 수정 IP 주소

    @PrePersist
    public void prePersist() {
        this.writeDate = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() { this.modifyDate = LocalDateTime.now();}
}