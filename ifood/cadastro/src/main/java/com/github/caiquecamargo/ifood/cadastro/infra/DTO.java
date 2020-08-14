package com.github.caiquecamargo.ifood.cadastro.infra;

import javax.validation.ConstraintValidatorContext;

public interface DTO {

  default boolean isValid(ConstraintValidatorContext constraintValidatorContext) {
    return true;
  }

}