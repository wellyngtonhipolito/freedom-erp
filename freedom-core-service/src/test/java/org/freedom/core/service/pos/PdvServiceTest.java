package org.freedom.core.service.pos;

import org.freedom.core.domain.pos.CaixaSessao;
import org.freedom.core.domain.pos.VendaPdv;
import org.freedom.core.service.stock.EstoqueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PdvServiceTest {

    @Mock
    private PdvRepository repository;

    @Mock
    private EstoqueService estoqueService;

    private PdvService service;

    @BeforeEach
    void setUp() {
        service = new PdvService(repository, estoqueService);
    }

    @Test
    @DisplayName("Deve registrar venda rápida com troco exato e tributos 2026")
    void deveRegistrarVendaComTrocoETributos() {
        BigDecimal total = new BigDecimal("80.00");
        BigDecimal pago = new BigDecimal("100.00"); // Troco deve ser R$ 20,00

        VendaPdv venda = service.registrarVendaRapida(
            1L, "CUPOM-101", total, pago, "DINHEIRO", List.of(5L)
        );

        assertThat(venda.getValorTroco()).isEqualByComparingTo("20.00");
        assertThat(venda.getValorCbs()).isEqualByComparingTo("0.72"); // 0.9% de 80.00
        assertThat(venda.getValorIbs()).isEqualByComparingTo("0.08"); // 0.1% de 80.00
        verify(repository).registrarVenda(any());
        verify(estoqueService).darSaidaEstoque(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Deve rejeitar venda com valor pago menor que o total")
    void deveRejeitarPagamentoInsuficiente() {
        assertThatThrownBy(() -> service.registrarVendaRapida(
            1L, "CUPOM-102", new BigDecimal("50.00"), new BigDecimal("40.00"), "DINHEIRO", null
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("insuficiente");
    }

    @Test
    @DisplayName("Não deve permitir abrir caixa se já houver sessão aberta para o terminal")
    void naoDevePermitirDoisCaixasAbertosNoMesmoTerminal() {
        when(repository.buscarCaixaAberto(1)).thenReturn(Optional.of(new CaixaSessao()));

        assertThatThrownBy(() -> service.abrirCaixa(1, "Caixa 01", new BigDecimal("100.00")))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Já existe uma sessão");
    }
}
