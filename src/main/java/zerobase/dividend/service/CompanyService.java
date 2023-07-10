package zerobase.dividend.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import zerobase.dividend.exception.impl.NoCompanyException;
import zerobase.dividend.persist.DividendRepository;
import zerobase.dividend.model.Company;
import zerobase.dividend.model.ScrapedResult;
import zerobase.dividend.persist.CompanyRepository;
import zerobase.dividend.persist.entity.CompanyEntity;
import zerobase.dividend.persist.entity.DividendEntity;
import zerobase.dividend.scraper.Scraper;

import java.util.List;

import java.util.stream.Collectors;

import org.apache.commons.collections4.Trie;

@Service
@AllArgsConstructor
public class CompanyService {  //스프링부트의 bean 이어서 싱글톤으로 관리되고있음

    private final Trie trie;   //09_자동완성 02 04:34

    private final Scraper yahooFinanceScraper;

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;




    public Company save(String ticker){
        //먼저 db에 해당 회사의 존재 여부를 파악
       boolean exists = this.companyRepository.existsByTicker(ticker);
        if(exists){
            throw new RuntimeException("aleady exists ticker -> " + ticker);
        }

        // 존재하지않는다면 스크래핑, 저장
        return this.storeCompanyAndDividend(ticker);
    }

    
    //저장에 성공시 회사를 리턴, 아니면 에러를 리턴
    private Company storeCompanyAndDividend(String ticker){
        // ticker 를 기준으로 회사를 스크래핑
        Company company = this.yahooFinanceScraper.scrapCompanyByTicker(ticker);

        if(ObjectUtils.isEmpty(company)){ //company가 비어있다면 예외발생
            throw new RuntimeException("failed to scrap ticker ->" + ticker);
        }

        // 해당 회사가 존재할 경우, 회사의 배당금 정보 스크래핑
        ScrapedResult scrapedResult = this.yahooFinanceScraper.scrap(company);


        // 스크래핑 결과

         CompanyEntity companyEntity = this.companyRepository.save(new CompanyEntity(company));


         List<DividendEntity> dividendEntities = scrapedResult.getDividends().stream()
                .map(e -> new DividendEntity(companyEntity.getId(), e))
                .collect(Collectors.toList());

        this.dividendRepository.saveAll(dividendEntities);

        return company;
    }


    public Page<CompanyEntity> getAllCompany(Pageable pageable){  //다 불러오면 부담이 커서 페이징기능 사용
        return this.companyRepository.findAll(pageable);
    }

    //Like 연산자 자동완성기능
    public List<String> getCompanyNamesByKeyword(String keyword){

        Pageable limit = PageRequest.of(0,10);
        Page<CompanyEntity> companyEntities = this.companyRepository.findByNameStartingWithIgnoreCase(keyword,limit);
       return companyEntities.stream()
                                .map(e -> e.getName())
                                .collect(Collectors.toList());
    }



    //Trie 이용한 자동완성
    public void addAutocompleteKeyword(String keyword){
        this.trie.put(keyword, null);
    }

    //Trie 이용한 자동완성
    public List<String> autocomplete(String keyword){
       return (List<String>) this.trie.prefixMap(keyword).keySet()
                .stream()
               .limit(10)
               .collect(Collectors.toList());
    }



    public void deleteAutocompleteKeyword(String keyword){
        this.trie.remove(keyword);
    }


    public String deleteCompany(String ticker) {
        CompanyEntity company = this.companyRepository.findByTicker(ticker)
                .orElseThrow( ()-> new NoCompanyException());

        // 삭제할 것들 ( 회사배당금데이터 -> 회사데이터 -> 자동완성데이터 -> 캐시데이터)
        this.dividendRepository.deleteAllByCompanyId(company.getId());
        this.companyRepository.delete(company);

        this.deleteAutocompleteKeyword(company.getName());

        return company.getName();

    }
}

