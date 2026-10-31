package org.freedom.core.service.finance;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Record DTO com o resultado dos encargos financeiros calculados por atraso.
 */
public record EncargosAtraso(
    long diasAtraso,
    BigDecimal valorMulta,
    BigDecimal valorJuros,
    BigDecimal valorTotalAtualizado
) {}

