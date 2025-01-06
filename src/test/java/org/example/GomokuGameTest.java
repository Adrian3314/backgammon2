package org.example;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterEach;

class GomokuGameTest {

    private GomokuGame game;

    @BeforeEach
    void setUp() {
        game = new GomokuGame();
    }

    @AfterEach
    void tearDown() {
        game.dispose();
    }

    @Test
    void testHorizontalWin() {
        // Simulate X placing five stones in a horizontal line
        int col=0;
        for(int i=10;i>0;i--){
            if(game.currentPlayer == 'X') {
                game.board[0][col].doClick();
                col++;
            }else{
                game.board[1][col].doClick();
            }
        }
        // Assert X wins
        assertEquals(1, game.playerXWins);
    }

    @Test
    void testVerticalWin() {
        // Simulate O placing five stones in a vertical line
        int row=0;
        for(int i=10;i>0;i--){
            if(game.currentPlayer == 'X') {
                if(row<3){
                    game.board[row][0].doClick();
                }else{
                    game.board[row][2].doClick();
                }
                row++;
            }else{
                game.board[row][1].doClick();
            }
        }
        // Assert O wins
        assertEquals(1, game.playerOWins);
    }

    @Test
    void testDiagonalWin() {
        // Simulate O placing five stones in a vertical line
        int row=0;
        for(int i=10;i>0;i--){
            if(game.currentPlayer == 'X') {
                if(row<3){
                    game.board[row][0].doClick();
                }else{
                    game.board[row][2].doClick();
                }
                row++;
            }else{
                game.board[row][row].doClick();
            }
        }
        // Assert O wins
        assertEquals(1, game.playerOWins);
    }


    @Test
    void testFullBoardNoWin() {
        // Fill the board alternately without any player forming a line of 5
        boolean currentPlayerIsX = true;
        for (int j = 0; j < 9; j++) {
            game.currentPlayer = currentPlayerIsX ? 'X' : 'O';
            game.board[j][0].doClick();
            currentPlayerIsX = !currentPlayerIsX;
        }
        for(int i=0;i<6;i++){
            for(int k=1;k<9;k++){
                if(i<3) {
                    game.board[i][k].doClick();
                }else{
                    game.board[i+3][k].doClick();
                }
            }
        }
        for(int i=0;i<3;i++){
            for(int k=8;k>0;k--){
                game.board[i+3][k].doClick();
            }
        }
        // Assert draw
        assertEquals(1, game.noOneWin);
    }

    @Test
    void testWinCount() {
        // Simulate X winning three games
        int row=0;
        for (int i = 0; i < 3; i++) {
            for(int j=10;j>0;j--){
                if(game.currentPlayer == 'X') {
                    if(row<3){
                        game.board[row][3].doClick();
                    }else{
                        game.board[row][6].doClick();
                    }
                    row++;
                }else{
                    game.board[row][5].doClick();
                }
            }
            if (i < 2) {
                game.resetBoard();
                row=0;
            }

        }
        // Assert X wins 3 games and game ends
        assertEquals(0, game.playerXWins);
        assertEquals(0, game.playerOWins);
        assertFalse(game.gameWon);
    }

    @Test
    void testUndoMove() {
        char lastPlayer ='X';
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                game.board[i][j].doClick();
                if (i == 3 - 1 && j == 4 - 2) {
                    lastPlayer = game.currentPlayer;
                }
            }
        }
        game.undoMove();
        assertEquals("", game.board[3][4].getText());
        assertEquals(lastPlayer, game.currentPlayer);
        assertFalse(game.gameWon);
    }

    @Test
    void testIllegalMove() {
        // Simulate X placing a stone
        game.board[0][0].doClick();
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
    @Test
    void testResetBoard() {
        // Simulate some moves
        game.board[0][0].doClick();
        game.board[0][1].doClick();
        game.board[0][2].doClick();
        // Reset the board
        game.resetBoard();
        // Assert the board is empty and game state is reset
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                assertEquals("", game.board[i][j].getText());
            }
        }
        assertEquals('X', game.currentPlayer);
        assertFalse(game.gameWon);
        assertEquals(0, game.moveHistory.size());
    }

    @Test
    void testResetGame() {
        // Simulate some moves and wins
        game.board[0][0].doClick();
        game.board[0][1].doClick();
        game.board[0][2].doClick();
        game.board[0][3].doClick();
        game.board[0][4].doClick();
        game.playerXWins = 3;
        // Reset the game
        game.resetGame();
        // Assert the game state is reset
        assertEquals(0, game.playerXWins);
        assertEquals(0, game.playerOWins);
        assertEquals(0, game.noOneWin);
        assertEquals('X', game.currentPlayer);
        assertFalse(game.gameWon);
    }

    @Test
    void testTimer() throws InterruptedException {
        // Simulate a move to start the timer
        game.board[0][0].doClick();
        // Wait for the timer to run out
        Thread.sleep(16000);
        // Assert the turn has switched
        assertEquals('O', game.currentPlayer);
    }

    @Test
    void testHint() {
        // Simulate a hint request
        Point hint = game.suggestMove();
        assertNotNull(hint);
        assertTrue(hint.x >= 0 && hint.x < 9);
        assertTrue(hint.y >= 0 && hint.y < 9);
    }
    @Test
    void testCountContinuousHorizontal() {
        // Simulate placing stones in a horizontal line
        for (int i = 0; i < 5; i++) {
            game.board[0][i].setText("X");
        }
        // Assert countContinuous returns 4 for horizontal direction
        assertEquals(4, game.countContinuous(0, 0, 0, 1, 'X'));
    }

    @Test
    void testCountContinuousVertical() {
        // Simulate placing stones in a vertical line
        for (int i = 0; i < 5; i++) {
            game.board[i][0].setText("X");
        }
        // Assert countContinuous returns 4 for vertical direction
        assertEquals(4, game.countContinuous(0, 0, 1, 0, 'X'));
    }

    @Test
    void testCountContinuousDiagonal() {
        // Simulate placing stones in a diagonal line
        for (int i = 0; i < 5; i++) {
            game.board[i][i].setText("X");
        }
        // Assert countContinuous returns 4 for diagonal direction
        assertEquals(4, game.countContinuous(0, 0, 1, 1, 'X'));
    }

    @Test
    void testCountContinuousAntiDiagonal() {
        // Simulate placing stones in an anti-diagonal line
        for (int i = 0; i < 5; i++) {
            game.board[i][8 - i].setText("X");
        }
        // Assert countContinuous returns 4 for anti-diagonal direction
        assertEquals(4, game.countContinuous(0, 8, 1, -1, 'X'));
    }
}

