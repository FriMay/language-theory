package arbina.sps.store.repository;

import arbina.sps.store.entity.Localization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.stream.Stream;

public interface LocalizationsRepository extends JpaRepository<Localization, Long> {

    @Query("SELECT tl FROM Localization tl " +
            "WHERE tl.templateId = :templateId")
    Stream<Localization> fetchAllByTemplateId(Long templateId);

    @Query("SELECT COUNT(tl) FROM Localization tl " +
            "WHERE tl.templateId = :templateId")
    Long fetchCountByTemplateId(Long templateId);

    @Query("SELECT tl FROM Localization tl " +
            "WHERE tl.templateId = :templateId " +
            "AND tl.localeIso = :localeIso")
    Optional<Localization> findByTemplateIdAndLocale(Long templateId, String localeIso);
}
