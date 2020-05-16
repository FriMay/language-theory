package arbina.sps.store.repository;

import arbina.sps.store.entity.TemplateLocalization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.stream.Stream;

public interface TemplateLocalizationRepository extends JpaRepository<TemplateLocalization, Long> {

    @Query("SELECT tl FROM TemplateLocalization tl " +
            "WHERE tl.templateId = :templateId")
    Stream<TemplateLocalization> fetchAllByTemplateId(Long templateId);

    @Query("SELECT tl FROM TemplateLocalization tl " +
            "WHERE tl.templateId = :templateId " +
            "AND tl.localeIso = :localeIso")
    Optional<TemplateLocalization> findByTemplateIdAndLocale(Long templateId, String localeIso);
}
