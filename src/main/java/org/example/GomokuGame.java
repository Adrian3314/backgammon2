package org.example;

import javax.swing.*; //Swing GUI
import java.awt.*; //GUI中繪圖和布局功能
import java.awt.event.KeyEvent; //按鍵按鈕
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

public class GomokuGame extends JFrame {

    public JButton[][] board; //棋盤
    public char currentPlayer; //紀錄當前是XO
    public boolean gameWon; //標記遊戲是否結束
    public JLabel statusBar; //狀態列，顯示當前玩家資訊
    public JLabel timerLabel; //顯示倒數計時
    public JLabel scoreLabel; //顯示分數
    public Stack<Point> moveHistory; //儲存玩家移動的歷史記錄
    public int playerXWins;
    public int playerOWins;
    public int noOneWin;
    public static final int WINNING_GAMES = 3; // 設定贏的局數
    private Timer timer; // 倒數計時器
    private int timeRemaining; // 剩餘時間
    private JButton hintedButton = null; // 用於記錄當前被提示的按鈕


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
        timer = new Timer();
        timeRemaining = 15; // 每回合15秒
    }

    public void initializeGUI() {
        setTitle("五子棋");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 初始化菜單欄
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu optionsMenu = new JMenu("Options");
        optionsMenu.setMnemonic(KeyEvent.VK_O); // 設置快捷鍵 Alt+O
        menuBar.add(optionsMenu);

        // 添加 Reset 選項
        JMenuItem resetItem = new JMenuItem("Reset");
        resetItem.setMnemonic(KeyEvent.VK_R);
        resetItem.addActionListener(e -> resetBoard());
        optionsMenu.add(resetItem);

        // 添加 Undo 選項
        JMenuItem undoItem = new JMenuItem("Undo");
        undoItem.setMnemonic(KeyEvent.VK_U);
        undoItem.addActionListener(e -> undoMove());
        optionsMenu.add(undoItem);

        // 添加 Hint 選項
        JMenuItem hintItem = new JMenuItem("Hint");
        hintItem.setMnemonic(KeyEvent.VK_H);
        hintItem.addActionListener(e -> {
            if (hintedButton != null) {
                hintedButton.setBackground(null); // 恢復原本的顏色
            }
            Point hint = suggestMove();
            if (hint != null) {
                hintedButton = board[hint.x][hint.y];
                hintedButton.setBackground(Color.RED);
            }
        });
        optionsMenu.add(hintItem);

        // 狀態欄
        JPanel statusPanel = new JPanel(new GridLayout(1, 3));

        statusBar = new JLabel("Current Turn: " + currentPlayer);
        statusBar.setFont(new Font("Arial", Font.PLAIN, 20));
        statusBar.setHorizontalAlignment(SwingConstants.CENTER);
        statusPanel.add(statusBar);

        timerLabel = new JLabel("Time Remaining: " + timeRemaining + " seconds");
        timerLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        timerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusPanel.add(timerLabel);

        scoreLabel = new JLabel("Score - X: 0 | O: 0");
        scoreLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusPanel.add(scoreLabel);

        add(statusPanel, BorderLayout.SOUTH);

        // 棋盤
        JPanel boardPanel = new JPanel(new GridLayout(9, 9));

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                board[i][j] = new JButton("");
                board[i][j].setFont(new Font("Arial", Font.PLAIN, 40));
                board[i][j].setFocusPainted(false);
                int row = i;
                int col = j;
                board[i][j].addActionListener(e -> {
                    if (hintedButton == board[row][col]) {
                        hintedButton.setBackground(null); // 恢復原本的顏色
                        hintedButton = null;
                    }
                    if (!gameWon && board[row][col].getText().isEmpty()) { // 若位置為空，紀錄動作並設置 XO
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
                            updateScore();

                            if (playerXWins == WINNING_GAMES || playerOWins == WINNING_GAMES) {
                                JOptionPane.showMessageDialog(null, currentPlayer + " wins the game!\nX Wins: " + playerXWins + " | O Wins: " + playerOWins);
                                resetGame();
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
                            resetTimer();
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

        startTimer();
    }

    public void startTimer() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (timeRemaining > 0) {
                    timeRemaining--;
                    timerLabel.setText("Time Remaining: " + timeRemaining + " seconds");
                } else {
                    // 時間到，切換玩家
                    JOptionPane.showMessageDialog(null, "Time's up! Switching turn to " + ((currentPlayer == 'X') ? 'O' : 'X'));
                    currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
                    statusBar.setText("Current Turn: " + currentPlayer);
                    resetTimer();
                }
            }
        }, 1000, 1000);
    }

    public void resetTimer() {
        timeRemaining = 15;
        timerLabel.setText("Time Remaining: " + timeRemaining + " seconds");
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
            resetTimer();
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
        resetTimer();
    }

    public void resetGame() {
        resetBoard();
        playerXWins = 0;
        playerOWins = 0;
        updateScore();
        JOptionPane.showMessageDialog(null, "遊戲結束! 請重新開始新的一局。");
    }

    public void updateScore() {
        scoreLabel.setText("Score - X: " + playerXWins + " | O: " + playerOWins + " | Draws: " + noOneWin);
    }

    // 添加到 GomokuGame 類中
    public Point suggestMove() {
        int bestScore = Integer.MIN_VALUE;
        Point bestMove = null;

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (board[i][j].getText().isEmpty()) {
                    // 嘗試在這個位置下子
                    board[i][j].setText(Character.toString(currentPlayer));
                    int score = evaluateBoard();
                    board[i][j].setText(""); // 恢復原狀

                    // 更新最佳分數和位置
                    if (score > bestScore) {
                        bestScore = score;
                        bestMove = new Point(i, j);
                    }
                }
            }
        }
        return bestMove;
    }

    // 評估棋盤狀態的方法
    public int evaluateBoard() {
        // 簡單評分邏輯：越接近贏得局面的分數越高
        int score = 0;

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (board[i][j].getText().equals(Character.toString(currentPlayer))) {
                    score += evaluatePosition(i, j, currentPlayer);
                } else if (board[i][j].getText().equals(Character.toString((currentPlayer == 'X') ? 'O' : 'X'))) {
                    score -= evaluatePosition(i, j, (currentPlayer == 'X') ? 'O' : 'X');
                }
            }
        }
        return score;
    }

    // 評估單一位置分數的方法
    public int evaluatePosition(int row, int col, char player) {
        int score = 0;

        // 檢查四個方向的連續子數
        score += countContinuous(row, col, 0, 1, player); // 橫向
        score += countContinuous(row, col, 1, 0, player); // 縱向
        score += countContinuous(row, col, 1, 1, player); // 對角線
        score += countContinuous(row, col, 1, -1, player); // 反對角線

        return score;
    }

    // 計算某方向連續棋子的數量
    public int countContinuous(int row, int col, int dx, int dy, char player) {
        int count = 0;

        int i = row + dx, j = col + dy;
        while (i >= 0 && i < 9 && j >= 0 && j < 9 && board[i][j].getText().equals(Character.toString(player))) {
            count++;
            i += dx;
            j += dy;
        }
        return count;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GomokuGame::new);
    }
}
