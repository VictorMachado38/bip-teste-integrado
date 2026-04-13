package br.com.bip.controller;

import br.com.bip.model.Beneficio;
import br.com.bip.service.BeneficioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BeneficioController.class)
@TestPropertySource(properties = "app.cors.allowed-origin=http://localhost:4200")
@DisplayName("BeneficioController")
class BeneficioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BeneficioService service;

    @MockBean
    private JwtDecoder jwtDecoder;

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
    // Seguranca
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /beneficios sem token retorna 401")
    void findAll_semAutenticacao_retorna401() throws Exception {
        mockMvc.perform(get("/api/v1/beneficios"))
                .andExpect(status().isUnauthorized());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /beneficios
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /beneficios retorna lista com 200")
    void findAll_retornaLista() throws Exception {
        when(service.findAll()).thenReturn(List.of(beneficioA, beneficioB));

        mockMvc.perform(get("/api/v1/beneficios").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nome").value("Beneficio A"))
                .andExpect(jsonPath("$[1].nome").value("Beneficio B"));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /beneficios/{id}
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /beneficios/{id} retorna beneficio com 200")
    void findById_retornaBeneficio() throws Exception {
        when(service.findById(1L)).thenReturn(beneficioA);

        mockMvc.perform(get("/api/v1/beneficios/1").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Beneficio A"))
                .andExpect(jsonPath("$.valor").value(1000.00));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /beneficios
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /beneficios cria beneficio e retorna 201")
    void create_retorna201() throws Exception {
        Beneficio novo = new Beneficio();
        novo.setNome("Beneficio Novo");
        novo.setValor(new BigDecimal("750.00"));

        Beneficio salvo = new Beneficio();
        salvo.setId(3L);
        salvo.setNome("Beneficio Novo");
        salvo.setValor(new BigDecimal("750.00"));

        when(service.save(any(Beneficio.class))).thenReturn(salvo);

        mockMvc.perform(post("/api/v1/beneficios")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.nome").value("Beneficio Novo"));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // PUT /beneficios/{id}
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /beneficios/{id} atualiza e retorna 200")
    void update_retorna200() throws Exception {
        Beneficio atualizado = new Beneficio();
        atualizado.setId(1L);
        atualizado.setNome("Beneficio Atualizado");
        atualizado.setValor(new BigDecimal("1200.00"));

        when(service.save(any(Beneficio.class))).thenReturn(atualizado);

        mockMvc.perform(put("/api/v1/beneficios/1")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Beneficio Atualizado"))
                .andExpect(jsonPath("$.valor").value(1200.00));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // DELETE /beneficios/{id}
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /beneficios/{id} remove e retorna 204")
    void delete_retorna204() throws Exception {
        doNothing().when(service).delete(1L);

        mockMvc.perform(delete("/api/v1/beneficios/1").with(jwt()))
                .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /beneficios/transfer
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /beneficios/transfer executa transferencia e retorna 200")
    void transfer_retorna200() throws Exception {
        doNothing().when(service).transfer(1L, 2L, new BigDecimal("300.00"));

        mockMvc.perform(post("/api/v1/beneficios/transfer")
                        .with(jwt())
                        .param("fromId", "1")
                        .param("toId", "2")
                        .param("amount", "300.00"))
                .andExpect(status().isOk());

        verify(service).transfer(1L, 2L, new BigDecimal("300.00"));
    }

    @Test
    @DisplayName("POST /beneficios/transfer com saldo insuficiente retorna 422")
    void transfer_saldoInsuficiente_retorna422() throws Exception {
        doThrow(new IllegalStateException("Saldo insuficiente no beneficio de origem."))
                .when(service).transfer(1L, 2L, new BigDecimal("9999.00"));

        mockMvc.perform(post("/api/v1/beneficios/transfer")
                        .with(jwt())
                        .param("fromId", "1")
                        .param("toId", "2")
                        .param("amount", "9999.00"))
                .andExpect(status().isUnprocessableEntity());
    }
}