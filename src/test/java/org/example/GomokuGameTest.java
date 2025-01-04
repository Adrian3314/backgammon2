package org.example;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GomokuGameTest {

    private GomokuGame game;

    @BeforeEach
    void setUp() {
        game = new GomokuGame();
    }

    @Test
    void testHorizontalWin() {
        // Simulate X placing five stones in a horizontal line
        for (int col = 0; col < 5; col++) {
            game.board[0][col].doClick();
        }
        // Assert X wins
        assertTrue(game.gameWon);
        assertEquals('X', game.currentPlayer);
    }

    @Test
    void testVerticalWin() {
        // Simulate O placing five stones in a vertical line
        game.currentPlayer = 'O';
        for (int row = 0; row < 5; row++) {
            game.board[row][0].doClick();
        }
        // Assert O wins
        assertTrue(game.gameWon);
        assertEquals('O', game.currentPlayer);
    }

    @Test
    void testFullBoardNoWin() { //因為填滿的方式是XO輪流，所以在填滿前該局就結束了
        // Fill the board alternately without any player forming a line of 5
        boolean currentPlayerIsX = true;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                game.currentPlayer = currentPlayerIsX ? 'X' : 'O';
                game.board[i][j].doClick();
                currentPlayerIsX = !currentPlayerIsX;
            }
        }
        // Assert draw
        assertTrue(game.isBoardFull());
        assertFalse(game.gameWon);
    }

    @Test
    void testWinCount() {
        // Simulate X winning three games
        for (int i = 0; i < 3; i++) {
            for (int col = 0; col < 5; col++) {
                game.board[i][col].doClick();
            }
            if (i < 2) {
                game.resetBoard();
            }
        }
        // Assert X wins 3 games and game ends
        assertEquals(3, game.playerXWins);
        assertEquals(0, game.playerOWins);
        assertTrue(game.gameWon);
    }

    @Test
    void testIllegalMove() {
        // Simulate X placing a stone
        game.board[0][0].doClick();
        // Attempt O placing a stone in the same spot
        game.currentPlayer = 'O';
        game.board[0][0].doClick();
        // Assert the spot is not overwritten
        assertEquals("X", game.board[0][0].getText());
    }

    @Test
    void testTurnOrder() {
        // Simulate X and O taking turns
        game.board[0][0].doClick();
        assertEquals('O', game.currentPlayer); // Turn switched to O
        game.board[0][1].doClick();
        assertEquals('X', game.currentPlayer); // Turn switched back to X
    }
}
