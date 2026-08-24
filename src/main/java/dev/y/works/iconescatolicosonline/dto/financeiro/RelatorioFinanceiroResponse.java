package dev.y.works.iconescatolicosonline.dto.financeiro;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RelatorioFinanceiroResponse(
        LocalDate inicio,
        LocalDate fim,
        int quantidadeVendas,
        int quantidadeGastos,
        BigDecimal receitaTotal,
        BigDecimal gastosDoPeriodo,
        BigDecimal lucroBrutoTotal,
        BigDecimal lucroLiquidoEstimadoTotal,
        BigDecimal resultadoDoPeriodo,
        List<VendaResponse> vendas,
        List<GastoResponse> gastos
) {
}
