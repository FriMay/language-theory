package arbina.sps.store.repository;

import arbina.sps.store.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.stream.Stream;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    @Query("SELECT t FROM DeviceToken t " +
            "ORDER BY t.id DESC")
    Stream<DeviceToken> fetchAllSortedStream();

    @Query("SELECT COUNT(t) > 0 FROM DeviceToken t " +
            "WHERE t.username = :username AND t.token = :token AND t.tokenType = :tokenType")
    boolean isTokenExists(String username, String token, String tokenType);


    @Query("SELECT dt FROM DeviceToken dt " +
            "WHERE dt.tokenType = :tokenType")
    Stream<DeviceToken> fetchDeviceTokenStreamByType(String tokenType);
}
