package com.starterkit.springboot.phone;

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
@RequestMapping("/api/phones")
public class PhoneController {

    private final phoneService phoneService;

    public PhoneController(phoneService phoneService) {
        this.phoneService = phoneService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public phone create(@Valid @RequestBody phoneRequest request) {
        return phoneService.create(request);
    }

    @GetMapping
    public List<phone> list() {
        return phoneService.listAll();
    }

    @PostMapping("/teste")
    public List<phone> seed(
            @RequestParam(defaultValue = "5") int count,
            @RequestParam(defaultValue = "false") boolean clear) {
        if (count < 1 || count > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "count deve estar entre 1 e 100");
        }
        if (clear) {
            List<phone> phones = phoneService.listAll();
            for (phone phone : phones) {
                phoneService.delete(phone.getId());
            }
        }
        List<phone> created = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            phoneRequest request = new phoneRequest();
            request.setNome(null);
            request.setPreco(null);
            request.setFornecedor(null);
            request.setQuantidadeStock(null);
            request.setEmPromocao(null);

        }
        return created;
    }

    @GetMapping("/{id}")
    public phone get(@PathVariable Long id) {
        return phoneService.getById(id);
    }

    @GetMapping("/codigo/{codigo}")
    public phone getByCodigo(@PathVariable String codigo) {
        return phoneService.getByCodigo(codigo);
    }

    @PutMapping("/{id}")
    public phone update(@PathVariable Long id, @Valid @RequestBody phoneRequest request) {
        return phoneService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        phoneService.delete(id);
    }
}
