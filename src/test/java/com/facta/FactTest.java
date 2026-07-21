package com.facta;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.facta.Facta.Sequence;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FactTest {
    
    class Board {
        boolean home = false;
        int x, y = 0;
        
    }

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board();
    }
    
    @Test
    public void shouldSequence() {
        assertEquals(
                new Node.Sequence<Board>(List.of()),
                Sequence().With(board).node);

    }
}
