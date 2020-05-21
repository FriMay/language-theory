package arbina.sps.api.services;

import arbina.infra.exceptions.BadRequestException;
import arbina.infra.services.id.ArbinaId;
import arbina.sps.store.entity.Client;
import arbina.sps.store.repository.ClientsRepository;
import org.springframework.stereotype.Service;


@Service
public class ClientsService {

    private final ClientsRepository clientsRepository;

    private final ArbinaId arbinaId;

    public ClientsService(ClientsRepository clientsRepository, ArbinaId arbinaId) {
        this.clientsRepository = clientsRepository;
        this.arbinaId = arbinaId;
    }

    /**
     * Validating and return client entity. If the client doesn't exist, create it.
     *
     * @param clientId client id.
     * @return client entity.
     */
    public Client validateAndGetClient(String clientId) {

        if (clientId == null){
            throw new BadRequestException("Client id can't be empty.");
        }

        Client client = clientsRepository.findById(clientId).orElse(null);

        if (client == null) {

            if (arbinaId.getClient(clientId) == null) {
                throw new BadRequestException(String.format("Client with \"%s\" name is not found", clientId));
            }

            client = Client.builder()
                    .clientId(clientId)
                    .build();

            client = clientsRepository.saveAndFlush(client);
        }

        return client;

    }
}
