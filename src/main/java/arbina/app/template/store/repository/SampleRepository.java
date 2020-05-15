package arbina.app.template.store.repository;

import arbina.app.template.store.entity.Sample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.stream.Stream;

public interface SampleRepository extends JpaRepository<Sample, String> {

    @Query("SELECT s FROM Sample s " +
            "ORDER BY s.id DESC")
    Stream<Sample> fetchAllSortedStream();
}
