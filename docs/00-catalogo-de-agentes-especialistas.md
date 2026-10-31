# Catálogo de Agentes Especialistas: Freedom ERP

Este documento cataloga formalmente os 7 agentes especialistas criados e registrados para a engenharia, governança e modernização integral do **Freedom ERP**.

---

## 1. Matriz de Agentes Especialistas

| Agente | ID do Subagente | Escopo e Responsabilidade Principal |
| :--- | :--- | :--- |
| **Arquiteto de Dados** | `data-architect` | Modelagem relacional estrita, DDL PostgreSQL 16+, normalização, migração Firebird $\rightarrow$ Postgres, estratégias de chaves primárias, sequences, índices B-Tree/GIN e auditoria JPA/Hibernate. |
| **Especialista em Legislação Brasileira** | `brazilian-fiscal-legal-expert` | Conformidade tributária com foco no marco de 2026 (Reforma Tributária EC 132/2023: CBS, IBS, Imposto Seletivo), regras transitórias de ICMS/ISS/PIS/COFINS, NF-e 4.0/5.0, NFC-e, SPED Fiscal, EFD-Reinf, Lei do IBPT e LGPD. |
| **Especialista em Módulos do Sistema** | `erp-modules-specialist` | Domínio funcional profundo de cada submódulo (STD, FNC, GMS, PCP, LVF, CRM, ATD, PDV, CFG, REP), mapeando fluxos de trabalho, cadastros e validações de tela. |
| **Analista de Sistemas** | `systems-analyst` | Especificação formal de requisitos pelo método SDD (Specification-Driven Development), criação do Spec Kit (Given-When-Then), máquinas de estado de negócio e diagramas de sequência. |
| **Engenheiro de Qualidade e Testes** | `qa-test-engineer` | TDD rigoroso (Red-Green-Refactor), pirâmide de testes (Unitários, Integração com Testcontainers, UI Swing com AssertJ-Swing) e guardrails arquiteturais com ArchUnit. |
| **Engenheiro de Banco de Dados** | `database-engineer` | Scripts Flyway versionados, otimização de planos de execução (EXPLAIN ANALYZE), funções em PL/pgSQL, particionamento e políticas de backup/restore. |
| **Engenheiro DevOps & SRE** | `devops-engineer` | Docker, Docker Compose, pipelines CI/CD (GitHub Actions), Maven com Java 25 LTS, empacotamento desktop sem dependência de Java local (`jpackage`/`jlink`). |

---

## 2. Invocação e Colaboração entre Agentes

Cada agente já foi definido e registrado no ambiente através do comando nativo `define_subagent` e pode ser invocado sob demanda para tarefas específicas:

* **Para modelar novas tabelas ou migrar DDLs**: Invocar `data-architect` e `database-engineer`.
* **Para auditar cálculos fiscais ou notas técnicas da SEFAZ**: Invocar `brazilian-fiscal-legal-expert`.
* **Para detalhar regras de faturamento, estoque ou produção**: Invocar `erp-modules-specialist`.
* **Para escrever especificações formais antes de codificar**: Invocar `systems-analyst`.
* **Para assegurar testes automatizados e guardrails**: Invocar `qa-test-engineer`.
* **Para criar pipelines e scripts de infraestrutura**: Invocar `devops-engineer`.

