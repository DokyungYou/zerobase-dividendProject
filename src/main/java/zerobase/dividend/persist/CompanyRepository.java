package zerobase.dividend.persist;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.dividend.persist.entity.CompanyEntity;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<CompanyEntity,Long> {
    // 해당 회사의 존재여부 확인
    boolean existsByTicker(String ticker);


    // CompanyEntity 로 바로 반환받지않고 Optional 로 감싸서 받는 이유:
    // NullPointerException 을 방지 , 값이 없는 경우에 대한 처리도 코드적으로 조금 더 깔끔하게 정리가능
    Optional<CompanyEntity> findByName(String name);

    //자동완성 기능 (대소문자구별 X) (Like 연산자)
    Page<CompanyEntity> findByNameStartingWithIgnoreCase(String s, Pageable pageable);


    Optional<CompanyEntity> findByTicker(String ticker);

}
