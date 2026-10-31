package org.freedom.infra.api.brasilapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO imutável (Record) representando o retorno de endereço da BrasilAPI / ViaCEP.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EnderecoBrasilApiDto(
    String cep,
    String state,
    String city,
    String neighborhood,
    String street,
    String service
) {
    public String obterEnderecoCompleto() {
        return String.format("%s, %s - %s/%s (CEP: %s)", 
            street != null ? street : "", 
            neighborhood != null ? neighborhood : "", 
            city != null ? city : "", 
            state != null ? state : "", 
            cep != null ? cep : "");
    }
}
