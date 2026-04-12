package br.com.bip.service;

import br.com.bip.model.Beneficio;
import br.com.bip.repository.BeneficioRepository;
import br.com.bip.service.impl.BeneficioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BeneficioServiceImpl")
class BeneficioServiceImplTest {

    @Mock
    private BeneficioRepository repository;

    @InjectMocks
    private BeneficioServiceImpl service;

    private Beneficio beneficioA;
    private Beneficio beneficioB;

    @BeforeEach
    void setUp() {
        beneficioA = new Beneficio();
        beneficioA.setId(1L);
        beneficioA.setNome("Beneficio A");
        beneficioA.setValor(new BigDecimal("1000.00"));

        beneficioB = new Beneficio();
        beneficioB.setId(2L);
        beneficioB.setNome("Beneficio B");
        beneficioB.setValor(new BigDecimal("500.00"));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // findAll
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findAll retorna lista completa")
    void findAll_retornaLista() {
        when(repository.findAll()).thenReturn(List.of(beneficioA, beneficioB));

        List<Beneficio> result = service.findAll();

        assertThat(result).hasSize(2).containsExactly(beneficioA, beneficioB);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // findById
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findById retorna beneficio existente")
    void findById_encontrado() {
        when(repository.findById(1L)).thenReturn(Optional.of(beneficioA));

        Beneficio result = service.findById(1L);

        assertThat(result).isEqualTo(beneficioA);
    }

    @Test
    @DisplayName("findById lanca excecao para ID inexistente")
    void findById_naoEncontrado() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // save
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("save delega ao repositorio e retorna entidade persistida")
    void save_delegaAoRepositorio() {
        when(repository.save(beneficioA)).thenReturn(beneficioA);

        Beneficio result = service.save(beneficioA);

        assertThat(result).isEqualTo(beneficioA);
        verify(repository).save(beneficioA);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // delete
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete remove beneficio existente")
    void delete_encontrado() {
        when(repository.existsById(1L)).thenReturn(true);

        service.delete(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    @DisplayName("delete lanca excecao para ID inexistente")
    void delete_naoEncontrado() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");

        verify(repository, never()).deleteById(any());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // transfer — bug EJB corrigido
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("transfer")
    class TransferTests {

        @Test
        @DisplayName("transfere valor com saldo suficiente")
        void transfer_sucesso() {
            when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(beneficioA));
            when(repository.findByIdForUpdate(2L)).thenReturn(Optional.of(beneficioB));

            service.transfer(1L, 2L, new BigDecimal("300.00"));

            assertThat(beneficioA.getValor()).isEqualByComparingTo("700.00");
            assertThat(beneficioB.getValor()).isEqualByComparingTo("800.00");
            verify(repository).save(beneficioA);
            verify(repository).save(beneficioB);
        }

        @Test
        @DisplayName("transfere valor exato do saldo (zero apos transferencia)")
        void transfer_valorExatoDoSaldo() {
            when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(beneficioA));
            when(repository.findByIdForUpdate(2L)).thenReturn(Optional.of(beneficioB));

            service.transfer(1L, 2L, new BigDecimal("1000.00"));

            assertThat(beneficioA.getValor()).isEqualByComparingTo("0.00");
            assertThat(beneficioB.getValor()).isEqualByComparingTo("1500.00");
        }

        @Test
        @DisplayName("rejeita transferencia com saldo insuficiente")
        void transfer_saldoInsuficiente() {
            when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(beneficioA));
            when(repository.findByIdForUpdate(2L)).thenReturn(Optional.of(beneficioB));

            assertThatThrownBy(() -> service.transfer(1L, 2L, new BigDecimal("1500.00")))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Saldo insuficiente");

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("rejeita transferencia com valor zero")
        void transfer_valorZero() {
            assertThatThrownBy(() -> service.transfer(1L, 2L, BigDecimal.ZERO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positivo");

            verifyNoInteractions(repository);
        }

        @Test
        @DisplayName("rejeita transferencia com valor negativo")
        void transfer_valorNegativo() {
            assertThatThrownBy(() -> service.transfer(1L, 2L, new BigDecimal("-100.00")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positivo");

            verifyNoInteractions(repository);
        }

        @Test
        @DisplayName("rejeita transferencia com valor nulo")
        void transfer_valorNulo() {
            assertThatThrownBy(() -> service.transfer(1L, 2L, null))
                    .isInstanceOf(IllegalArgumentException.class);

            verifyNoInteractions(repository);
        }

        @Test
        @DisplayName("lanca excecao quando beneficio de origem nao existe")
        void transfer_origemNaoEncontrada() {
            when(repository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.transfer(99L, 2L, new BigDecimal("100.00")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("lanca excecao quando beneficio de destino nao existe")
        void transfer_destinoNaoEncontrado() {
            when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(beneficioA));
            when(repository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.transfer(1L, 99L, new BigDecimal("100.00")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("99");

            verify(repository, never()).save(any());
        }
    }
}