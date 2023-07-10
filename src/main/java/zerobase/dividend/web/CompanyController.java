package zerobase.dividend.web;

import lombok.AllArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import zerobase.dividend.model.Company;
import zerobase.dividend.model.constants.CacheKey;
import zerobase.dividend.persist.entity.CompanyEntity;
import zerobase.dividend.service.CompanyService;

import java.util.List;


@RestController
@RequestMapping("/company")  //경로에서 공통되는 부분
@AllArgsConstructor
public class CompanyController {


    private final CompanyService companyService;

    private final CacheManager redisCacheManager;

    //자동완성
    @GetMapping("/autocomplete")
    public ResponseEntity<?> autocomplete(@RequestParam String keyword){
//        List<String> result = this.companyService.autocomplete(keyword);   //방법1
        List<String> result = this.companyService.getCompanyNamesByKeyword(keyword);  //방법2
        return ResponseEntity.ok(result);
    }



    @GetMapping
    @PreAuthorize("hasRole('READ')")
    public ResponseEntity<?> searchCompany(final Pageable pageable){ // Pageable 값이 임의로 변하는 것을 방지
        Page<CompanyEntity> companies = this.companyService.getAllCompany(pageable);
        return ResponseEntity.ok(companies);
    }


    /**
     * 회사 및 배당금 정보 추가
     * @param request
     * @return
     * */
    @PostMapping
    @PreAuthorize("hasRole('WRITE')")  // 관리자(WIRTE) 권한을 가진 계정만 허용
    public ResponseEntity<?> addCompany(@RequestBody Company request){
        String ticker = request.getTicker().trim();
        if(ObjectUtils.isEmpty(ticker)){
            throw new RuntimeException("ticker is empty!");
        }

        Company company = this.companyService.save(ticker);
        this.companyService.addAutocompleteKeyword(company.getName());  //회사 저장할때마다 trie 에 저장


        return ResponseEntity.ok(company);  // 회사정보를 반환하고 종료
    }



    @DeleteMapping("/{ticker}")
    public ResponseEntity<?> deleteCompany(@PathVariable String ticker){

        String companyName = this.companyService.deleteCompany(ticker);
        this.clearFinanceCache(companyName);  // 해당회사 데이터들을 지우고 캐시데이터도 삭제해준다.

        return ResponseEntity.ok(companyName);
    }

    public void clearFinanceCache(String companyName){
        this.redisCacheManager.getCache(CacheKey.KEY_FINANCE).evict(companyName);
    }

}
