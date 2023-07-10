package zerobase.dividend.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import zerobase.dividend.exception.impl.NoCompanyException;
import zerobase.dividend.model.Company;
import zerobase.dividend.model.Dividend;
import zerobase.dividend.model.ScrapedResult;
import zerobase.dividend.model.constants.CacheKey;
import zerobase.dividend.persist.CompanyRepository;
import zerobase.dividend.persist.DividendRepository;
import zerobase.dividend.persist.entity.CompanyEntity;
import zerobase.dividend.persist.entity.DividendEntity;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class FinanceService {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;


    @Cacheable(key = "#companyName", value = CacheKey.KEY_FINANCE)
    public ScrapedResult getDividendByCompanyName(String companyName){

        log.info("search company -> " + companyName);
        // 1. 회사명을 기준으로 회사 정보 조회
        CompanyEntity company = this.companyRepository.findByName(companyName)
                                            .orElseThrow(()-> new NoCompanyException());
        // .orElseThrow():  값이 없으면 인자로 넘겨주는 예외를 발생, 정상적이면 Optional 이 벗겨진 상태로 반환


        // 2. 조회된 회사 ID로 배당금 정보 조회
        List<DividendEntity> dividendEntities = this.dividendRepository.findAllByCompanyId(company.getId());


        // 3. 결과 조합 후 반환

        //for 문 사용시 (방법1)
        List<Dividend> dividends = new ArrayList<>();
        for(var entity: dividendEntities){
            dividends.add( new Dividend(entity.getDate(),entity.getDividend()));
        }




//        stream 사용 시 (방법2)
//        List<Dividend> dividends = dividendEntities.stream().map(e -> new Dividend(e.getDate(), e.getDividend()))
//                        .collect(Collectors.toList());



        return new ScrapedResult( new Company(company.getTicker(),company.getName()),dividends);
        // ID로 조회한 배당금 정보가 엔티티타입 리스트이기때문에, 위에서 벗기는 가공을 먼저해줌

    }
}
