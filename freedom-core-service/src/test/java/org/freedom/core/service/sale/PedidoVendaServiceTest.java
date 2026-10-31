package org.freedom.core.service.sale;

import org.freedom.core.domain.sale.ItemPedidoVenda;
import org.freedom.core.domain.sale.PedidoVenda;
import org.freedom.core.service.finance.ContaReceberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoVendaServiceTest {

    @Mock
    private PedidoVendaRepository repository;

    @Mock
    private ContaReceberService financeiroService;

    private PedidoVendaService service;

    @BeforeEach
    void setUp() {
        service = new PedidoVendaService(repository, financeiroService);
    }

    @Test
    @DisplayName("Deve calcular tributos da Reforma 2026 (CBS 0.9% e IBS 0.1%) no total do pedido")
    void deveSalvarPedidoComTributosReforma() {
        PedidoVenda pedido = new PedidoVenda(1L, 10L, "PED-2026-001");
        ItemPedidoVenda item = new ItemPedidoVenda(100L, 1, new BigDecimal("2.0000"), new BigDecimal("500.00")); // R$ 1.000,00

        when(repository.salvarPedido(any(), any())).thenReturn(pedido);

        PedidoVenda salvo = service.salvarPedido(pedido, List.of(item));

        assertThat(salvo).isNotNull();
        assertThat(pedido.getValorTotal()).isEqualByComparingTo("1000.00");
        assertThat(pedido.getValorCbs()).isEqualByComparingTo("9.00");  // 0.9% de 1000
        assertThat(pedido.getValorIbs()).isEqualByComparingTo("1.00");  // 0.1% de 1000
        verify(repository).salvarPedido(pedido, List.of(item));
    }

    @Test
    @DisplayName("Aprovação de pedido deve emitir título no Contas a Receber automaticamente")
    void deveAprovarPedidoEEmitirTitulo() {
        PedidoVenda pedido = new PedidoVenda(1L, 10L, "PED-2026-002");
        pedido.setValorTotal(new BigDecimal("1500.00"));
        pedido.setFormaPagamento("PIX");

        service.aprovarPedido(pedido);

        assertThat(pedido.getStatusPedido()).isEqualTo("APROVADO");
        verify(financeiroService).emitirTitulo(any());
    }

    @Test
    @DisplayName("Deve rejeitar pedido sem itens")
    void deveRejeitarPedidoSemItens() {
        PedidoVenda pedido = new PedidoVenda(1L, 10L, "PED-2026-003");

        assertThatThrownBy(() -> service.salvarPedido(pedido, List.of()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("pelo menos um item");
    }
}

