package org.freedom.core.service.person;

import org.freedom.core.domain.person.Pessoa;
import org.freedom.infra.api.brasilapi.BrasilApiClient;
import org.freedom.infra.api.brasilapi.EnderecoBrasilApiDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Serviço de aplicação para gestão de Clientes e Pessoas.
 * Integra auto-preenchimento via BrasilAPI e execução assíncrona com Virtual Threads do Java 25.
 */
public class ClienteService {

    private static final Logger log = LoggerFactory.getLogger(ClienteService.class);

    private final PessoaRepository repository;
    private final BrasilApiClient brasilApiClient;

    public ClienteService() {
        this.repository = new PessoaRepository();
        this.brasilApiClient = new BrasilApiClient();
    }

    public ClienteService(PessoaRepository repository, BrasilApiClient brasilApiClient) {
        this.repository = repository;
        this.brasilApiClient = brasilApiClient;
    }

    /**
     * Consulta assíncrona de clientes via Virtual Threads para não travar a Event Dispatch Thread (EDT) do Swing.
     */
    public CompletableFuture<List<Pessoa>> buscarClientesAsync(String termo) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Buscando clientes com termo '{}' em Virtual Thread [{}]", termo, Thread.currentThread());
            return repository.buscarPorTermo(termo);
        }, runnable -> Thread.ofVirtual().start(runnable));
    }

    /**
     * Completa dados de endereço a partir de um CEP consultado na BrasilAPI.
     */
    public void preencherEnderecoPorCep(Pessoa pessoa, String cep) {
        Optional<EnderecoBrasilApiDto> enderecoOpt = brasilApiClient.consultarCep(cep);
        enderecoOpt.ifPresent(dto -> {
            pessoa.setCep(dto.cep());
            pessoa.setLogradouro(dto.street());
            pessoa.setBairro(dto.neighborhood());
            pessoa.setNomeMunicipio(dto.city());
            pessoa.setUf(dto.state());
            log.info("Endereço preenchido com sucesso via BrasilAPI para o CEP {}", cep);
        });
    }

    public Pessoa salvarCliente(Pessoa cliente) {
        if (cliente.getRazaoSocial() == null || cliente.getRazaoSocial().isBlank()) {
            throw new IllegalArgumentException("A razão social ou nome é obrigatório.");
        }
        if (cliente.getCpfCnpj() == null || cliente.getCpfCnpj().isBlank()) {
            throw new IllegalArgumentException("O CPF ou CNPJ é obrigatório.");
        }
        cliente.setEhCliente(true);
        return repository.salvar(cliente);
    }
}

