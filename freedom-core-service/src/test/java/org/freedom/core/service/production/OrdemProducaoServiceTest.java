package org.freedom.core.service.production;

import org.freedom.core.domain.production.OrdemProducao;
import org.freedom.core.service.stock.EstoqueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrdemProducaoServiceTest {

    @Mock
    private OrdemProducaoRepository repository;

    @Mock
    private EstoqueService estoqueService;

    private OrdemProducaoService service;

    @BeforeEach
    void setUp() {
        service = new OrdemProducaoService(repository, estoqueService);
    }

    @Test
    @DisplayName("Deve abrir Ordem de Produção com status PLANEJADA")
    void deveAbrirOrdemProducaoValida() {
        OrdemProducao op = new OrdemProducao(1L, "OP-2026-101", 10L, 1L, new BigDecimal("100.0000"));
        when(repository.salvar(any())).thenReturn(op);

        OrdemProducao salva = service.abrirOrdemProducao(op);

        assertThat(salva.getStatusOp()).isEqualTo("PLANEJADA");
        verify(repository).salvar(op);
    }

    @Test
    @DisplayName("Finalização de OP deve dar entrada física no estoque do produto acabado")
    void deveFinalizarOpEDarEntradaNoEstoque() {
        OrdemProducao op = new OrdemProducao(1L, "OP-2026-102", 50L, 1L, new BigDecimal("20.0000"));

        service.finalizarOrdemProducao(op, new BigDecimal("20.0000"), new BigDecimal("45.0000"));

        assertThat(op.getStatusOp()).isEqualTo("FINALIZADA");
        assertThat(op.getQuantidadeProduzida()).isEqualByComparingTo("20.0000");
        verify(estoqueService).darEntradaEstoque(eq(50L), eq(new BigDecimal("20.0000")), eq(new BigDecimal("45.0000")), any(), any());
    }

    @Test
    @DisplayName("Deve rejeitar abertura de OP com quantidade nula ou negativa")
    void deveRejeitarQuantidadeInvalida() {
        OrdemProducao op = new OrdemProducao(1L, "OP-2026-103", 10L, 1L, BigDecimal.ZERO);

        assertThatThrownBy(() -> service.abrirOrdemProducao(op))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("positiva");
    }
}
