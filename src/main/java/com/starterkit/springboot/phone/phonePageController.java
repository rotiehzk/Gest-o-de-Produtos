package com.starterkit.springboot.phone;

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
@RequestMapping("phones")
public class phonePageController {

    private final phoneService phoneService;

    @Value("${security.api-key.admin:}")
    private String adminKey;

    public phonePageController(phoneService phoneService) {
        this.phoneService = phoneService;
    }

    @GetMapping
    public String lista(Model model) {
        model.addAttribute("titulo", "Gestao de phones");
        model.addAttribute("phones", phoneService.listAll());
        model.addAttribute("pageScript", "/js/phones.js");
        return "phones/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("titulo", "Novo phone");
        model.addAttribute("phone", null);
        model.addAttribute("phoneForm", new phoneForm());
        model.addAttribute("modo", "novo");
        model.addAttribute("pageScript", "/js/phones.js");
        return "phones/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        phone phone = phoneService.getById(id);
        phoneForm form = new phoneForm();
        copyphoneToForm(phone, form);
        model.addAttribute("titulo", "Editar phone");
        model.addAttribute("phone", phone);
        model.addAttribute("phoneForm", form);
        model.addAttribute("modo", "editar");
        model.addAttribute("pageScript", "/js/phones.js");
        return "phones/form";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        phone phone = phoneService.getById(id);
        model.addAttribute("titulo", "Ficha do phone");
        model.addAttribute("phone", phone);
        model.addAttribute("pageScript", "/js/phones.js");
        return "phones/detalhe";
    }

    @GetMapping("/{id}/imprimir")
    public String imprimir(@PathVariable Long id, Model model) {
        phone phone = phoneService.getById(id);
        model.addAttribute("phone", phone);
        return "phones/imprimir";
    }

    @GetMapping("/codigo/{codigo}")
    public String detalhePorCodigo(@PathVariable String codigo, Model model) {
        phone phone = phoneService.getByCodigo(codigo);
        model.addAttribute("titulo", "Ficha do phone");
        model.addAttribute("phone", phone);
        model.addAttribute("pageScript", "/js/phones.js");
        return "phones/detalhe";
    }

    @GetMapping("/scan")
    public String scan(Model model) {
        model.addAttribute("titulo", "Ler QR Code");
        model.addAttribute("pageScript", "/js/phones.js");
        return "phones/scan";
    }

    @PostMapping
    public String criar(@Valid phoneForm phoneForm, BindingResult bindingResult, Model model) {
        if (!isValidAdminKey(phoneForm.getAdminApiKey())) {
            bindingResult.rejectValue("adminApiKey", "adminApiKey.invalid", "Chave de administrador invalida.");
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("titulo", "Novo phone");
            model.addAttribute("phone", null);
            model.addAttribute("modo", "novo");
            model.addAttribute("pageScript", "/js/phones.js");
            return "phones/form";
        }

        phoneService.create(phoneForm);
        return "redirect:/phones?status=created";
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id, @Valid phoneForm phoneForm, BindingResult bindingResult,
            Model model) {
        phone phone = phoneService.getById(id);
        if (!isValidAdminKey(phoneForm  .getAdminApiKey())) {
            bindingResult.rejectValue("adminApiKey", "adminApiKey.invalid", "Chave de administrador invalida.");
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("titulo", "Editar phone");
            model.addAttribute("modo", "editar");
            model.addAttribute("phone", phone);
            model.addAttribute("pageScript", "/js/phones.js");
            return "phones/form";
        }

        phoneService.update(id, phoneForm);
        return "redirect:/phones?status=updated";
    }

    private boolean isValidAdminKey(String providedKey) {
        return providedKey != null && !providedKey.trim().isEmpty() && providedKey.equals(adminKey);
    }

    private void copyphoneToForm(phone phone, phoneForm form) {
        form.setNome(phone.getNome());
        form.setPreco(phone.getPreco());
        form.setFornecedor(phone.getFornecedor());
        form.setQuantidadeStock(phone.getQuantidadeStock());
        form.setCategoria(phone.getCategoria());
        form.setEmPromocao(phone.getEmPromocao());
        form.setPercentagemPromocao(phone.getPercentagemPromocao());
       
    }
}
