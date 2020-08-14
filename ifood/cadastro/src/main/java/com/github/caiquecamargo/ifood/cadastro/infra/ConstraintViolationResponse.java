package com.github.caiquecamargo.ifood.cadastro.infra;

import java.util.ArrayList;
import java.util.List;

import javax.validation.ConstraintViolationException;

public class ConstraintViolationResponse {

  private final List<ConstraintViolationImpl> violacoes = new ArrayList<>();

  private ConstraintViolationResponse(ConstraintViolationException excption) {
    excption.getConstraintViolations().forEach(violation -> violacoes.add(ConstraintViolationImpl.of(violation)));
  }

  public static ConstraintViolationResponse of(ConstraintViolationException exception) {
    return new ConstraintViolationResponse(exception);
  }

  public List<ConstraintViolationImpl> getViolacoes() {
    return violacoes;
  }

}