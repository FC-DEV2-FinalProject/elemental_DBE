package ipat.admin.board.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "board_manager")
@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BoardManager {
    @Id
    @Column(name = "manager_num", precision = 38, scale = 0)
    private Long managerNum;

    @Column(name ="board_code", length = 10)
    private String boardCode;

    @Column(name = "board_name", length = 30)
    private String boardName;

    @Column(name = "board_type", length = 2)
    private String boardType;

    @Column(name = "board_secret")
    private Boolean boardSecret;

    @Column(name = "board_list_cnt", precision = 38, scale = 0)
    private Long boardListCnt;

    @Column(name = "comment_list_cnt", precision = 38, scale = 0)
    private Long commentListCnt;

    @Column(name = "board_title", length = 200)
    private String boardTitle;

    @Column(name = "board_save_path", length = 300)
    private String boardSavePath;

    @Column(name = "reg_date")
    private LocalDateTime regDate;

    @PrePersist
    public void prePersist() {
        this.regDate = LocalDateTime.now();
    }
}
