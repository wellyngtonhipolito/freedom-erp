# Engenharia DevOps, Infraestrutura Docker & CI/CD

Este documento estabelece os padrões operacionais de infraestrutura, conteinerização e automação de build para o **Freedom ERP**.

---

## 1. Configuração do Compilador Java 25 no Maven

O arquivo `pom.xml` raiz configura o compilador para a versão **Java 25 LTS**:

```xml
<properties>
    <java.version>25</java.version>
    <maven.compiler.source>25</maven.compiler.source>
    <maven.compiler.target>25</maven.compiler.target>
    <maven.compiler.release>25</maven.compiler.release>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <hibernate.version>6.6.1.Final</hibernate.version>
    <flatlaf.version>3.5.2</flatlaf.version>
    <testcontainers.version>1.20.2</testcontainers.version>
</properties>
```

---

## 2. Orquestração Docker do Banco de Dados

O arquivo [`docker/docker-compose.yml`](file:///c:/Users/welly/OneDrive/Desktop/projetos/SISTEMAS/freedom-erp/docker/docker-compose.yml) provê o ambiente isolado do PostgreSQL 16 Alpine:

```bash
# Subir o ambiente em background
docker compose -f docker/docker-compose.yml up -d

# Visualizar status de saúde do container
docker compose -f docker/docker-compose.yml ps

# Executar backup manual do banco de dados
docker exec -t freedom-postgres pg_dump -U freedom_user freedom_erp > backup_freedom_$(date +%Y%m%d).sql
```

---

## 3. Pipeline de Integração Contínua (GitHub Actions)

Localizado em `.github/workflows/ci.yml`:

```yaml
name: Freedom ERP CI Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  build-and-test:
    runs-on: ubuntu-latest

    services:
      postgres:
        image: postgres:16-alpine
        env:
          POSTGRES_DB: freedom_erp_test
          POSTGRES_USER: freedom_test
          POSTGRES_PASSWORD: test_password
        ports:
          - 5432:5432
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

    steps:
    - name: Checkout Código-Fonte
      uses: actions/checkout@v4

    - name: Configurar JDK 25
      uses: actions/setup-java@v4
      with:
        distribution: 'temurin'
        java-version: '25'
        cache: 'maven'

    - name: Compilação e Execução de Testes com Guardrails
      run: mvn clean verify -B
```

---

## 4. Empacotamento Desktop com `jpackage` (Sem Dependência de Java no Cliente)

Para entregar o sistema comercialmente sem exigir que os clientes finais instalem ou configurem o Java manualmente na máquina:
1. Usamos `jlink` para extrair apenas os módulos estritamente necessários do Java 25, reduzindo o tamanho do JRE de 400MB para ~45MB.
2. Usamos `jpackage` para gerar instaladores nativos:
   * **Windows**: Gera um arquivo `freedom-erp-setup-1.0.exe` ou `.msi` com ícone na área de trabalho e atalho no menu iniciar.
   * **Linux**: Gera pacotes `.deb` (Debian/Ubuntu) e `.rpm` (RedHat/Fedora).

