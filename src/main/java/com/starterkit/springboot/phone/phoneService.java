package com.starterkit.springboot.phone;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.http.HttpStatus;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class phoneService {

    private final phoneRepository repo;
    private final Path phonesUploadDir;

    public phoneService(phoneRepository repo, @Value("${app.upload-dir:./uploads}") String uploadDir) {
        this.repo = repo;
        this.phonesUploadDir = Paths.get(uploadDir).toAbsolutePath().normalize().resolve("phones");
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(phonesUploadDir);
        } catch (IOException ex) {
            throw new IllegalStateException("Nao foi possivel criar a pasta de uploads", ex);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void fillMissingCodigoUnico() {
        List<phone> phones = repo.findAll();
        boolean changed = false;
        for (phone phone : phones) {
            if (!StringUtils.hasText(phone.getCodigoUnico())) {
                phone.setCodigoUnico(UUID.randomUUID().toString());
                changed = true;
            }
        }
        if (changed) {
            repo.saveAll(phones);
        }
    }

    public List<phone> listAll() {
        return repo.findAll();
    }

    public phone getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "phone nao encontrado"));
    }

    public phone getByCodigo(String codigo) {
        return repo.findByCodigoUnico(codigo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "phone nao encontrado"));
    }

    public phone create(phoneRequest request) {
        phone phone = new phone();
        applyRequest(phone, request);
        return repo.save(phone);
    }

    public phone create(phoneForm form) {
        phone phone = new phone();
        applyRequest(phone, form);
        phone.setImagemPath(storeImage(form.getImagem(), null));
        return repo.save(phone);
    }

    public phone update(Long id, phoneRequest request) {
        phone phone = getById(id);
        applyRequest(phone, request);
        return repo.save(phone);
    }

    public phone update(Long id, phoneForm form) {
        phone phone = getById(id);
        applyRequest(phone, form);
        phone.setImagemPath(storeImage(form.getImagem(), phone.getImagemPath()));
        return repo.save(phone);
    }

    public void delete(Long id) {
        phone phone = getById(id);
        deleteStoredImage(phone.getImagemPath());
        repo.delete(phone);
    }

    private void applyRequest(phone phone, phoneRequest request) {
        phone.setNome(request.getNome());
        phone.setPreco(request.getPreco());
        phone.setFornecedor(request.getFornecedor());
        phone.setQuantidadeStock(request.getQuantidadeStock());
        phone.setCategoria(request.getCategoria());
        phone.setEmPromocao(request.getEmPromocao());
        phone.setPercentagemPromocao(request.getPercentagemPromocao());

    }

    private String storeImage(MultipartFile imagem, String currentImagePath) {
        if (imagem == null || imagem.isEmpty()) {
            return currentImagePath;
        }

        String originalName = StringUtils.cleanPath(imagem.getOriginalFilename());
        String extension = getExtension(originalName);
        if (!isAllowedImageExtension(extension)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de imagem nao suportado");
        }

        String generatedName = UUID.randomUUID().toString() + extension;
        Path destination = phonesUploadDir.resolve(generatedName).normalize();
        if (!destination.startsWith(phonesUploadDir)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome de ficheiro invalido");
        }

        try (InputStream inputStream = imagem.getInputStream()) {
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Falha ao guardar a imagem");
        }

        deleteStoredImage(currentImagePath);
        return "phones/" + generatedName;
    }

    private void deleteStoredImage(String imagePath) {
        if (!StringUtils.hasText(imagePath)) {
            return;
        }

        String relativePath = imagePath.replace('/', java.io.File.separatorChar);
        Path filePath = phonesUploadDir.getParent().resolve(relativePath).normalize();
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Falha ao remover a imagem");
        }
    }

    private String getExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index < 0) {
            return "";
        }
        return fileName.substring(index).toLowerCase(Locale.ROOT);
    }

    private boolean isAllowedImageExtension(String extension) {
        return ".png".equals(extension)
                || ".jpg".equals(extension)
                || ".jpeg".equals(extension)
                || ".webp".equals(extension)
                || ".gif".equals(extension);
    }
}
