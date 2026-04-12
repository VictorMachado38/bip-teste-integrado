package br.com.bip.service.impl;

import br.com.bip.model.Beneficio;
import br.com.bip.repository.BeneficioRepository;
import br.com.bip.service.BeneficioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class BeneficioServiceImpl implements BeneficioService {

    private final BeneficioRepository repository;

    public BeneficioServiceImpl(BeneficioRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Beneficio> findAll() {
        return repository.findAll();
    }

    @Override
    public Beneficio findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Beneficio não encontrado: " + id));
    }

    @Override
    @Transactional
    public Beneficio save(Beneficio beneficio) {
        return repository.save(beneficio);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Beneficio não encontrado: " + id);
        }
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public void transfer(Long fromId, Long toId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor da transferência deve ser positivo.");
        }

        // Bloqueio pessimista para evitar lost update e leitura suja
        Beneficio from = repository.findByIdForUpdate(fromId)
                .orElseThrow(() -> new IllegalArgumentException("Beneficio de origem não encontrado: " + fromId));
        Beneficio to = repository.findByIdForUpdate(toId)
                .orElseThrow(() -> new IllegalArgumentException("Beneficio de destino não encontrado: " + toId));

        if (from.getValor().compareTo(amount) < 0) {
            throw new IllegalStateException("Saldo insuficiente no beneficio de origem.");
        }

        from.setValor(from.getValor().subtract(amount));
        to.setValor(to.getValor().add(amount));

        repository.save(from);
        repository.save(to);
    }
}