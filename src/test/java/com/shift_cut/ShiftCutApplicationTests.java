package com.shift_cut;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("ShiftCut - Carga de contexto de la aplicación")
class ShiftCutApplicationTests {

    @Test
    @DisplayName("El contexto de Spring se levanta correctamente")
    void contextLoads() {
        // Verifica que todos los beans se inicializan sin errores
    }

}
