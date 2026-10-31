package org.freedom.infra.api.brasilapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

/**
 * Cliente de Integração com a BrasilAPI utilizando o java.net.http nativo do Java moderno.
 * Elimina milhares de linhas de tabelas de CEP estáticas locais do ERP legado.
 */
public class BrasilApiClient {

    private static final Logger log = LoggerFactory.getLogger(BrasilApiClient.class);
    private static final String BRASIL_API_BASE_URL = "https://brasilapi.com.br/api/cep/v1/";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public BrasilApiClient() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Consulta dados de logradouro a partir do CEP.
     *
     * @param cep CEP com ou sem formatação
     * @return Optional contendo o endereço resolvido
     */
    public Optional<EnderecoBrasilApiDto> consultarCep(String cep) {
        if (cep == null) {
            return Optional.empty();
        }

        String cepSanitizado = cep.replaceAll("\\D", "");
        if (cepSanitizado.length() != 8) {
            log.warn("CEP inválido fornecido: {}", cep);
            return Optional.empty();
        }

        try {
            URI uri = URI.create(BRASIL_API_BASE_URL + cepSanitizado);
            HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(8))
                .header("Accept", "application/json")
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                EnderecoBrasilApiDto dto = objectMapper.readValue(response.body(), EnderecoBrasilApiDto.class);
                return Optional.of(dto);
            } else {
                log.warn("BrasilAPI respondeu status {} para o CEP {}", response.statusCode(), cepSanitizado);
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("Erro ao consultar BrasilAPI para o CEP {}: {}", cepSanitizado, e.getMessage());
            return Optional.empty();
        }
    }
}
