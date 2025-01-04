package org.example;

import javax.swing.*; //Swing GUI
import java.awt.*; //GUI中繪圖和布局功能
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent; //按鍵按鈕
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class GomokuGame extends JFrame {

    public JButton[][] board; //棋盤
    public char currentPlayer; //紀錄當前是XO
    public boolean gameWon; //標記遊戲是否結束
    public JLabel statusBar; //狀態列，顯示當前玩家資訊
    public Stack<Point> moveHistory; //儲存玩家移動的歷史記錄
    public int playerXWins;
    public int playerOWins;
    public int noOneWin;
    public static final int WINNING_GAMES = 3; // 設定贏的局數

    public GomokuGame() { //初始化遊戲邏輯和GUI
        initializeGame();
        initializeGUI();
    }

    public void initializeGame() {
        board = new JButton[9][9];
        currentPlayer = 'X';
        gameWon = false;
        moveHistory = new Stack<>();
        playerXWins = 0;
        playerOWins = 0;
        noOneWin = 0;
    }

    public void initializeGUI() {
        setTitle("五子棋");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu optionsMenu = new JMenu("Options");
        optionsMenu.setMnemonic(KeyEvent.VK_O); //設置快捷鍵Alt+O
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
                    if (!gameWon && board[row][col].getText().isEmpty()) { //若位置為空，紀錄動作並設置XO
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
                            noOneWin++;
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

        setSize(600, 600);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void undoMove() {
        if (!moveHistory.isEmpty()) {
            Point lastMove = moveHistory.pop(); //從堆疊取出最後一次移動的座標
            int row = (int) lastMove.getX();
            int col = (int) lastMove.getY();
            board[row][col].setText("");
            board[row][col].setForeground(null);
            currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
            statusBar.setText("Current Turn: " + currentPlayer);
            gameWon = false;
        }
    }

    public void highlightWinningButtons(List<Point> winningLine) {
        for (Point point : winningLine) {
            int i = point.x;
            int j = point.y;
            board[i][j].setForeground(Color.RED);
        }
    }

    public List<Point> checkWin(int row, int col) {
        List<Point> winningLine = checkRow(row); //橫列
        if (winningLine != null) return winningLine;

        winningLine = checkColumn(col); //直列
        if (winningLine != null) return winningLine;

        winningLine = checkDiagonal(row, col); //對角線
        if (winningLine != null) return winningLine;

        winningLine = checkAntiDiagonal(row, col); //反對角線
        if (winningLine != null) return winningLine;

        return null;
    }

    public List<Point> checkRow(int row) {
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

    public List<Point> checkColumn(int col) {
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

    public List<Point> checkDiagonal(int row, int col) {
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

    public List<Point> checkAntiDiagonal(int row, int col) {
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

    public boolean isBoardFull() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (board[i][j].getText().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    public void resetBoard() {
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

    public void resetGame() {
        resetBoard();
        playerXWins = 0;
        playerOWins = 0;
        JOptionPane.showMessageDialog(null, "遊戲結束! 請重新開始新的一局。");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GomokuGame::new);
    }
}
