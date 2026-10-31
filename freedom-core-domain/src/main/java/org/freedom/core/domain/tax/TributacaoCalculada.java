package org.freedom.core.domain.tax;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Record imutável que representa o cálculo dos tributos federais e subnacionais.
 * Contempla o regime tradicional (ICMS, PIS, COFINS) e a fase de transição
 * da Reforma Tributária 2026 (CBS teste 0.9%, IBS teste 0.1% e Imposto Seletivo).
 */
public record TributacaoCalculada(
    BigDecimal baseCalculo,
    BigDecimal valorIcms,
    BigDecimal valorPis,
    BigDecimal valorCofins,
    BigDecimal aliquotaCbs,
    BigDecimal valorCbs,
    BigDecimal aliquotaIbs,
    BigDecimal valorIbs,
    BigDecimal valorImpostoSeletivo,
    BigDecimal totalTributos
) {
    public TributacaoCalculada {
        if (baseCalculo == null || baseCalculo.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("A base de cálculo não pode ser nula ou negativa.");
        }
    }

    /**
     * Calcula os tributos da fase de transição de 2026 com base na EC 132/2023.
     * Alíquotas de teste padrão: CBS = 0.9%, IBS = 0.1%.
     */
    public static TributacaoCalculada calcularParaAno2026(BigDecimal baseCalculo, BigDecimal aliquotaIcms) {
        BigDecimal aliquotaCbs = new BigDecimal("0.009"); // 0.9%
        BigDecimal aliquotaIbs = new BigDecimal("0.001"); // 0.1%

        BigDecimal icms = baseCalculo.multiply(aliquotaIcms).setScale(2, RoundingMode.HALF_UP);
        BigDecimal cbs = baseCalculo.multiply(aliquotaCbs).setScale(2, RoundingMode.HALF_UP);
        BigDecimal ibs = baseCalculo.multiply(aliquotaIbs).setScale(2, RoundingMode.HALF_UP);

        BigDecimal total = icms.add(cbs).add(ibs);

        return new TributacaoCalculada(
            baseCalculo,
            icms,
            BigDecimal.ZERO, // PIS compensado na transição de 2026
            BigDecimal.ZERO, // COFINS compensado na transição de 2026
            aliquotaCbs,
            cbs,
            aliquotaIbs,
            ibs,
            BigDecimal.ZERO,
            total
        );
    }
}

