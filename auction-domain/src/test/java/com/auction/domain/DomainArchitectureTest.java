package com.auction.domain;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.auction.domain", importOptions = ImportOption.DoNotIncludeTests.class)
class DomainArchitectureTest {

    @ArchTest
    static final ArchRule domainDoesNotDependOnUpperLayers = noClasses()
            .that().resideInAPackage("com.auction.domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "com.auction.application..", "com.auction.infrastructure..",
                    "com.auction.server..", "com.auction.client..");

    @ArchTest
    static final ArchRule domainHasNoFrameworkDependency = noClasses()
            .that().resideInAPackage("com.auction.domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "com.fasterxml..", "javafx..", "java.sql..", "java.net..");
}