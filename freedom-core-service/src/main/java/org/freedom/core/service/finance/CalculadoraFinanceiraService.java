package org.freedom.core.service.finance;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Serviço puro de cálculos financeiros (Juros diários de mora e Multa contratual).
 * Desenvolvido sob rigoroso TDD e precisão monetária (BigDecimal).
 */
public class CalculadoraFinanceiraService {

    public static final BigDecimal MULTA_PADRAO_PERCENTUAL = new BigDecimal("0.02"); // 2% de multa contratual (Código de Defesa do Consumidor)
    public static final BigDecimal JUROS_MENSAL_PADRAO_PERCENTUAL = new BigDecimal("0.01"); // 1% ao mês (0.0333% ao dia)

    /**
     * Calcula multa e juros pro-rata die para títulos vencidos.
     *
     * @param valorNominal Valor original do título
     * @param dataVencimento Data em que o título deveria ter sido pago
     * @param dataCalculo Data de referência do cálculo (ou pagamento)
     * @return EncargosAtraso detalhados
     */
    public EncargosAtraso calcularEncargosAtraso(BigDecimal valorNominal, LocalDate dataVencimento, LocalDate dataCalculo) {
        if (valorNominal == null || valorNominal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor nominal deve ser positivo.");
        }
        if (dataVencimento == null || dataCalculo == null) {
            throw new IllegalArgumentException("As datas de vencimento e cálculo são obrigatórias.");
        }

        long diasAtraso = ChronoUnit.DAYS.between(dataVencimento, dataCalculo);

        // Se não houver atraso, não incidem juros nem multa
        if (diasAtraso <= 0) {
            return new EncargosAtraso(0, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                valorNominal.setScale(2, RoundingMode.HALF_UP));
        }

        // 1. Multa fixa sobre o valor nominal (2%)
        BigDecimal multa = valorNominal.multiply(MULTA_PADRAO_PERCENTUAL).setScale(2, RoundingMode.HALF_UP);

        // 2. Juros de mora diários (1% ao mês / 30 dias = 0.033333% ao dia)
        BigDecimal taxaDiaria = JUROS_MENSAL_PADRAO_PERCENTUAL.divide(new BigDecimal("30"), 8, RoundingMode.HALF_UP);
        BigDecimal juros = valorNominal.multiply(taxaDiaria).multiply(BigDecimal.valueOf(diasAtraso)).setScale(2, RoundingMode.HALF_UP);

        // 3. Valor Total Atualizado
        BigDecimal total = valorNominal.add(multa).add(juros).setScale(2, RoundingMode.HALF_UP);

        return new EncargosAtraso(diasAtraso, multa, juros, total);
    }
}

