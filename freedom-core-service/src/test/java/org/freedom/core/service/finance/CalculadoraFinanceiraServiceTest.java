package org.freedom.core.service.finance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CalculadoraFinanceiraServiceTest {

    private CalculadoraFinanceiraService calculadora;

    @BeforeEach
    void setUp() {
        calculadora = new CalculadoraFinanceiraService();
    }

    @Test
    @DisplayName("Não deve cobrar multa nem juros quando o título está no prazo de vencimento")
    void naoDeveCobrarEncargosEmDia() {
        LocalDate vencimento = LocalDate.of(2026, 10, 31);
        LocalDate pagamento = LocalDate.of(2026, 10, 31);
        BigDecimal nominal = new BigDecimal("1000.00");

        EncargosAtraso resultado = calculadora.calcularEncargosAtraso(nominal, vencimento, pagamento);

        assertThat(resultado.diasAtraso()).isZero();
        assertThat(resultado.valorMulta()).isEqualByComparingTo("0.00");
        assertThat(resultado.valorJuros()).isEqualByComparingTo("0.00");
        assertThat(resultado.valorTotalAtualizado()).isEqualByComparingTo("1000.00");
    }

    @Test
    @DisplayName("Deve calcular corretamente multa de 2% e juros de 1% ao mês para 30 dias de atraso")
    void deveCalcularMultaEJurosPara30Dias() {
        LocalDate vencimento = LocalDate.of(2026, 10, 1);
        LocalDate pagamento = LocalDate.of(2026, 10, 31);
        BigDecimal nominal = new BigDecimal("1000.00");

        EncargosAtraso resultado = calculadora.calcularEncargosAtraso(nominal, vencimento, pagamento);

        assertThat(resultado.diasAtraso()).isEqualTo(30);
        assertThat(resultado.valorMulta()).isEqualByComparingTo("20.00"); // 2% de 1000
        assertThat(resultado.valorJuros()).isEqualByComparingTo("10.00"); // 1% de 1000
        assertThat(resultado.valorTotalAtualizado()).isEqualByComparingTo("1030.00");
    }

    @Test
    @DisplayName("Deve rejeitar valor nominal nulo ou negativo")
    void deveRejeitarValorInvalido() {
        LocalDate hoje = LocalDate.of(2026, 10, 31);
        assertThatThrownBy(() -> calculadora.calcularEncargosAtraso(BigDecimal.ZERO, hoje, hoje))
            .isInstanceOf(IllegalArgumentException.class);
    }
}

