package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class GomokuGame extends JFrame {

    private JButton[][] board;
    private char currentPlayer;
    private boolean gameWon;
    private JLabel statusBar;
    private Stack<Point> moveHistory;
    private int playerXWins;
    private int playerOWins;
    private static final int WINNING_GAMES = 3; // 設定贏的局數

    public GomokuGame() {
        initializeGame();
        initializeGUI();
    }

    private void initializeGame() {
        board = new JButton[9][9];
        currentPlayer = 'X';
        gameWon = false;
        moveHistory = new Stack<>();
        playerXWins = 0;
        playerOWins = 0;
    }

    private void initializeGUI() {
        setTitle("五子棋");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu optionsMenu = new JMenu("Options");
        optionsMenu.setMnemonic(KeyEvent.VK_O);
        menuBar.add(optionsMenu);

        JMenuItem resetItem = new JMenuItem("Reset");
        resetItem.setMnemonic(KeyEvent.VK_R);
        resetItem.addActionListener(e -> resetBoard());
        optionsMenu.add(resetItem);

        JMenuItem undoItem = new JMenuItem("Undo");
        undoItem.setMnemonic(KeyEvent.VK_U);
        undoItem.addActionListener(e -> undoMove());
        optionsMenu.add(undoItem);

        statusBar = new JLabel("Current Turn: " + currentPlayer);
        statusBar.setFont(new Font("Arial", Font.PLAIN, 20));
        statusBar.setHorizontalAlignment(SwingConstants.CENTER);
        add(statusBar, BorderLayout.SOUTH);

        JPanel boardPanel = new JPanel(new GridLayout(9, 9));

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                board[i][j] = new JButton("");
                board[i][j].setFont(new Font("Arial", Font.PLAIN, 40));
                board[i][j].setFocusPainted(false);
                int row = i;
                int col = j;
                board[i][j].addActionListener(e -> {
                    if (!gameWon && board[row][col].getText().equals("")) {
                        moveHistory.push(new Point(row, col));
                        board[row][col].setText(Character.toString(currentPlayer));

                        List<Point> winningLine = checkWin(row, col);
                        if (winningLine != null) {
                            gameWon = true;
                            highlightWinningButtons(winningLine);
                            if (currentPlayer == 'X') {
                                playerXWins++;
                            } else {
                                playerOWins++;
                            }

                            if (playerXWins == WINNING_GAMES || playerOWins == WINNING_GAMES) {
                                JOptionPane.showMessageDialog(null, currentPlayer + " wins the game!\nX Wins: " + playerXWins + " | O Wins: " + playerOWins);
                                resetGame();
                                return;
                            } else {
                                JOptionPane.showMessageDialog(null, currentPlayer + " wins this round!\nX Wins: " + playerXWins + " | O Wins: " + playerOWins);
                                resetBoard();
                            }
                        } else if (isBoardFull()) {
                            gameWon = true;
                            JOptionPane.showMessageDialog(null, "平手!\nX Wins: " + playerXWins + " | O Wins: " + playerOWins);
                            resetBoard();
                        } else {
                            currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
                            statusBar.setText("Current Turn: " + currentPlayer);
                        }
                    }
                });
                boardPanel.add(board[i][j]);
            }
        }

        add(boardPanel, BorderLayout.CENTER);

        setSize(600, 700);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void undoMove() {
        if (!moveHistory.isEmpty()) {
            Point lastMove = moveHistory.pop();
            int row = (int) lastMove.getX();
            int col = (int) lastMove.getY();
            board[row][col].setText("");
            board[row][col].setForeground(null);
            currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
            statusBar.setText("Current Turn: " + currentPlayer);
            gameWon = false;
        }
    }

    private void highlightWinningButtons(List<Point> winningLine) {
        for (Point point : winningLine) {
            int i = point.x;
            int j = point.y;
            board[i][j].setForeground(Color.RED);
        }
    }

    private List<Point> checkWin(int row, int col) {
        List<Point> winningLine = checkRow(row);
        if (winningLine != null) return winningLine;

        winningLine = checkColumn(col);
        if (winningLine != null) return winningLine;

        winningLine = checkDiagonal(row, col);
        if (winningLine != null) return winningLine;

        winningLine = checkAntiDiagonal(row, col);
        if (winningLine != null) return winningLine;

        return null;
    }

    private List<Point> checkRow(int row) {
        int count = 0;
        List<Point> winningLine = new ArrayList<>();

        for (int j = 0; j < 9; j++) {
            if (board[row][j].getText().equals(Character.toString(currentPlayer))) {
                count++;
                winningLine.add(new Point(row, j));
                if (count == 5) return winningLine;
            } else {
                count = 0;
                winningLine.clear();
            }
        }
        return null;
    }

    private List<Point> checkColumn(int col) {
        int count = 0;
        List<Point> winningLine = new ArrayList<>();

        for (int i = 0; i < 9; i++) {
            if (board[i][col].getText().equals(Character.toString(currentPlayer))) {
                count++;
                winningLine.add(new Point(i, col));
                if (count == 5) return winningLine;
            } else {
                count = 0;
                winningLine.clear();
            }
        }
        return null;
    }

    private List<Point> checkDiagonal(int row, int col) {
        int count = 1;
        List<Point> winningLine = new ArrayList<>();
        winningLine.add(new Point(row, col));

        int i = row - 1;
        int j = col - 1;
        while (i >= 0 && j >= 0 && board[i][j].getText().equals(Character.toString(currentPlayer))) {
            count++;
            winningLine.add(0, new Point(i, j));
            i--;
            j--;
        }

        i = row + 1;
        j = col + 1;
        while (i < 9 && j < 9 && board[i][j].getText().equals(Character.toString(currentPlayer))) {
            count++;
            winningLine.add(new Point(i, j));
            i++;
            j++;
        }

        return count >= 5 ? winningLine : null;
    }

    private List<Point> checkAntiDiagonal(int row, int col) {
        int count = 1;
        List<Point> winningLine = new ArrayList<>();
        winningLine.add(new Point(row, col));

        int i = row - 1;
        int j = col + 1;
        while (i >= 0 && j < 9 && board[i][j].getText().equals(Character.toString(currentPlayer))) {
            count++;
            winningLine.add(0, new Point(i, j));
            i--;
            j++;
        }

        i = row + 1;
        j = col - 1;
        while (i < 9 && j >= 0 && board[i][j].getText().equals(Character.toString(currentPlayer))) {
            count++;
            winningLine.add(new Point(i, j));
            i++;
            j--;
        }

        return count >= 5 ? winningLine : null;
    }

    private boolean isBoardFull() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (board[i][j].getText().equals("")) {
                    return false;
                }
            }
        }
        return true;
    }

    private void resetBoard() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                board[i][j].setText("");
                board[i][j].setForeground(null);
            }
        }
        currentPlayer = 'X';
        gameWon = false;
        moveHistory.clear();
        statusBar.setText("Current Turn: " + currentPlayer);
    }

    private void resetGame() {
        resetBoard();
        playerXWins = 0;
        playerOWins = 0;
        JOptionPane.showMessageDialog(null, "遊戲結束! 請重新開始新的一局。");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GomokuGame::new);
    }
}
