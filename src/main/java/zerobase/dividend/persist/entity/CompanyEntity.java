package zerobase.dividend.persist.entity;

import lombok.*;
import zerobase.dividend.model.Company;

import javax.persistence.*;

@Entity(name = "company")
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CompanyEntity {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;

    private String name;


    @Column(unique = true)
    private String ticker;

    public CompanyEntity(Company company){
        this.ticker = company.getTicker();
        this.name = company.getName();

    }
}
