package com.starterkit.springboot.produto;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.persistence.*;
import org.hibernate.annotations.GenericGenerator;


@Entity
@Table(name = "produtos")
public class produto {

    @Id
    @GeneratedValue(generator = "forn-id-gen")
    @GenericGenerator(name = "forn-id-gen", strategy = "increment")
    private Long id;



    @Column(nullable = false, length = 25)
    private String nome;


    @Column(nullable = false, length = 50)
    private Double preco;


    @Column(nullable = false, length = 125)
    private String fornecedor;


    @Column(nullable = false, length = 50)
    private Integer quantidadeStock;

    @Column(nullable = false, length = 25)
    private String categoria;


    @Column(nullable = false, length = 50)
    private Boolean emPromocao = false;

    @Column(nullable = true)
    private Integer percentagemPromocao;

    @Column(nullable = true, length = 100)
    private String codigoUnico;

    @Column(nullable = true, length = 255)
    private String imagemPath;


    public Long getId() { return id; }


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

    public String getCodigoUnico() {
        return codigoUnico;
    }


    public void setCodigoUnico(String codigoUnico) {
        this.codigoUnico = codigoUnico;
    }


    public void setImagemPath(String imagemPath) {
        this.imagemPath = imagemPath;
    }


    public String getImagemPath() {
        return imagemPath;
    }

    public Double getPrecoPromocional() {
        if (preco == null) {
            return null;
        }
        BigDecimal precoBD = BigDecimal.valueOf(preco).setScale(2, RoundingMode.HALF_UP);
        if (emPromocao != null && emPromocao && percentagemPromocao != null) {
            BigDecimal fator = BigDecimal.valueOf(1.0 - percentagemPromocao / 100.0);
            return precoBD.multiply(fator).setScale(2, RoundingMode.HALF_UP).doubleValue();
        }
        return precoBD.doubleValue();
    }
}
