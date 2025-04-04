package ipat.admin.board.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "board_pds")
@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BoardPds {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pds_num;

    @ManyToOne
    @JoinColumn(name = "board_num", nullable = false)
    private Board board;

    @Column(name = "file_original_name", length = 100)
    private String fileOriginalName;

    @Column(name = "file_saved_name", length = 100)
    private String fileSavedName;

    @Column(name ="file_save_path", length = 50)
    private String fileSavePath;

    @Column(name = "content_type", length = 100)
    private String contentType;


}
