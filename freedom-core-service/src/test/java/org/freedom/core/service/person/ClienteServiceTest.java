package org.freedom.core.service.person;

import org.freedom.core.domain.person.Pessoa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private PessoaRepository repository;

    private ClienteService service;

    @BeforeEach
    void setUp() {
        service = new ClienteService(repository, null);
    }

    @Test
    @DisplayName("Deve salvar cliente com sucesso quando dados válidos")
    void deveSalvarClienteValido() {
        Pessoa p = new Pessoa(1L, "PJ", "Empresa Exemplo S/A", "Exemplo", "12.345.678/0001-90");
        when(repository.salvar(any())).thenReturn(p);

        Pessoa salvo = service.salvarCliente(p);

        assertThat(salvo).isNotNull();
        assertThat(salvo.isEhCliente()).isTrue();
        verify(repository).salvar(p);
    }

    @Test
    @DisplayName("Deve rejeitar cliente com Razão Social vazia")
    void deveRejeitarRazaoSocialVazia() {
        Pessoa p = new Pessoa(1L, "PJ", "  ", "Exemplo", "12.345.678/0001-90");
        assertThatThrownBy(() -> service.salvarCliente(p))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("razão social");
    }

    @Test
    @DisplayName("Deve buscar clientes assincronamente através de Virtual Thread sem exceções")
    void deveBuscarClientesAsync() throws Exception {
        Pessoa p = new Pessoa(1L, "PJ", "Cliente Teste", "Fantasia", "11122233344");
        when(repository.buscarPorTermo("Teste")).thenReturn(List.of(p));

        CompletableFuture<List<Pessoa>> future = service.buscarClientesAsync("Teste");
        List<Pessoa> resultado = future.get();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getRazaoSocial()).isEqualTo("Cliente Teste");
    }
}
