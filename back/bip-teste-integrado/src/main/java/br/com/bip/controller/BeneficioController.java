package br.com.bip.controller;

import br.com.bip.model.Beneficio;
import br.com.bip.service.BeneficioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/beneficios")
public class BeneficioController {

    private final BeneficioService service;

    public BeneficioController(BeneficioService service) {
        this.service = service;
    }

    @GetMapping
    public List<Beneficio> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Beneficio findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<Beneficio> create(@RequestBody Beneficio beneficio) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(beneficio));
    }

    @PutMapping("/{id}")
    public Beneficio update(@PathVariable Long id, @RequestBody Beneficio beneficio) {
        beneficio.setId(id);
        return service.save(beneficio);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/transfer")
    public ResponseEntity<Void> transfer(
            @RequestParam Long fromId,
            @RequestParam Long toId,
            @RequestParam BigDecimal amount) {
        service.transfer(fromId, toId, amount);
        return ResponseEntity.ok().build();
    }
}