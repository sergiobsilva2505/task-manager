package br.com.forjacode.taskmanager.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@AnalyzeClasses(packages = "br.com.forjacode.taskmanager")
class HexagonalArchitectureTest {

    @ArchTest
    static final ArchRule layers_should_respect_hexagonal_dependencies =
            layeredArchitecture()
                    .consideringOnlyDependenciesInLayers()
                    .layer("Domain").definedBy("..domain..")
                    .layer("Application").definedBy("..application..")
                    .layer("Adapters").definedBy("..adapters..")
                    .whereLayer("Domain").mayNotAccessAnyLayer()
                    .whereLayer("Application").mayOnlyAccessLayers("Domain")
                    .whereLayer("Adapters").mayOnlyAccessLayers("Application", "Domain");

    @ArchTest
    static final ArchRule domain_should_not_depend_on_frameworks =
            noClasses().that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "org.springframework..",
                            "jakarta.persistence..");

    @ArchTest
    static final ArchRule use_case_classes_should_be_interfaces =
            classes().that().haveSimpleNameEndingWith("UseCase")
                    .should().beInterfaces();

    @ArchTest
    static final ArchRule port_classes_should_be_interfaces =
            classes().that().haveSimpleNameEndingWith("Port")
                    .should().beInterfaces();
}