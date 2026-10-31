package org.freedom.core.service.stock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CalculadoraCustoMedioTest {

    @Test
    @DisplayName("Deve calcular corretamente o Custo Médio Ponderado após entrada de mercadoria")
    void deveCalcularCustoMedioCorretamente() {
        // Exemplo: 10 unidades a R$ 100,00 = R$ 1.000,00
        // Entrada: 10 unidades a R$ 200,00 = R$ 2.000,00
        // Total: 20 unidades por R$ 3.000,00 -> Novo Custo Médio = R$ 150,00
        BigDecimal saldoQtd = new BigDecimal("10.0000");
        BigDecimal custoAtual = new BigDecimal("100.0000");
        BigDecimal entradaQtd = new BigDecimal("10.0000");
        BigDecimal custoEntrada = new BigDecimal("200.0000");

        BigDecimal novoCusto = CalculadoraCustoMedio.calcularNovoCustoMedio(saldoQtd, custoAtual, entradaQtd, custoEntrada);

        assertThat(novoCusto).isEqualByComparingTo("150.0000");
    }

    @Test
    @DisplayName("Primeira entrada de produto com estoque zerado deve adotar o custo de entrada")
    void deveAdotarCustoEntradaEmEstoqueZero() {
        BigDecimal novoCusto = CalculadoraCustoMedio.calcularNovoCustoMedio(
            BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("50.0000"), new BigDecimal("32.5000")
        );

        assertThat(novoCusto).isEqualByComparingTo("32.5000");
    }
}

