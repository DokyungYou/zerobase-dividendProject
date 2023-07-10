package zerobase.dividend.scheduler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import zerobase.dividend.model.Company;
import zerobase.dividend.model.ScrapedResult;
import zerobase.dividend.model.constants.CacheKey;
import zerobase.dividend.persist.CompanyRepository;
import zerobase.dividend.persist.DividendRepository;
import zerobase.dividend.persist.entity.CompanyEntity;
import zerobase.dividend.persist.entity.DividendEntity;
import zerobase.dividend.scraper.Scraper;

import java.util.List;

@Slf4j
@Component
@EnableCaching
@AllArgsConstructor
public class ScraperScheduler {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;
    private final Scraper yahooFinanceScraper;


    //일정 주기마다 수행 (배당금 데이터가 중복으로 저장되는 것을 막기 위해 dividendEntity 에서 복합 유니크키("companyId","date" 설정해주기)
    @CacheEvict(value = CacheKey.KEY_FINANCE, allEntries = true) // value 에 해당하는 부분이 key의 프리픽스로 사용됨 / finance 에 해당하는 데이터는 모두 비운다는 의미
    @Scheduled(cron = "${scheduler.scrap.yahoo}")  // 설정 경로 (.yml)
    public void yahooFinanceScheduling(){
        log.info("scraping scheduler is started");
        //저장된 회사 목록 조회
        List<CompanyEntity> companyEntities = companyRepository.findAll();

        //회사마다 배당금 정보를 새로 스크래핑
        for(CompanyEntity company : companyEntities){
            log.info("scraping scheduler is started -> " + company.getName());
            ScrapedResult scrapedResult = this.yahooFinanceScraper.scrap(
                    new Company(company.getName(), company.getTicker()));


        //스크래핑한 배당금정보 중에 DB에 없는 값은 저장
          scrapedResult.getDividends().stream()
                  // dividend -> dividendEntity 로 매핑
                  .map(e -> new DividendEntity(company.getId(),e))
                  
                  // element 를 하나씩 dividendRepository 에 삽입 (존재하지 않은 경우에만)
                  .forEach(e -> {
                      boolean exists = this.dividendRepository.existsByCompanyIdAndDate(e.getCompanyId(), e.getDate());
                      if(!exists){
                          this.dividendRepository.save(e);
                          log.info("insert new dividends-> " + e.toString());
                      }

                  });

            // 스크래핑 대상 사이트 서버에 연속적으로 요청을 날리지 않도록 일시정지 (텀을 두면서 해당서버에 요청)
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }


        }

        

    }
}
