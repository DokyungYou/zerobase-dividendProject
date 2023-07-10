package zerobase.dividend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data  //@Getter, @Setter, @RequiredArgsConstructor, @ToString, @EqualsAndHashCode 를 포함
//@Builder  // 빌더패턴을 사용할 수 있게 해줌 (인스턴스 생성 시에 각각 어떤 멤버변수에 들어가는지 지정해줄 수 있음, 순서상관 X, 원하는 값만 넣을 수도 있음)
@NoArgsConstructor
@AllArgsConstructor
public class Company {

    private String ticker;
    private String name;
}
