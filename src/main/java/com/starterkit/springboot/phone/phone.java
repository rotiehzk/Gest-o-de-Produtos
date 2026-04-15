package com.starterkit.springboot.phone;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.persistence.*;
import org.hibernate.annotations.GenericGenerator;


@Entity
@Table(name = "phones")
public class phone {

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
        if (preco == null || !Double.isFinite(preco)) {
            return null;
        }
        if (emPromocao != null && emPromocao && percentagemPromocao != null && percentagemPromocao > 0) {
            double fator = 1.0 - (percentagemPromocao.doubleValue() / 100.0);
            return preco * fator;
        }
        return preco;
    }

}
