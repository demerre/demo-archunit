package com.example;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.Architectures.LayeredArchitecture;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RestController;

class ArchUnitTest {

  JavaClasses importedClasses = new ClassFileImporter().importPackages("com.example");

  // Validate that class names in the controller package end with Controller
  @Test
  void controllerClassNameShouldEndWithController() {
    ArchRule rule = classes()
        .that().resideInAPackage("..controller..")
        .and().areAnnotatedWith(RestController.class)
        .should().haveSimpleNameEndingWith("Controller");

    rule.check(importedClasses);
  }


  // Ensure the payment package only depends on the common or java packages
  @Test
  void paymentFeatureShouldNotDependOnOtherFeatures() {
    ArchRule rule = classes()
        .that().resideInAPackage("..payment..")
        .should().onlyDependOnClassesThat()
        .resideInAnyPackage("com.example.common..", "java..", "org.springframework..");

    rule.check(importedClasses);
  }


  // Ensure the common package does not depend on any other feature packages
  @Test
  void commonCodeShouldNotDependOnOtherFeatures() {
    ArchRule rule = noClasses()
        .that().resideInAPackage("com.example.common..")
        .should().dependOnClassesThat()
        .resideOutsideOfPackages("com.example.common..", "java..", "org.springframework..");

    rule.check(importedClasses);
  }


  // Ensure controller layer is isolated and service is only accessed by controller
  @Test
  void controllerShouldOnlyUseService() {
    LayeredArchitecture arch = layeredArchitecture()
        .consideringAllDependencies()
        .layer("Controller").definedBy("..controller..")
        .layer("Service").definedBy("..service..")
        .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
        .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller");

    arch.check(importedClasses);
  }


  // Ensure domain modules do not depend on each other
  @Test
  void domainModulesShouldNotDependOnEachOther() {
    ArchRule rule = noClasses()
        .that().resideInAPackage("com.example.payment..")
        .should().dependOnClassesThat()
        .resideInAPackage("com.example.reservation..");

    rule.check(importedClasses);
  }

  // Validate that class names in the service package end with Service
  @Test
  void serviceClassesShouldBeSuffixedWithService() {
    ArchRule rule = classes()
        .that().resideInAPackage("..service..")
        .should().haveSimpleNameEndingWith("Service");

    rule.check(importedClasses);
  }

  // Ensure controller package depends only on service or common
  @Test
  void controllerShouldOnlyDependOnServiceOrCommon() {
    ArchRule rule = classes()
        .that().resideInAPackage("..controller..")
        .should().onlyDependOnClassesThat()
        .resideInAnyPackage("..service..", "java..", "org.springframework..",
            "com.example.common..");

    rule.check(importedClasses);
  }
}
