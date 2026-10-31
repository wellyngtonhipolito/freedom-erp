package org.freedom.core.domain.person;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Entidade unificada representando Pessoas (Clientes, Fornecedores, Transportadoras e Vendedores).
 * Mapeada para a tabela std.pessoa no PostgreSQL 16+.
 */
@Entity
@Table(name = "pessoa", schema = "std")
public class Pessoa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "filial_id", nullable = false)
    private Long filialId;

    @Column(name = "tipo_pessoa", length = 2, nullable = false)
    private String tipoPessoa = "PJ"; // 'PF' ou 'PJ'

    @Column(name = "razao_social", length = 120, nullable = false)
    private String razaoSocial;

    @Column(name = "nome_fantasia", length = 80)
    private String nomeFantasia;

    @Column(name = "cpf_cnpj", length = 18, nullable = false)
    private String cpfCnpj;

    @Column(name = "inscricao_estadual", length = 30)
    private String inscricaoEstadual;

    @Column(name = "inscricao_municipal", length = 30)
    private String inscricaoMunicipal;

    @Column(name = "eh_cliente", nullable = false)
    private boolean ehCliente = true;

    @Column(name = "eh_fornecedor", nullable = false)
    private boolean ehFornecedor = false;

    @Column(name = "eh_transportadora", nullable = false)
    private boolean ehTransportadora = false;

    @Column(name = "eh_vendedor", nullable = false)
    private boolean ehVendedor = false;

    // Endereço
    @Column(name = "cep", length = 9)
    private String cep;

    @Column(name = "logradouro", length = 100)
    private String logradouro;

    @Column(name = "numero", length = 20)
    private String numero;

    @Column(name = "complemento", length = 60)
    private String complemento;

    @Column(name = "bairro", length = 60)
    private String bairro;

    @Column(name = "municipio_ibge", length = 7)
    private String municipioIbge;

    @Column(name = "nome_municipio", length = 80)
    private String nomeMunicipio;

    @Column(name = "uf", length = 2)
    private String uf;

    // Contato
    @Column(name = "telefone", length = 20)
    private String telefone;

    @Column(name = "celular_whatsapp", length = 20)
    private String celularWhatsapp;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm = Instant.now();

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm = Instant.now();

    public Pessoa() {
    }

    public Pessoa(Long filialId, String tipoPessoa, String razaoSocial, String nomeFantasia, String cpfCnpj) {
        this.filialId = filialId;
        this.tipoPessoa = tipoPessoa;
        this.razaoSocial = razaoSocial;
        this.nomeFantasia = nomeFantasia;
        this.cpfCnpj = cpfCnpj;
        this.ehCliente = true;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getFilialId() { return filialId; }
    public void setFilialId(Long filialId) { this.filialId = filialId; }

    public String getTipoPessoa() { return tipoPessoa; }
    public void setTipoPessoa(String tipoPessoa) { this.tipoPessoa = tipoPessoa; }

    public String getRazaoSocial() { return razaoSocial; }
    public void setRazaoSocial(String razaoSocial) { this.razaoSocial = razaoSocial; }

    public String getNomeFantasia() { return nomeFantasia; }
    public void setNomeFantasia(String nomeFantasia) { this.nomeFantasia = nomeFantasia; }

    public String getCpfCnpj() { return cpfCnpj; }
    public void setCpfCnpj(String cpfCnpj) { this.cpfCnpj = cpfCnpj; }

    public String getInscricaoEstadual() { return inscricaoEstadual; }
    public void setInscricaoEstadual(String inscricaoEstadual) { this.inscricaoEstadual = inscricaoEstadual; }

    public String getInscricaoMunicipal() { return inscricaoMunicipal; }
    public void setInscricaoMunicipal(String inscricaoMunicipal) { this.inscricaoMunicipal = inscricaoMunicipal; }

    public boolean isEhCliente() { return ehCliente; }
    public void setEhCliente(boolean ehCliente) { this.ehCliente = ehCliente; }

    public boolean isEhFornecedor() { return ehFornecedor; }
    public void setEhFornecedor(boolean ehFornecedor) { this.ehFornecedor = ehFornecedor; }

    public boolean isEhTransportadora() { return ehTransportadora; }
    public void setEhTransportadora(boolean ehTransportadora) { this.ehTransportadora = ehTransportadora; }

    public boolean isEhVendedor() { return ehVendedor; }
    public void setEhVendedor(boolean ehVendedor) { this.ehVendedor = ehVendedor; }

    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }

    public String getLogradouro() { return logradouro; }
    public void setLogradouro(String logradouro) { this.logradouro = logradouro; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getComplemento() { return complemento; }
    public void setComplemento(String complemento) { this.complemento = complemento; }

    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }

    public String getMunicipioIbge() { return municipioIbge; }
    public void setMunicipioIbge(String municipioIbge) { this.municipioIbge = municipioIbge; }

    public String getNomeMunicipio() { return nomeMunicipio; }
    public void setNomeMunicipio(String nomeMunicipio) { this.nomeMunicipio = nomeMunicipio; }

    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getCelularWhatsapp() { return celularWhatsapp; }
    public void setCelularWhatsapp(String celularWhatsapp) { this.celularWhatsapp = celularWhatsapp; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(Instant atualizadoEm) { this.atualizadoEm = atualizadoEm; }
}

