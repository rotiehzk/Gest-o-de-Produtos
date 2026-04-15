package com.starterkit.springboot.produto;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final produtoService produtoService;

    public ProdutoController(produtoService produtoService) {
        this.produtoService = produtoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public produto create(@Valid @RequestBody produtoRequest request) {
        return produtoService.create(request);
    }

    @GetMapping
    public List<produto> list() {
        return produtoService.listAll();
    }

    @PostMapping("/teste")
    public List<produto> seed(
            @RequestParam(defaultValue = "5") int count,
            @RequestParam(defaultValue = "false") boolean clear) {
        if (count < 1 || count > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "count deve estar entre 1 e 100");
        }
        if (clear) {
            List<produto> produtos = produtoService.listAll();
            for (produto produto : produtos) {
                produtoService.delete(produto.getId());
            }
        }
        List<produto> created = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            produtoRequest request = new produtoRequest();
            request.setNome(null);
            request.setPreco(null);
            request.setFornecedor(null);
            request.setQuantidadeStock(null);
            request.setEmPromocao(null);

        }
        return created;
    }

    @GetMapping("/{id}")
    public produto get(@PathVariable Long id) {
        return produtoService.getById(id);
    }

    @GetMapping("/codigo/{codigo}")
    public produto getByCodigo(@PathVariable String codigo) {
        return produtoService.getByCodigo(codigo);
    }

    @PutMapping("/{id}")
    public produto update(@PathVariable Long id, @Valid @RequestBody produtoRequest request) {
        return produtoService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        produtoService.delete(id);
    }
}
