package com.starterkit.springboot.produto;

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
public class produtoService {

    private final produtoRepository repo;
    private final Path produtosUploadDir;

    public produtoService(produtoRepository repo, @Value("${app.upload-dir:./uploads}") String uploadDir) {
        this.repo = repo;
        this.produtosUploadDir = Paths.get(uploadDir).toAbsolutePath().normalize().resolve("produtos");
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(produtosUploadDir);
        } catch (IOException ex) {
            throw new IllegalStateException("Nao foi possivel criar a pasta de uploads", ex);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void fillMissingCodigoUnico() {
        List<produto> produtos = repo.findAll();
        boolean changed = false;
        for (produto produto : produtos) {
            if (!StringUtils.hasText(produto.getCodigoUnico())) {
                produto.setCodigoUnico(UUID.randomUUID().toString());
                changed = true;
            }
        }
        if (changed) {
            repo.saveAll(produtos);
        }
    }

    public List<produto> listAll() {
        return repo.findAll();
    }

    public produto getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "produto nao encontrado"));
    }

    public produto getByCodigo(String codigo) {
        return repo.findByCodigoUnico(codigo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "produto nao encontrado"));
    }

    public produto create(produtoRequest request) {
        produto produto = new produto();
        applyRequest(produto, request);
        return repo.save(produto);
    }

    public produto create(produtoForm form) {
        produto produto = new produto();
        applyRequest(produto, form);
        produto.setImagemPath(storeImage(form.getImagem(), null));
        return repo.save(produto);
    }

    public produto update(Long id, produtoRequest request) {
        produto produto = getById(id);
        applyRequest(produto, request);
        return repo.save(produto);
    }

    public produto update(Long id, produtoForm form) {
        produto produto = getById(id);
        applyRequest(produto, form);
        produto.setImagemPath(storeImage(form.getImagem(), produto.getImagemPath()));
        return repo.save(produto);
    }

    public void delete(Long id) {
        produto produto = getById(id);
        deleteStoredImage(produto.getImagemPath());
        repo.delete(produto);
    }

    private void applyRequest(produto produto, produtoRequest request) {
        produto.setNome(request.getNome());
        produto.setPreco(request.getPreco());
        produto.setFornecedor(request.getFornecedor());
        produto.setQuantidadeStock(request.getQuantidadeStock());
        produto.setCategoria(request.getCategoria());
        produto.setEmPromocao(request.getEmPromocao());
        produto.setPercentagemPromocao(request.getPercentagemPromocao());

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
        Path destination = produtosUploadDir.resolve(generatedName).normalize();
        if (!destination.startsWith(produtosUploadDir)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome de ficheiro invalido");
        }

        try (InputStream inputStream = imagem.getInputStream()) {
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Falha ao guardar a imagem");
        }

        deleteStoredImage(currentImagePath);
        return "produtos/" + generatedName;
    }

    private void deleteStoredImage(String imagePath) {
        if (!StringUtils.hasText(imagePath)) {
            return;
        }

        String relativePath = imagePath.replace('/', java.io.File.separatorChar);
        Path filePath = produtosUploadDir.getParent().resolve(relativePath).normalize();
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
