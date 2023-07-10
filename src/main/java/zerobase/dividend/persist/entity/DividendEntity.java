package zerobase.dividend.persist.entity;

import lombok.*;
import zerobase.dividend.model.Dividend;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "dividend")
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"companyId","date"}
                )
        }
)
public class DividendEntity {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;

    private Long companyId;   // 강의에서 따라한대로 company_id 였는데 레파지토리에서 메소드생성 시에 의존성주입 문제 생김 '_'가 안 잡혀서..?

    private LocalDateTime date;

    private String dividend;

    public DividendEntity(Long companyId, Dividend dividend){
        this.companyId = companyId;
        this.date = dividend.getDate();
        this.dividend = dividend.getDividend();
    }

}
