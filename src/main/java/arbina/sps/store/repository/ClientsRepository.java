package arbina.sps.store.repository;

import arbina.sps.store.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientsRepository extends JpaRepository<Client, String> {
}
