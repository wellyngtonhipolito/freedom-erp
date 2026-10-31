package org.freedom.core.service.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Guardrails Arquiteturais Estritos (ArchUnit).
 * Garante que nenhuma classe de tela acesse banco de dados ou frameworks ORM diretamente,
 * e que a camada de domínio permaneça pura (sem acoplamento com Swing).
 */
@AnalyzeClasses(packages = "org.freedom", importOptions = {ImportOption.DoNotIncludeTests.class})
public class ArchitectureGuardrailsTest {

    @ArchTest
    public static final ArchRule presentationMustNotAccessDatabaseDirectly =
        noClasses().that().resideInAPackage("..ui..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "java.sql..",
                "javax.sql..",
                "jakarta.persistence..",
                "org.hibernate.."
            )
            .as("A camada de UI (Swing) não pode acessar classes de banco de dados ou persistência diretamente.");

    @ArchTest
    public static final ArchRule domainMustNotDependOnUI =
        noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "java.awt..",
                "javax.swing..",
                "com.formdev.flatlaf.."
            )
            .as("O domínio de negócio deve ser puro e nunca deve depender de interfaces gráficas.");
}

