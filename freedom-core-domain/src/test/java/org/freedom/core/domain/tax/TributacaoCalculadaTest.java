package org.freedom.core.domain.tax;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TributacaoCalculadaTest {

    @Test
    @DisplayName("Deve calcular corretamente CBS (0.9%) e IBS (0.1%) da Reforma Tributária 2026")
    void deveCalcularCbsEIbsCorretamente() {
        BigDecimal base = new BigDecimal("1000.00");
        BigDecimal aliquotaIcms = new BigDecimal("0.18"); // 18% ICMS

        TributacaoCalculada tributos = TributacaoCalculada.calcularParaAno2026(base, aliquotaIcms);

        assertThat(tributos.valorIcms()).isEqualByComparingTo("180.00");
        assertThat(tributos.valorCbs()).isEqualByComparingTo("9.00");  // 0.9% de 1000.00
        assertThat(tributos.valorIbs()).isEqualByComparingTo("1.00");  // 0.1% de 1000.00
        assertThat(tributos.totalTributos()).isEqualByComparingTo("190.00");
    }

    @Test
    @DisplayName("Deve rejeitar base de cálculo negativa com exceção amigável")
    void deveRejeitarBaseNegativa() {
        assertThatThrownBy(() -> new TributacaoCalculada(
            new BigDecimal("-10.00"),
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        )).isInstanceOf(IllegalArgumentException.class);
    }
}

