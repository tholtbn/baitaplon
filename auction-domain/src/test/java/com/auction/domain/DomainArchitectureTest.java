package com.auction.domain;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/** Khóa luật layer: domain không được biết hạ tầng, mạng, UI hay các layer bên ngoài. */
@AnalyzeClasses(packages = "com.auction.domain", importOptions = ImportOption.DoNotIncludeTests.class)
class DomainArchitectureTest {

    @ArchTest
    static final ArchRule domainDoesNotDependOnOuterLayersOrFrameworks =
            noClasses()
                    .that().resideInAPackage("com.auction.domain..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "java.sql..",
                            "javax.sql..",
                            "java.net..",
                            "javafx..",
                            "com.fasterxml.jackson..",
                            "com.auction.application..",
                            "com.auction.infrastructure..",
                            "com.auction.server..",
                            "com.auction.client..");

    @ArchTest
    static final ArchRule topLevelPackagesAreFreeOfCycles =
            slices().matching("com.auction.domain.(*)..").should().beFreeOfCycles();
}