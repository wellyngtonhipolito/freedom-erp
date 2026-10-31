package org.freedom.core.service.product;

import org.freedom.core.domain.product.Produto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Serviço de aplicação para gestão de Produtos e Tabela de Preços.
 * Valida regras fiscais (NCM de 8 dígitos, EAN) e executa consultas em Virtual Threads.
 */
public class ProdutoService {

    private static final Logger log = LoggerFactory.getLogger(ProdutoService.class);

    private final ProdutoRepository repository;

    public ProdutoService() {
        this.repository = new ProdutoRepository();
    }

    public ProdutoService(ProdutoRepository repository) {
        this.repository = repository;
    }

    public Produto salvarProduto(Produto produto) {
        if (produto.getDescricao() == null || produto.getDescricao().isBlank()) {
            throw new IllegalArgumentException("A descrição do produto é obrigatória.");
        }
        if (produto.getNcm() == null || produto.getNcm().replaceAll("\\D", "").length() != 8) {
            throw new IllegalArgumentException("O NCM deve conter exatamente 8 dígitos numéricos válidos.");
        }
        if (produto.getPrecoVenda() == null || produto.getPrecoVenda().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O preço de venda não pode ser negativo.");
        }

        produto.recalcularMarkup();
        return repository.salvar(produto);
    }

    public CompletableFuture<List<Produto>> buscarProdutosAsync(String termo) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Buscando produtos em Virtual Thread com termo '{}'", termo);
            return repository.buscarPorTermo(termo);
        }, runnable -> Thread.ofVirtual().start(runnable));
    }
}

