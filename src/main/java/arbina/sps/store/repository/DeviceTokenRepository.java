package arbina.sps.store.repository;

import arbina.sps.store.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.stream.Stream;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    @Query("SELECT s FROM DeviceToken s " +
            "ORDER BY s.id DESC")
    Stream<DeviceToken> fetchAllSortedStream();
}
