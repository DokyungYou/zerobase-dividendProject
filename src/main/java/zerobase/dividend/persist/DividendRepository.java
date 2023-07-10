package zerobase.dividend.persist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import zerobase.dividend.persist.entity.DividendEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DividendRepository extends JpaRepository<DividendEntity,Long> {

    List<DividendEntity> findAllByCompanyId(Long CompanyId);

    boolean existsByCompanyIdAndDate(Long CompanyId, LocalDateTime date); // 일반 select where 절의 조건으로 조회하는 것보다 훨씬 빠르게 조회가능

     @Transactional
    void deleteAllByCompanyId(Long id);
}
