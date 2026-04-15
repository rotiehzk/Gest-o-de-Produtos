package com.starterkit.springboot.phone;

import org.springframework.web.multipart.MultipartFile;
import javax.validation.constraints.NotBlank;

public class phoneForm extends phoneRequest {

    private MultipartFile imagem;

    @NotBlank(message = "Chave de administrador é obrigatória")
    private String adminApiKey;

    public MultipartFile getImagem() {
        return imagem;
    }

    public void setImagem(MultipartFile imagem) {
        this.imagem = imagem;
    }

    public String getAdminApiKey() {
        return adminApiKey;
    }

    public void setAdminApiKey(String adminApiKey) {
        this.adminApiKey = adminApiKey;
    }
}
