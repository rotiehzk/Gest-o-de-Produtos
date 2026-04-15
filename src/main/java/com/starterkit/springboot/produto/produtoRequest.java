package com.starterkit.springboot.produto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;

public class produtoRequest {
    @NotBlank(message = "Nome é obrigatório")
    private String nome;
    
    @NotBlank(message = "Fornecedor é obrigatório")
    private String fornecedor;
    
    @NotBlank(message = "Categoria é obrigatória")
    private String categoria;
    
    @NotNull(message = "Quantidade Stock é obrigatória e deve ser um número inteiro")
    @Min(value = 0, message = "Quantidade Stock não pode ser negativa")
    private Integer quantidadeStock;
    
    @NotNull(message = "Preço é obrigatório e deve ser um número decimal")
    @DecimalMin(value = "0.0", message = "Preço não pode ser negativo")
    private Double preco;
    
    private Boolean emPromocao;

    @Min(value = 0, message = "Percentagem não pode ser negativa")
    @Max(value = 100, message = "Percentagem não pode ser maior que 100")
    private Integer percentagemPromocao;




    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Double getPreco() { return preco; }
    public void setPreco(Double preco) { this.preco = preco; }

    public String getFornecedor() { return fornecedor; }
    public void setFornecedor(String fornecedor) { this.fornecedor = fornecedor; }
    
    public Integer getQuantidadeStock() { return quantidadeStock; }
    public void setQuantidadeStock(Integer quantidadeStock) { this.quantidadeStock = quantidadeStock; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public Boolean getEmPromocao() { return emPromocao; }
    public void setEmPromocao(Boolean emPromocao) { this.emPromocao = emPromocao; }

    public Integer getPercentagemPromocao() { return percentagemPromocao; }
    public void setPercentagemPromocao(Integer percentagemPromocao) { this.percentagemPromocao = percentagemPromocao; }



 
}