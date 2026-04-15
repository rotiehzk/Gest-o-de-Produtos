package com.starterkit.springboot.produto;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/produtos")
public class produtoPageController {

    private final produtoService produtoService;

    @Value("${security.api-key.admin:}")
    private String adminKey;

    public produtoPageController(produtoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping
    public String lista(Model model) {
        model.addAttribute("titulo", "Gestao de produtos");
        model.addAttribute("produtos", produtoService.listAll());
        model.addAttribute("pageScript", "/js/produtos.js");
        return "produtos/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("titulo", "Novo produto");
        model.addAttribute("produto", null);
        model.addAttribute("produtoForm", new produtoForm());
        model.addAttribute("modo", "novo");
        model.addAttribute("pageScript", "/js/produtos.js");
        return "produtos/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        produto produto = produtoService.getById(id);
        produtoForm form = new produtoForm();
        copyprodutoToForm(produto, form);
        model.addAttribute("titulo", "Editar produto");
        model.addAttribute("produto", produto);
        model.addAttribute("produtoForm", form);
        model.addAttribute("modo", "editar");
        model.addAttribute("pageScript", "/js/produtos.js");
        return "produtos/form";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        produto produto = produtoService.getById(id);
        model.addAttribute("titulo", "Ficha do produto");
        model.addAttribute("produto", produto);
        model.addAttribute("pageScript", "/js/produtos.js");
        return "produtos/detalhe";
    }

    @GetMapping("/codigo/{codigo}")
    public String detalhePorCodigo(@PathVariable String codigo, Model model) {
        produto produto = produtoService.getByCodigo(codigo);
        model.addAttribute("titulo", "Ficha do produto");
        model.addAttribute("produto", produto);
        model.addAttribute("pageScript", "/js/produtos.js");
        return "produtos/detalhe";
    }

    @GetMapping("/scan")
    public String scan(Model model) {
        model.addAttribute("titulo", "Ler QR Code");
        model.addAttribute("pageScript", "/js/produtos.js");
        return "produtos/scan";
    }

    @PostMapping
    public String criar(@Valid produtoForm produtoForm, BindingResult bindingResult, Model model) {
        if (!isValidAdminKey(produtoForm.getAdminApiKey())) {
            bindingResult.rejectValue("adminApiKey", "adminApiKey.invalid", "Chave de administrador invalida.");
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("titulo", "Novo produto");
            model.addAttribute("produto", null);
            model.addAttribute("modo", "novo");
            model.addAttribute("pageScript", "/js/produtos.js");
            return "produtos/form";
        }

        produtoService.create(produtoForm);
        return "redirect:/produtos?status=created";
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id, @Valid produtoForm produtoForm, BindingResult bindingResult,
            Model model) {
        produto produto = produtoService.getById(id);
        if (!isValidAdminKey(produtoForm.getAdminApiKey())) {
            bindingResult.rejectValue("adminApiKey", "adminApiKey.invalid", "Chave de administrador invalida.");
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("titulo", "Editar produto");
            model.addAttribute("modo", "editar");
            model.addAttribute("produto", produto);
            model.addAttribute("pageScript", "/js/produtos.js");
            return "produtos/form";
        }

        produtoService.update(id, produtoForm);
        return "redirect:/produtos?status=updated";
    }

    private boolean isValidAdminKey(String providedKey) {
        return providedKey != null && !providedKey.trim().isEmpty() && providedKey.equals(adminKey);
    }

    private void copyprodutoToForm(produto produto, produtoForm form) {
        form.setNome(produto.getNome());
        form.setPreco(produto.getPreco());
        form.setFornecedor(produto.getFornecedor());
        form.setQuantidadeStock(produto.getQuantidadeStock());
        form.setCategoria(produto.getCategoria());
        form.setEmPromocao(produto.getEmPromocao());
        form.setPercentagemPromocao(produto.getPercentagemPromocao());
       
    }
}
