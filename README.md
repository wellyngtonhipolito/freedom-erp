# Freedom ERP — Modernização & Arquitetura

[![Java](https://img.shields.io/badge/Java-25%20LTS-orange.svg)](https://openjdk.org/)
[![Database](https://img.shields.io/badge/Database-PostgreSQL%2016-blue.svg)](https://www.postgresql.org/)
[![UI](https://img.shields.io/badge/UI-Java%20Swing%20%2B%20FlatLaf-green.svg)](https://www.formdev.com/flatlaf/)
[![ORM](https://img.shields.io/badge/Persistence-JPA%203.2%20%2F%20Hibernate%206-red.svg)](https://hibernate.org/)
[![Container](https://img.shields.io/badge/Docker-Ready-2496ED.svg)](https://www.docker.com/)
[![Build](https://img.shields.io/badge/Build-Maven%203.9%2B-C71A36.svg)](https://maven.apache.org/)

O **Freedom ERP** é um robusto sistema integrado de gestão empresarial de código aberto, originalmente desenvolvido em Java Swing e com arquitetura em 2 camadas. Este repositório concentra a iniciativa de modernização tecnológica integral do sistema para os padrões contemporâneos de engenharia de software corporativo.

---

## 🎯 Diretrizes do Projeto de Modernização

* **Linguagem & Runtime:** Migração de Java 1.5/1.6 para **Java 25 LTS** (Virtual Threads, Records, Pattern Matching, Sealed Classes).
* **Interface Gráfica (Desktop):** Continuidade em **Java Swing**, porém modernizado com **FlatLaf** (temas claro/escuro, renderização vetorial SVG, suporte HiDPI 4K) e novos componentes customizados (*ModernDataTable*, *ModernSidebar*, *KPI Cards*).
* **Banco de Dados:** Migração completa do Firebird 1.5/2.5 para **PostgreSQL 16/17** (eliminação de UDFs proprietárias em C, adoção de Sequences, Tipos Nativos e Flyway).
* **Infraestrutura:** Orquestração de banco e ferramentas administrativas via **Docker & Docker Compose**.
* **Persistência de Dados:** Transição para **JPA 3.2 (Jakarta Persistence) e Hibernate 6.6+**, eliminando SQLs dinâmicos diretos na camada visual.
* **Build & Dependências:** Substituição dos JARs manuais e projetos Eclipse por uma estrutura **Maven Multi-Módulos**.
* **Qualidade & Metodologia:** Adoção de **TDD (Test-Driven Development)** com JUnit 5, Mockito e Testcontainers; **SDD (Specification-Driven Development)** com Spec Kit e Guardrails arquiteturais com **ArchUnit**.
* **Integração com APIs Externas:** Consumo de APIs modernas (BrasilAPI, ViaCEP, Nuvem Fiscal/Focus NFe, Pix Open Finance) para eliminar dezenas de milhares de linhas de código legado.

---

## 📂 Estrutura dos Módulos

| Módulo Original | Classes | LOC | Papel no Sistema | Status na Modernização |
| :--- | :---: | :---: | :--- | :--- |
| `freedom` | 870 | 345k | Núcleo da aplicação, telas de negócio e relatórios | Migração gradual de telas e separação em serviços |
| `freedom-persist` | 839 | 166k | Mapeamentos JPA/Hibernate de todas as tabelas | Atualização para Jakarta Persistence 3.2 |
| `freedom-fw1` | 205 | 68k | Framework RAD base (`FDados`, `ListaCampos`) | Desacoplamento de UI e banco de dados |
| `freedom-infra` | 73 | 10k | Utilitários de infraestrutura, criptografia e IO | Modernização para APIs padrão do Java 25 |
| `freedom-ecf` | 46 | 13k | Drivers para impressoras fiscais seriais (ECF) | Obsoleto; substituído por emissão NFC-e/NFe via API |
| `freedom-pool` | 8 | 1.6k | Gerenciamento e pool de conexões com TopLink | Substituição integral pelo HikariCP |
| `freedom-nfe` | 7 | 700 | Módulo inicial de NF-e | Modernizado via APIs REST fiscais |

---

## 🚀 Ambiente de Desenvolvimento com Docker

Para iniciar a instância do PostgreSQL 16 e a interface do pgAdmin 4:

```bash
# Subir os containers do banco de dados e pgAdmin
docker compose -f docker/docker-compose.yml up -d

# Verificar os logs de inicialização
docker compose -f docker/docker-compose.yml logs -f
```

* **Host:** `localhost:5432`
* **Banco:** `freedom_erp`
* **Usuário:** `freedom_user`
* **Senha:** `freedom_secure_password`
* **pgAdmin:** `http://localhost:5050` (Login: `admin@freedom.local` / Senha: `admin_password`)

---

## 📚 Documentação Técnica Completa (Zero Brechas de Interpretação)

Toda a documentação técnica foi estruturada sem ambiguidades e está disponível no diretório [`docs/`](docs/):

1. 🤖 **[Catálogo de Agentes Especialistas](docs/00-catalogo-de-agentes-especialistas.md)**: Papéis e perfis dos 7 agentes (Dados, Legislação 2026, Módulos, Análise, QA, DB e DevOps).
2. 🎯 **[Visão de Produto Comercial 2026](docs/01-visao-produto-comercial.md)**: Posicionamento de mercado, personas B2B e requisitos corporativos.
3. 🔄 **[Matriz de Módulos (Inclusão vs. Remoção)](docs/02-matriz-modulos-inclusao-remocao.md)**: Remoção de hardware obsoleto (ECF serial, TopLink) e inclusão de PIX Dinâmico, DFe Cloud e WhatsApp.
4. ⚖️ **[Legislação Tributária e Fiscal 2026+](docs/03-legislacao-tributaria-e-fiscal-2026.md)**: Reforma Tributária (EC 132/23: CBS, IBS, Imposto Seletivo), regras transitórias, NF-e/NFC-e, IBPT e LGPD.
5. 📦 **[Caderno de Regras de Negócio por Módulo](docs/04-caderno-regras-de-negocio-por-modulo.md)**: Regras exaustivas de STD, FNC, GMS, PCP, LVF, CRM, ATD, PDV, CFG e REP.
6. 🐘 **[Arquitetura de Dados & PostgreSQL 16+](docs/05-arquitetura-dados-e-postgresql.md)**: Mapeamento relacional, DDL canônico, Sequences, Triggers PL/pgSQL e JPA 3.2.
7. 📐 **[Análise de Sistemas & Especificações SDD](docs/06-analise-sistemas-e-especificacoes-sdd.md)**: Casos de uso completos, invariantes e cenários BDD (*Spec Kit*).
8. 🧪 **[Engenharia de Qualidade, TDD & Guardrails](docs/07-qualidade-tdd-e-guardrails.md)**: Pirâmide de testes, Testcontainers e regras estritas com ArchUnit.
9. 🚀 **[DevOps, Infraestrutura Docker & CI/CD](docs/08-devops-infra-docker-ci-cd.md)**: Docker Compose, compilação Maven Java 25 e empacotamento com `jpackage`.

