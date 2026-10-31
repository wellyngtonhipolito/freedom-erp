package org.freedom.core.service.stock;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calculadora pura do Custo Médio Ponderado (CMP) para entradas de mercadorias.
 * Regra: (SaldoAtual * CustoAtual + QtdEntrada * CustoEntrada) / (SaldoAtual + QtdEntrada)
 */
public final class CalculadoraCustoMedio {

    private CalculadoraCustoMedio() {}

    public static BigDecimal calcularNovoCustoMedio(
        BigDecimal saldoQtdAtual,
        BigDecimal custoMedioAtual,
        BigDecimal qtdEntrada,
        BigDecimal custoEntrada
    ) {
        if (qtdEntrada == null || qtdEntrada.compareTo(BigDecimal.ZERO) <= 0) {
            return custoMedioAtual != null ? custoMedioAtual : BigDecimal.ZERO;
        }

        BigDecimal qtdAtual = (saldoQtdAtual != null && saldoQtdAtual.compareTo(BigDecimal.ZERO) > 0)
            ? saldoQtdAtual : BigDecimal.ZERO;
        BigDecimal custoAtual = (custoMedioAtual != null && custoMedioAtual.compareTo(BigDecimal.ZERO) > 0)
            ? custoMedioAtual : BigDecimal.ZERO;
        BigDecimal custoEnt = custoEntrada != null ? custoEntrada : BigDecimal.ZERO;

        BigDecimal valorEstoqueAtual = qtdAtual.multiply(custoAtual);
        BigDecimal valorNovaEntrada = qtdEntrada.multiply(custoEntrada);
        BigDecimal valorTotal = valorEstoqueAtual.add(valorNovaEntrada);
        BigDecimal novaQuantidadeTotal = qtdAtual.add(qtdEntrada);

        if (novaQuantidadeTotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return valorTotal.divide(novaQuantidadeTotal, 4, RoundingMode.HALF_UP);
    }
}
