package br.com.bip.service;

import br.com.bip.model.Beneficio;

import java.math.BigDecimal;
import java.util.List;

public interface BeneficioService {

    List<Beneficio> findAll();

    Beneficio findById(Long id);

    Beneficio save(Beneficio beneficio);

    void delete(Long id);

    void transfer(Long fromId, Long toId, BigDecimal amount);
}