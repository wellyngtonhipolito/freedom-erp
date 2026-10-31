package org.freedom.core.service.finance;

import org.freedom.core.domain.finance.ContaReceber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContaReceberServiceTest {

    @Mock
    private ContaReceberRepository repository;

    @Mock
    private CalculadoraFinanceiraService calculadora;

    private ContaReceberService service;

    @BeforeEach
    void setUp() {
        service = new ContaReceberService(repository, calculadora);
    }

    @Test
    @DisplayName("Deve emitir título a receber com status PENDENTE e saldo igual ao nominal")
    void deveEmitirTituloComSucesso() {
        LocalDate emissao = LocalDate.of(2026, 10, 1);
        LocalDate vencimento = LocalDate.of(2026, 10, 31);
        BigDecimal valor = new BigDecimal("2500.00");

        ContaReceber novo = new ContaReceber(1L, 10L, "DOC-9988", emissao, vencimento, valor);
        when(repository.salvar(any())).thenReturn(novo);

        ContaReceber emitido = service.emitirTitulo(novo);

        assertThat(emitido.getStatusTitulo()).isEqualTo("PENDENTE");
        assertThat(emitido.getSaldoDevedor()).isEqualByComparingTo("2500.00");
        verify(repository).salvar(novo);
    }

    @Test
    @DisplayName("Deve rejeitar emissão de título com valor zero ou negativo")
    void deveRejeitarValorInvalido() {
        LocalDate emissao = LocalDate.of(2026, 10, 1);
        LocalDate vencimento = LocalDate.of(2026, 10, 31);

        ContaReceber invalido = new ContaReceber(1L, 10L, "DOC-9988", emissao, vencimento, BigDecimal.ZERO);

        assertThatThrownBy(() -> service.emitirTitulo(invalido))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("maior que zero");
    }
}
