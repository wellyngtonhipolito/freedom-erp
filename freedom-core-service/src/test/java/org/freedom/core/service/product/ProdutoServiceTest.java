package org.freedom.core.service.product;

import org.freedom.core.domain.product.Produto;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository repository;

    private ProdutoService service;

    @BeforeEach
    void setUp() {
        service = new ProdutoService(repository);
    }

    @Test
    @DisplayName("Deve calcular markup de 100% e salvar produto válido com NCM de 8 dígitos")
    void deveSalvarProdutoValido() {
        Produto p = new Produto(1L, "PROD-001", "Cabo de Rede Cat6 305m", "CX",
            new BigDecimal("200.00"), new BigDecimal("400.00"), "85444900");

        when(repository.salvar(any())).thenReturn(p);

        Produto salvo = service.salvarProduto(p);

        assertThat(salvo).isNotNull();
        assertThat(salvo.getMarkupPercentual()).isEqualByComparingTo("100.00");
        verify(repository).salvar(p);
    }

    @Test
    @DisplayName("Deve rejeitar produto com NCM inválido (diferente de 8 dígitos)")
    void deveRejeitarNcmInvalido() {
        Produto p = new Produto(1L, "PROD-002", "Teclado USB", "UN",
            new BigDecimal("50.00"), new BigDecimal("80.00"), "123");

        assertThatThrownBy(() -> service.salvarProduto(p))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("NCM deve conter exatamente 8 dígitos");
    }
}

