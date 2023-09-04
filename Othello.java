import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

// 石の色と状態を表す列挙型
enum Stone {
    BLACK, WHITE, EMPTY
}

// 座標を表すクラス
class Position {
    final int x; // x座標（行）
    final int y; // y座標（列）

    Position(int x, int y) {
        this.x = x;
        this.y = y;
    }
}

// 盤面に関するクラス
class Board implements Cloneable {
    
    // 8方向の移動量
    private static final int[] DX = {-1, 0, 1, -1, 1, -1, 0, 1};
    private static final int[] DY = {-1, -1, -1, 0, 0, 1, 1, 1};
    
    private final int BOARD_SIZE;
    private final Stone[][] boardState;

    public Board(int size) {
        this.BOARD_SIZE = size;
        this.boardState = new Stone[BOARD_SIZE][BOARD_SIZE];
    }

    // 盤面のコピーを返す
    public Board copy() {
        Board copyBoard = new Board(BOARD_SIZE);
        for (int i = 0; i < BOARD_SIZE; i++) {
            System.arraycopy(boardState[i], 0, copyBoard.boardState[i], 0, BOARD_SIZE);
        }
        return copyBoard;
    }

    // 盤面を初期化する
    public void initialize() {
        
        // 盤面を空にする
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                boardState[i][j] = Stone.EMPTY;
            }
        }

        // 中央の初期配置を行う
        boardState[BOARD_SIZE/2 - 1][BOARD_SIZE/2 - 1] = Stone.WHITE;
        boardState[BOARD_SIZE/2][BOARD_SIZE/2] = Stone.WHITE;
        boardState[BOARD_SIZE/2 - 1][BOARD_SIZE/2] = Stone.BLACK;
        boardState[BOARD_SIZE/2][BOARD_SIZE/2 - 1] = Stone.BLACK;        
    }

    // (x, y)に石を置いて，ひっくり返す
    public void flipStones(int x, int y, Stone player) {
        boardState[x][y] = player;
        for (int dir = 0; dir < DX.length; dir++) {
            if (isOpponentStoneInDirection(x, y, player, dir)) {
                if (isOwnStoneInLine(x, y, player, dir)) {
                    for (int i = x + DX[dir], j = y + DY[dir]; boardState[i][j] != player; i += DX[dir], j += DY[dir]) {
                        boardState[i][j] = player;
                    }
                }
            }
        }
    }

    // 合法手のリストを返す
    public List<Position> getValidMoves(Stone player) {
        List<Position> validMoves = new ArrayList<>();
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (boardState[i][j] == Stone.EMPTY && isValidMove(i, j, player)) {
                    validMoves.add(new Position(i, j));
                }
            }
        }
        return validMoves;
    }

    // (x, y)に石を置けるかどうかを返す
    public boolean isValidMove(int x, int y, Stone player) {
        for (int dir = 0; dir < DX.length; dir++) {
            if (isOpponentStoneInDirection(x, y, player, dir) && isOwnStoneInLine(x, y, player, dir)) {
                return true;
            }
        }
        return false;
    }

    // (x, y)のdir方向に自分の石があるかどうかを返す
    private boolean isOwnStoneInLine(int x, int y, Stone player, int dir) {
        for (int i = x + DX[dir], j = y + DY[dir]; isInsideBoard(i, j); i += DX[dir], j += DY[dir]) {
            if (boardState[i][j] == Stone.EMPTY) { // 空マスがあると挟んでひっくり返せない
                break;
            }
            if (boardState[i][j] == player) {
                return true;
            }
        }
        return false;
    }

    // (x, y)のdir方向に相手の石があるかどうかを返す
    private boolean isOpponentStoneInDirection(int x, int y, Stone player, int dir) {
        int nx = x + DX[dir], ny = y + DY[dir];
        return isInsideBoard(nx, ny) && boardState[nx][ny] != player && boardState[nx][ny] != Stone.EMPTY;
    }

    // (x, y)が盤面の範囲内かどうかを返す
    private boolean isInsideBoard(int x, int y) {
        return x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE;
    }

    // 相手の色を返す
    public Stone getOpponentColor(Stone color) {
        return color == Stone.BLACK ? Stone.WHITE : Stone.BLACK;
    }

    // (x, y)の石の色または状態を返す
    public Stone getStone(int x, int y) {
        return boardState[x][y];
    }

    // ゲームが終了しているかどうかを返す
    public boolean isGameOver() {
        return getValidMoves(Stone.BLACK).isEmpty() && getValidMoves(Stone.WHITE).isEmpty();
    }

    // 空マスの数を返す
    public int countEmpty() {
        int emptyCount = 0;
        for (Stone[] row : boardState) {
            for (Stone stone : row) {
                if (stone == Stone.EMPTY) {
                    emptyCount++;
                }
            }
        }
        return emptyCount;
    }

    // 石の数を返す
    public int countStones(Stone color) {
        int stoneCount = 0;
        for (Stone[] row : boardState) {
            for (Stone stone : row) {
                if (stone == color) {
                    stoneCount++;
                }
            }
        }
        return stoneCount;
    }

    // 取った角の数を返す
    public int countCorners(Stone color) {
        int cornerCount = 0;
        if (boardState[0][0] == color) {
            cornerCount++;
        }
        if (boardState[0][BOARD_SIZE - 1] == color) {
            cornerCount++;
        }
        if (boardState[BOARD_SIZE - 1][0] == color) {
            cornerCount++;
        }
        if (boardState[BOARD_SIZE - 1][BOARD_SIZE - 1] == color) {
            cornerCount++;
        }
        return cornerCount;
    }

    // 辺の上にある石の数を返す
    public int countEdges(Stone color) {
        int edgeCount = 0;
        for (int i = 1; i < BOARD_SIZE - 1; i++) {
            if (boardState[0][i] == color) {
                edgeCount++;
            }
            if (boardState[i][0] == color) {
                edgeCount++;
            }
            if (boardState[BOARD_SIZE - 1][i] == color) {
                edgeCount++;
            }
            if (boardState[i][BOARD_SIZE - 1] == color) {
                edgeCount++;
            }
        }
        return edgeCount;
    }
}


// Negascout法による事前探索の結果を格納するクラス
class PresearchResult {
    public Position bestMove;
    public double bestScore;
    public int startDepth;

    public PresearchResult(Position bestMove, double bestScore, int startDepth) {
        this.bestMove = bestMove;
        this.bestScore = bestScore;
        this.startDepth = startDepth;
    }
}


// Negascout法による探索を行うクラス
class NegascoutAgent {

    int size; // 盤面のサイズ
    int maxTine; // 探索時間の上限
    Stone playerColor;
    int startDepth; // 探索を開始する深さ
    Board board;
    double preBestScore;

    public NegascoutAgent(int size, int maxTime, Stone playerColor, int startDepth, Board board, double preBestScore) {
        this.size = size;
        this.maxTine = maxTime;
        this.playerColor = playerColor;
        this.startDepth = startDepth;
        this.board = board;
        this.preBestScore = preBestScore;
    }

    // 最善手を返す
    public Position selectMove() {
        long startTime = System.currentTimeMillis();
        int depth = startDepth;
        Position bestMove = new Position(-1, -1);
        double bestScore = Double.NEGATIVE_INFINITY;

        while (System.currentTimeMillis() - startTime < maxTine && depth <= board.countEmpty()) { // 探索時間の上限を超えるか，盤面が埋まるまで探索を続ける
            for (Position move : board.getValidMoves(playerColor)) { // すべての合法手に対して探索を行う
                Board boardCopy = board.copy();
                boardCopy.flipStones(move.x, move.y, playerColor);
                double score = -negascout(boardCopy, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, depth, board.getOpponentColor(playerColor), startTime);
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
            }
            depth++;
        }

        bestScore = (bestScore == Double.NEGATIVE_INFINITY) ? preBestScore : bestScore; // タイムアウトした場合は事前探索の結果を返す

        System.out.println("Computer eval Score: " + Math.round(bestScore * 100) / 100.0 + ", Searched depth: " + (depth - 1));
        return bestMove;
    }

    // Negascout法（https://ja.wikipedia.org/wiki/Negascout）
    public double negascout(Board board, double alpha, double beta, int depth, Stone color, long startTime) {        
        if (System.currentTimeMillis() - startTime > maxTine || board.isGameOver() || depth == 0) {
            return evaluate(board, color);
        }

        double b = beta;
        double score;
        boolean first = true;

        List<Position> validMoves = board.getValidMoves(color);
        if (validMoves.isEmpty()) { // パスの場合
            return -negascout(board, -beta, -alpha, depth - 1, board.getOpponentColor(color), startTime);
        }

        for (Position move : validMoves) {
            Board boardCopy = board.copy();
            boardCopy.flipStones(move.x, move.y, color);
            if (first) {
                // null window searchの準備
                score = -negascout(boardCopy, -b, -alpha, depth - 1, board.getOpponentColor(color), startTime);
                first = false;
            } else {
                // null window search
                score = -negascout(boardCopy, -alpha - 1, -alpha, depth - 1, board.getOpponentColor(color), startTime);
                if (alpha < score && score < beta) {
                    score = -negascout(boardCopy, -beta, -score, depth - 1, board.getOpponentColor(color), startTime);
                }
            }
            if (score > alpha) alpha = score;
            if (alpha >= beta) return alpha;
            b = alpha + 1;
        }
    
        return alpha;
    }

    // 評価関数
    private double evaluate(Board board, Stone color) {
        int emptyCount = board.countEmpty();
        int stoneScore = board.countStones(Stone.BLACK) - board.countStones(Stone.WHITE); // 石数の差
        int cornerScore = board.countCorners(Stone.BLACK) - board.countCorners(Stone.WHITE); // 角の数の差
        int edgeScore = board.countEdges(Stone.BLACK) - board.countEdges(Stone.WHITE); // 辺の数の差
        int mobilityScore = board.getValidMoves(playerColor).size() - board.getValidMoves(board.getOpponentColor(playerColor)).size(); // 着手可能手数の差
        double progressRate = 1 - (double) emptyCount / size / size; // 盤面の埋まり具合

        // ボードのサイズで正規化してある
        double score = stoneScore * (progressRate - 0.2) * 10 / size / size // 序盤は石数が少ない方が有利なので、重みは最初は負で，終盤は正にする
                        + cornerScore * (1.5 - progressRate) // 終盤は角の重要度が下がるので、progressRateの係数は負
                        + edgeScore * (1.5 - progressRate) / size // 終盤は辺の重要度が下がるので、progressRateの係数は負
                        + mobilityScore * progressRate / size / size; // 終盤は着手可能手数の重要度が上がるので、progressRateの係数は正

        if (color == Stone.WHITE) {
            score *= -1;
        }

        return score;
    }
}


// 事前探索を行うクラス（人間側が石を置くまでコンピュータの最善手を事前に計算する）
class Presearch extends NegascoutAgent implements Runnable {
    
    private PresearchResult[] presearchResult;
    private volatile boolean clicked = false;

    public Presearch(int size, int maxTime, Stone playerColor, int startDepth, Board board, double preBestScore) {
        super(size, maxTime, playerColor, startDepth, board, preBestScore);
    }

    public synchronized PresearchResult[] getResults() {
        return presearchResult;
    }

    synchronized void stopPresearch() {
        clicked = true;
    }

    // 排他制御（人間側が石を置いかどうかを返す）
    synchronized boolean isClicked() {
        return clicked;
    }

    public void run() {
        int depth = 1;
        presearchResult = new PresearchResult[size * size];
        final Stone humanColor = playerColor;
        final Stone computerColor = board.getOpponentColor(humanColor);

        while (depth <= board.countEmpty()) {
            // 人間側の各moveに対してコンピュータのbestMoveを計算し，その結果をpresearchResultに格納する
            for (Position humanMove : board.getValidMoves(humanColor)) {
                Board humanBoardCopy = board.copy();
                humanBoardCopy.flipStones(humanMove.x, humanMove.y, humanColor);

                // コンピュータのbestMoveを計算
                Position computerBestMove = new Position(-1, -1);
                double computerBestScore = Double.NEGATIVE_INFINITY;
                for (Position computerMove : humanBoardCopy.getValidMoves(computerColor)) {
                    Board computerBoardCopy = humanBoardCopy.copy();
                    computerBoardCopy.flipStones(computerMove.x, computerMove.y, computerColor);
                    double computerScore = -negascout(computerBoardCopy, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, depth, humanColor, Long.MAX_VALUE);
                    if (computerScore > computerBestScore) {
                        computerBestScore = computerScore;
                        computerBestMove = computerMove;
                    }
                    if (isClicked()) { // 人間側が石を置いたら探索を打ち切る
                        return;
                    }
                }

                // presearchResultに結果を格納
                int humanMoveIndex = humanMove.x * size + humanMove.y;
                presearchResult[humanMoveIndex] = new PresearchResult(computerBestMove, computerBestScore, depth);
            }
            depth++;
        }
    }
}


// ゲームのGUIを実装するクラス
public class Othello extends JFrame {

    private static final int FRAME_SIZE = 600;
    private int BOARD_SIZE; // 盤面のサイズ
    private int MAX_TIME; // 探索時間の上限
    private Stone humanColor;
    private Stone computerColor;
    private final JPanel[][] cells; // 盤面の各マスを表すJPanelの配列
    private final Board board;
    private Presearch presearch;
    private Position lastHumanMove = new Position(-1, -1); // 人間側の最後の手

    public static void main(String[] args) {
        new Othello();
    }
    
    public Othello() {
        configureGameSettings(); // ゲームの設定を最初にする
        this.cells = new JPanel[BOARD_SIZE][BOARD_SIZE];
        this.board = new Board(BOARD_SIZE);
        startGame();
        this.setVisible(true);
    }

    // ゲームの設定を行う
    private void configureGameSettings() {
        JTextField sizeField = new JTextField("8", 5); // デフォルトの盤面サイズは8
        JTextField timeField = new JTextField("0.5", 5); // デフォルトの探索時間は0.5秒
        JComboBox<String> colorBox = new JComboBox<>(new String[]{"Black", "White"});
        
        JPanel settingPanel = new JPanel();
        settingPanel.setLayout(new BoxLayout(settingPanel, BoxLayout.Y_AXIS));

        // 盤面のサイズを入力する
        JPanel sizePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sizePanel.add(new JLabel("Board size:"));
        sizePanel.add(sizeField);
        settingPanel.add(sizePanel);

        // 探索時間を入力する
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timePanel.add(new JLabel("Max time for computation (sec):"));
        timePanel.add(timeField);
        settingPanel.add(timePanel);

        // 自分の色を選択する
        JPanel colorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        colorPanel.add(new JLabel("Your color:"));
        colorPanel.add(colorBox);
        settingPanel.add(colorPanel);
        
        int result = JOptionPane.showConfirmDialog(null, settingPanel, "Setting", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                BOARD_SIZE = Integer.parseInt(sizeField.getText());
                MAX_TIME = (int) (Double.parseDouble(timeField.getText()) * 1000);
                humanColor = colorBox.getSelectedIndex() == 0 ? Stone.BLACK : Stone.WHITE;
                computerColor = humanColor == Stone.BLACK ? Stone.WHITE : Stone.BLACK;
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Invalid input! Please try again.");
                configureGameSettings();
            }
            if (BOARD_SIZE < 3 && MAX_TIME <= 0) { // 盤面のサイズは3以上，探索時間は0以上
                JOptionPane.showMessageDialog(null, "Invalid board size! Please try again.");
                configureGameSettings();
            }
        } else {
            System.exit(0);
        }
    }

    // ゲームを開始する
    private void startGame() {
        this.setTitle("Othello Game");
        this.setSize(FRAME_SIZE, FRAME_SIZE);
        this.setLayout(new GridLayout(BOARD_SIZE, BOARD_SIZE));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 盤面を初期化
        board.initialize();
        initializeCells();

        // 先手がコンピュータの場合は最初に石を置く
        if (computerColor == Stone.BLACK) {
            makeMove(computerColor);
        }

        updateBoard(-1, -1, Stone.BLACK);
    }

    // 盤面の各マスを初期化する
    private void initializeCells() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                cells[i][j] = createCell(i, j);
                this.add(cells[i][j]);
            }
        }
    }

    // 盤面の各マスを作成する
    private JPanel createCell(int i, int j) {
        JPanel cell = new JPanel(new BorderLayout());
        cell.setBorder(BorderFactory.createLineBorder(Color.black));
        cell.setBackground(Color.green);
        cell.addMouseListener(new CellClickListener(i, j));
        return cell;
    }

    // 盤面のマスをクリックしたときの処理
    class CellClickListener extends MouseAdapter {
        int x;
        int y;

        public CellClickListener(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public void mouseClicked(MouseEvent e) {
            // クリックされたらそのマスに石を置く
            if (board.getStone(x, y) == Stone.EMPTY && board.isValidMove(x, y, humanColor)) {
                lastHumanMove = new Position(x, y);
                board.flipStones(x, y, humanColor);
                if (presearch != null) {
                    presearch.stopPresearch();
                }
                updateBoard(x, y, computerColor);
                makeMove(computerColor);
            }
        }
    }

    // 交互に石を置く
    private void makeMove(Stone currentColor) {
        if (board.isGameOver()) { // ゲーム終了の場合
            displayGameResult();
            return;
        }
    
        List<Position> validMoves = board.getValidMoves(currentColor);
        if (validMoves.isEmpty()) { // パスの場合
            JOptionPane.showMessageDialog(null, (currentColor == Stone.BLACK ? "Black" : "White") + " Pass");
            Stone nextColor = board.getOpponentColor(currentColor);
            updateBoard(-1, -1, nextColor);
            makeMove(nextColor);
        } else if (currentColor == computerColor) { // コンピュータの番の場合
            SwingWorker<Position, Void> computerMoveWorker = new SwingWorker<>() {
                // コンピュータの最善手を計算している間はマスをクリックできないようにする
                protected Position doInBackground() {
                    disableCellListeners();
                    int startDepth = 1;
                    Position preBestMove = null;
                    double preBestScore = 0;
                    if (presearch != null) {
                        PresearchResult[] presearchResults = presearch.getResults();
                        int lastHumanMoveIndex = lastHumanMove.x * BOARD_SIZE + lastHumanMove.y;
                        startDepth = presearchResults[lastHumanMoveIndex].startDepth;
                        preBestMove = presearchResults[lastHumanMoveIndex].bestMove;
                        preBestScore = presearchResults[lastHumanMoveIndex].bestScore;
                    }
                    Position bestMove = new NegascoutAgent(BOARD_SIZE, MAX_TIME, computerColor, startDepth, board, preBestScore).selectMove();
                    presearch = null;
                    return (bestMove.x == -1) ? preBestMove : bestMove; // タイムアウトした場合は事前探索の結果を返す
                }
    
                // コンピュータの最善手を計算し終えたとき，その手を盤面に反映する
                protected void done() {
                    try {
                        Position move = get();
                        board.flipStones(move.x, move.y, computerColor);
                        enableCellListeners();
                        updateBoard(move.x, move.y, humanColor);
                        makeMove(humanColor);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            computerMoveWorker.execute();
        } else if (currentColor == humanColor) { // 人間の番の場合
            // スレッドを作成し，人間側が石を置くまでコンピュータの最善手を事前に計算する
            presearch = new Presearch(BOARD_SIZE, MAX_TIME, humanColor, 1, board, 0);
            Thread presearchThread = new Thread(presearch);
            presearchThread.start();
        }
    }
    
    // マスのクリックを無効化する
    private void disableCellListeners() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                cells[i][j].removeMouseListener(cells[i][j].getMouseListeners()[0]);
            }
        }
    }
    
    // マスのクリックを有効化する
    private void enableCellListeners() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                cells[i][j].addMouseListener(new CellClickListener(i, j));
            }
        }
    }    

    // 盤面を更新する
    private void updateBoard(int x, int y, Stone currentColor) {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                updateCells(i, j, x, y, currentColor);
            }
        }
    }

    // マスを更新する
    private void updateCells(int i, int j, int x, int y, Stone currentColor) {
        cells[i][j].removeAll();
        cells[i][j].setBackground(Color.GREEN);
        if (i == x && j == y) {
            // 最後に置いた石の場所のマスをオレンジにする
            cells[i][j].setBackground(Color.ORANGE);
        }
        if (board.getStone(i, j) != Stone.EMPTY) {
            // 色ごとに石を描画する
            cells[i][j].add(drawStone(board.getStone(i, j) == Stone.BLACK ? Color.black : Color.white));
        } else if (board.isValidMove(i, j, humanColor) && currentColor == humanColor) {
            // 人間の番のときは合法手の場所にマークをつける
            cells[i][j].add(drawHint());
        }
        cells[i][j].validate();
        cells[i][j].repaint();
    }

    // 石を描画する
    private JPanel drawStone(Color color) {
        JPanel panel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(color);
                g.fillOval(0, 0, getSize().width, getSize().height);
            }
        };
        panel.setOpaque(false);
        return panel;
    }

    // 合法手の場所にグレーのマークをつける
    private JPanel drawHint() {
        JPanel panel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.LIGHT_GRAY);
                int radius = getSize().width / 4;
                int x = (getSize().width - radius) / 2;
                int y = (getSize().height - radius) / 2;
                g.fillOval(x, y, radius, radius);
            }
        };
        panel.setOpaque(false);
        return panel;
    }

    // ゲームの結果（各色の石の数，勝者）を表示する
    private void displayGameResult() {
        int blackStoneCount = board.countStones(Stone.BLACK);
        int whiteStoneCount = board.countStones(Stone.WHITE);
        
        String winner;
        if ((humanColor == Stone.BLACK && blackStoneCount > whiteStoneCount) || (humanColor == Stone.WHITE && whiteStoneCount > blackStoneCount)) {
            winner = "You";
        } else if ((humanColor == Stone.BLACK && blackStoneCount < whiteStoneCount) || (humanColor == Stone.WHITE && whiteStoneCount < blackStoneCount)) {
            winner = "Computer";
        } else {
            winner = "Draw";
        }

        int n = JOptionPane.showConfirmDialog(null, "Black: " + blackStoneCount + ", White: " + whiteStoneCount + ". Winner: " + winner + "\nNew Game?", "Game Over", JOptionPane.YES_NO_OPTION);
        if (n == 0) {
            this.dispose();
            new Othello();
        } else {
            System.exit(0);
        }
    }
}
