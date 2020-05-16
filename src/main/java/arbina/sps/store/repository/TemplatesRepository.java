package arbina.sps.store.repository;

import arbina.sps.store.entity.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.Optional;
import java.util.stream.Stream;


public interface TemplatesRepository extends JpaRepository<Template, Long> {

    @Query("SELECT t FROM Template t " +
            "ORDER BY t.id DESC")
    Stream<Template> fetchAllSortedStream();

    Optional<Template> findByName(String name);

    @Query("SELECT t FROM Template t " +
            "WHERE LOWER(t.name) LIKE LOWER(CONCAT('%',:templateName,'%')) " +
            "ORDER BY t.id DESC")
    Stream<Template> findLikeName(String templateName);

    @Query("SELECT COUNT(t) FROM Template t " +
            "WHERE LOWER(t.name) LIKE LOWER(CONCAT('%',:templateName,'%') ) ")
    Optional<Integer> countLikeName(String templateName);

    @Modifying
    @Query("UPDATE Template t " +
            "SET t.lastUsedAt = :lastUsedAt " +
            "WHERE t.id = :templateId")
    void setLastUsedAtById(Date lastUsedAt, Long templateId);
}
