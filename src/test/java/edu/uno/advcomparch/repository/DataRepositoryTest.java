package edu.uno.advcomparch.repository;

import edu.uno.advcomparch.AbstractCompArchTest;
import edu.uno.advcomparch.model.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class DataRepositoryTest extends AbstractCompArchTest {

    private DataRepository<String, Integer> dataRepository;

    @BeforeEach
    public void setup() {
        dataRepository = new DataRepository<>();
    }

    @Test
    public void testGet() {
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> dataRepository.get(1))
                .withMessage("Get - Unsupported Operation");
    }

    @Test
    public void testVictimize() {
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> dataRepository.victimize(1))
                .withMessage("Victimize - Unsupported Operation");
    }

    @Test
    public void testWrite() {
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> dataRepository.write(new Data<>("Blargh")))
                .withMessage("Write - Unsupported Operation");
    }
}
