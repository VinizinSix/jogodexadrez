package view;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

// --- CLASSES DE LÓGICA DO JOGO ---

// Representa uma posição no tabuleiro
@SuppressWarnings("unused")
class Position {
    private final int row, col;

    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return col;
    }
    
    // Verifica se a posição está dentro do tabuleiro
    public boolean isValid() {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return row == position.row && col == position.col;
    }

    @Override
    public int hashCode() {
        return 31 * row + col;
    }
    
    @Override
    public String toString() {
        return "" + (char) ('a' + col) + (8 - row);
    }
}

// Representa um movimento
class Move {
    final Position from, to;
    int score = 0;

    Move(Position f, Position t) {
        this.from = f;
        this.to = t;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Move move = (Move) o;
        return from.equals(move.from) && to.equals(move.to);
    }

    @Override
    public int hashCode() {
        return 31 * from.hashCode() + to.hashCode();
    }
}


// Classe base para todas as peças
abstract class Piece implements Cloneable {
    private boolean isWhite;
    protected String symbol;

    public Piece(boolean isWhite, String symbol) {
        this.isWhite = isWhite;
        this.symbol = symbol;
    }

    public boolean isWhite() {
        return isWhite;
    }

    public String getSymbol() {
        return symbol;
    }
    
    // Lista os movimentos pseudolegais (sem considerar o xeque)
    public abstract List<Position> getPseudoLegalMoves(Board board, Position from);
    
    // Clona a peça
    @Override
    public Piece clone() {
        try {
            return (Piece) super.clone();
        } catch (CloneNotSupportedException e) {
            return null; // Nunca deve acontecer
        }
    }
}

// Implementações de peças
class Pawn extends Piece {
    public Pawn(boolean isWhite) { super(isWhite, isWhite ? "P" : "p"); }
    public List<Position> getPseudoLegalMoves(Board board, Position from) {
        List<Position> moves = new ArrayList<>();
        int dir = isWhite() ? -1 : 1;
        
        // Movimento para frente
        Position forwardOne = new Position(from.getRow() + dir, from.getColumn());
        if (forwardOne.isValid() && board.get(forwardOne) == null) {
            moves.add(forwardOne);
            
            // Movimento inicial de 2 casas
            if ((isWhite() && from.getRow() == 6) || (!isWhite() && from.getRow() == 1)) {
                Position forwardTwo = new Position(from.getRow() + 2 * dir, from.getColumn());
                if (board.get(forwardTwo) == null) {
                    moves.add(forwardTwo);
                }
            }
        }
        
        // Capturas
        int[] captureCols = {-1, 1};
        for (int c : captureCols) {
            Position capturePos = new Position(from.getRow() + dir, from.getColumn() + c);
            if (capturePos.isValid()) {
                Piece target = board.get(capturePos);
                if (target != null && target.isWhite() != isWhite()) {
                    moves.add(capturePos);
                }
            }
        }
        return moves;
    }
}
class Knight extends Piece {
    public Knight(boolean isWhite) { super(isWhite, isWhite ? "N" : "n"); }
    public List<Position> getPseudoLegalMoves(Board board, Position from) {
        List<Position> moves = new ArrayList<>();
        int[] dr = {-2, -2, -1, -1, 1, 1, 2, 2};
        int[] dc = {-1, 1, -2, 2, -2, 2, -1, 1};
        for(int i=0; i<8; i++) {
            Position to = new Position(from.getRow() + dr[i], from.getColumn() + dc[i]);
            if (to.isValid()) {
                Piece target = board.get(to);
                if (target == null || target.isWhite() != isWhite()) {
                    moves.add(to);
                }
            }
        }
        return moves;
    }
}
class Bishop extends Piece {
    public Bishop(boolean isWhite) { super(isWhite, isWhite ? "B" : "b"); }
    public List<Position> getPseudoLegalMoves(Board board, Position from) {
        List<Position> moves = new ArrayList<>();
        int[] dr = {1, 1, -1, -1};
        int[] dc = {1, -1, 1, -1};
        for(int i=0; i<4; i++) {
            int r = from.getRow() + dr[i];
            int c = from.getColumn() + dc[i];
            while(r >= 0 && r < 8 && c >= 0 && c < 8) {
                Position to = new Position(r, c);
                Piece target = board.get(to);
                if (target == null) {
                    moves.add(to);
                } else {
                    if (target.isWhite() != isWhite()) {
                        moves.add(to);
                    }
                    break;
                }
                r += dr[i];
                c += dc[i];
            }
        }
        return moves;
    }
}
class Rook extends Piece {
    public Rook(boolean isWhite) { super(isWhite, isWhite ? "R" : "r"); }
    public List<Position> getPseudoLegalMoves(Board board, Position from) {
        List<Position> moves = new ArrayList<>();
        int[] dr = {0, 0, 1, -1};
        int[] dc = {1, -1, 0, 0};
        for(int i=0; i<4; i++) {
            int r = from.getRow() + dr[i];
            int c = from.getColumn() + dc[i];
            while(r >= 0 && r < 8 && c >= 0 && c < 8) {
                Position to = new Position(r, c);
                Piece target = board.get(to);
                if (target == null) {
                    moves.add(to);
                } else {
                    if (target.isWhite() != isWhite()) {
                        moves.add(to);
                    }
                    break;
                }
                r += dr[i];
                c += dc[i];
            }
        }
        return moves;
    }
}
class Queen extends Piece {
    public Queen(boolean isWhite) { super(isWhite, isWhite ? "Q" : "q"); }
    public List<Position> getPseudoLegalMoves(Board board, Position from) {
        List<Position> moves = new ArrayList<>();
        moves.addAll(new Rook(isWhite()).getPseudoLegalMoves(board, from));
        moves.addAll(new Bishop(isWhite()).getPseudoLegalMoves(board, from));
        return moves;
    }
}
class King extends Piece {
    public King(boolean isWhite) { super(isWhite, isWhite ? "K" : "k"); }
    public List<Position> getPseudoLegalMoves(Board board, Position from) {
        List<Position> moves = new ArrayList<>();
        for(int dr = -1; dr <= 1; dr++) {
            for(int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                Position to = new Position(from.getRow() + dr, from.getColumn() + dc);
                if (to.isValid()) {
                    Piece target = board.get(to);
                    if (target == null || target.isWhite() != isWhite()) {
                        moves.add(to);
                    }
                }
            }
        }
        return moves;
    }
}

// Representa o tabuleiro
class Board implements Cloneable {
    private Piece[][] board;

    public Board() {
        board = new Piece[8][8];
        setup();
    }

    // Clona o tabuleiro
    @Override
    public Board clone() {
        Board newBoard = new Board();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece piece = this.board[r][c];
                newBoard.board[r][c] = (piece != null) ? piece.clone() : null;
            }
        }
        return newBoard;
    }

    // Configuração inicial do tabuleiro
    public void setup() {
        // Zera o tabuleiro
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                board[r][c] = null;
            }
        }
        // Peças brancas
        board[7][0] = new Rook(true);
        board[7][1] = new Knight(true);
        board[7][2] = new Bishop(true);
        board[7][3] = new Queen(true);
        board[7][4] = new King(true);
        board[7][5] = new Bishop(true);
        board[7][6] = new Knight(true);
        board[7][7] = new Rook(true);
        for (int c = 0; c < 8; c++) {
            board[6][c] = new Pawn(true);
        }

        // Peças pretas
        board[0][0] = new Rook(false);
        board[0][1] = new Knight(false);
        board[0][2] = new Bishop(false);
        board[0][3] = new Queen(false);
        board[0][4] = new King(false);
        board[0][5] = new Bishop(false);
        board[0][6] = new Knight(false);
        board[0][7] = new Rook(false);
        for (int c = 0; c < 8; c++) {
            board[1][c] = new Pawn(false);
        }
    }

    public Piece get(Position pos) {
        if (!pos.isValid()) {
            return null;
        }
        return board[pos.getRow()][pos.getColumn()];
    }

    public void set(Position pos, Piece piece) {
        if (!pos.isValid()) {
            return;
        }
        board[pos.getRow()][pos.getColumn()] = piece;
    }
}

// Lógica do jogo
class Game implements Cloneable {
    private Board board;
    private boolean whiteToMove;
    private List<String> history;
    private boolean isGameOver;

    public Game() {
        newGame();
    }

    @Override
    public Game clone() {
        Game tempGame = new Game();
        tempGame.board = this.board.clone();
        tempGame.whiteToMove = this.whiteToMove;
        tempGame.history = new ArrayList<>(this.history);
        tempGame.isGameOver = this.isGameOver;
        return tempGame;
    }

    public void newGame() {
        this.board = new Board();
        this.whiteToMove = true;
        this.history = new ArrayList<>();
        this.isGameOver = false;
    }
    
    // Obtém todos os movimentos legais para a vez atual
    public List<Move> getLegalMoves() {
        List<Move> legalMoves = new ArrayList<>();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position from = new Position(r, c);
                Piece piece = board.get(from);
                if (piece != null && piece.isWhite() == whiteToMove) {
                    for (Position to : piece.getPseudoLegalMoves(board, from)) {
                        // Simula o movimento para verificar se o Rei ficará em xeque
                        Game tempGame = this.clone();
                        tempGame.board().set(to, tempGame.board().get(from));
                        tempGame.board().set(from, null);

                        if (!tempGame.inCheck(whiteToMove)) {
                            legalMoves.add(new Move(from, to));
                        }
                    }
                }
            }
        }
        return legalMoves;
    }

    public void move(Position from, Position to, Character promo) {
        Piece piece = board.get(from);
        if (piece == null) return;
        
        // Simula o movimento
        board.set(to, piece);
        board.set(from, null);

        // Adiciona ao histórico (notação simplificada)
        String moveStr = from.toString() + " -> " + to.toString();
        history.add(moveStr);
        
        // Alterna o turno
        whiteToMove = !whiteToMove;
        
        // Verifica se o jogo acabou
        if (getLegalMoves().isEmpty()) {
            isGameOver = true;
        }
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public boolean whiteToMove() {
        return whiteToMove;
    }

    public boolean inCheck(boolean isWhite) {
        Position kingPos = findKing(isWhite);
        if (kingPos == null) return false;

        boolean opponentColor = !isWhite;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position from = new Position(r, c);
                Piece piece = board.get(from);
                if (piece != null && piece.isWhite() == opponentColor) {
                    if (piece.getPseudoLegalMoves(board, from).contains(kingPos)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    // Encontra a posição do Rei da cor especificada
    public Position findKing(boolean isWhite) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position pos = new Position(r, c);
                Piece p = board.get(pos);
                if (p instanceof King && p.isWhite() == isWhite) {
                    return pos;
                }
            }
        }
        return null; // Não deveria acontecer em um jogo válido
    }
    
    public Board board() {
        return board;
    }

    public List<String> history() {
        return history;
    }
    
    // Verifica se um movimento é uma captura
    public boolean isCaptura(Position to) {
        return board.get(to) != null;
    }

    // Verifica se um movimento é uma promoção
    public boolean isPromotion(Position from, Position to) {
        Piece p = board.get(from);
        if (p instanceof Pawn) {
            return (p.isWhite() && to.getRow() == 0) || (!p.isWhite() && to.getRow() == 7);
        }
        return false;
    }
}

// Classe de utilidade para ícones de peças (adicionada para compatibilidade)
class ImageUtil {
    public static ImageIcon getPieceIcon(boolean isWhite, char symbol, int size) {
        // Este é um método de demonstração. Em um jogo real, você usaria
        // arquivos de imagem (por exemplo, SVG ou PNG) aqui.
        return null;
    }
}

// --- CLASSE PRINCIPAL ---

public class ChessGUI extends JFrame {
    private static final long serialVersionUID = 1L;

    // --- Config de cores/styles ---
    private static final Color LIGHT_SQ = new Color(240, 217, 181);
    private static final Color DARK_SQ = new Color(181, 136, 99);
    private static final Color HILITE_SELECTED = new Color(135, 205, 255);
    private static final Color HILITE_LEGAL = new Color(0, 0, 255);
    private static final Color HILITE_LASTMOVE = new Color(0, 0, 139);
    private static final Color HILITE_CHECK_LEGAL = new Color(255, 165, 0); // Laranja para xeque

    private static final Border BORDER_SELECTED = new MatteBorder(2, 2, 2, 2, HILITE_SELECTED);
    private static final Border BORDER_LEGAL = new MatteBorder(2, 2, 2, 2, HILITE_LEGAL);
    private static final Border BORDER_LASTMOVE = new MatteBorder(2, 2, 2, 2, HILITE_LASTMOVE);

    private final Game game;

    private final JPanel boardPanel;
    private final JButton[][] squares = new JButton[8][8];

    private final JLabel status;
    private final JTextArea history;
    private final JScrollPane historyScroll;

    // Menu / controles
    private JCheckBoxMenuItem pcAsBlack;
    private JSpinner depthSpinner;
    private JMenuItem newGameItem, quitItem;

    // Seleção atual e movimentos legais
    private Position selected = null;
    private List<Position> legalForSelected = new ArrayList<>();

    // Realce do último lance
    private Position lastFrom = null, lastTo = null;

    // IA
    private boolean aiThinking = false;
    private final Map<String, Move> openingBook = new HashMap<>();


    @SuppressWarnings("unused")
    public ChessGUI() {
        super("ChessGame");

        // Look&Feel Nimbus
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception ignored) {
        }
        
        // Carrega o livro de aberturas
        loadOpeningBook();

        this.game = new Game();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        // Menu
        setJMenuBar(buildMenuBar());

        // Painel do tabuleiro (8x8)
        boardPanel = new JPanel(new GridLayout(8, 8, 0, 0));
        boardPanel.setBackground(Color.DARK_GRAY);
        boardPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        // Cria botões das casas
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                final int rr = r;
                final int cc = c;
                JButton b = new JButton();
                b.setMargin(new Insets(0, 0, 0, 0));
                b.setFocusPainted(false);
                b.setOpaque(true);
                b.setBorderPainted(true);
                b.setContentAreaFilled(true);
                b.setFont(b.getFont().deriveFont(Font.BOLD, 24f));
                b.addActionListener(e -> handleClick(new Position(rr, cc)));
                squares[r][c] = b;
                boardPanel.add(b);
            }
        }

        // Barra inferior de status
        status = new JLabel("Sua Vez: Brancas");
        status.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        // Histórico
        history = new JTextArea(14, 22);
        history.setEditable(false);
        history.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        historyScroll = new JScrollPane(history);

        // Layout principal: tabuleiro à esquerda, histórico à direita
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // fundo gradiente azul claro
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, new Color(180, 210, 255), 0, getHeight(), new Color(220, 240, 255)));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel histLabel = new JLabel("Histórico de Movimentos:");
        histLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        histLabel.setForeground(new Color(30, 60, 120));
        histLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));

        rightPanel.add(histLabel, BorderLayout.NORTH);

        // painel do histórico com borda arredondada
        JScrollPane historyScrollPane = new JScrollPane(historyScroll);
        historyScrollPane.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 200), 2, true));
        rightPanel.add(historyScrollPane, BorderLayout.CENTER);

        // controles inferiores com botões coloridos
        JPanel controlsPanel = buildSideControls();
        controlsPanel.setBackground(new Color(200, 220, 255));
        for (Component comp : controlsPanel.getComponents()) {
            if (comp instanceof JButton btn) {
                btn.setBackground(new Color(100, 150, 255));
                btn.setForeground(Color.WHITE);
                btn.setFocusPainted(false);
            }
        }
        rightPanel.add(controlsPanel, BorderLayout.SOUTH);

        add(boardPanel, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        add(rightPanel, BorderLayout.EAST);

        // Atualiza ícones conforme a janela/painel muda de tamanho
        boardPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                refresh();
            }
        });

        setMinimumSize(new Dimension(920, 680));
        setLocationRelativeTo(null);

        // Atalhos: Ctrl+N, Ctrl+Q
        setupAccelerators();

        setVisible(true);
        refresh();
        maybeTriggerAI();
    }

    // ----------------- Menus e controles -----------------

    @SuppressWarnings("unused")
    private JMenuBar buildMenuBar() {
        JMenuBar mb = new JMenuBar();

        JMenu gameMenu = new JMenu("Jogo");

        newGameItem = new JMenuItem("Clique para um novo jogo");
        newGameItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        newGameItem.addActionListener(e -> doNewGame());

        pcAsBlack = new JCheckBoxMenuItem("Seu adversario joga com as Pretas");
        pcAsBlack.setSelected(true); // IA joga com as pretas por padrão

        JMenu depthMenu = new JMenu("Profundidade IA");
        // Aumentando a profundidade padrão para 3
        depthSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 4, 1));
        depthSpinner.setToolTipText("Profundidade de busca da IA");
        depthMenu.add(depthSpinner);

        quitItem = new JMenuItem("Sair");
        quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        quitItem.addActionListener(e -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));

        gameMenu.add(newGameItem);
        gameMenu.addSeparator();
        gameMenu.add(pcAsBlack);
        gameMenu.add(depthMenu);
        gameMenu.addSeparator();
        gameMenu.add(quitItem);

        mb.add(gameMenu);
        return mb;
    }

    @SuppressWarnings("unused")
    private JPanel buildSideControls() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JButton btnNew = new JButton("Novo Jogo");
        btnNew.addActionListener(e -> doNewGame());
        panel.add(btnNew);

        JCheckBox cb = new JCheckBox("Adversário (Pretas)");
        cb.setSelected(pcAsBlack.isSelected());
        cb.addActionListener(e -> pcAsBlack.setSelected(cb.isSelected()));
        panel.add(cb);

        panel.add(new JLabel("IA:"));
        int curDepth = ((Integer) depthSpinner.getValue()).intValue();
        JSpinner sp = new JSpinner(new SpinnerNumberModel(curDepth, 1, 4, 1));
        sp.addChangeListener(e -> depthSpinner.setValue(sp.getValue()));
        panel.add(sp);

        return panel;
    }

    private void setupAccelerators() {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), "newGame");
        getRootPane().getActionMap().put("newGame", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doNewGame();
            }
        });

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), "quit");
        getRootPane().getActionMap().put("quit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispatchEvent(new WindowEvent(ChessGUI.this, WindowEvent.WINDOW_CLOSING));
            }
        });
    }

    private void doNewGame() {
        selected = null;
        legalForSelected.clear();
        lastFrom = lastTo = null;
        aiThinking = false;
        game.newGame();
        refresh();
        maybeTriggerAI();
    }

    // ----------------- Interação de tabuleiro -----------------

    private void handleClick(Position clicked) {
        if (game.isGameOver() || aiThinking) return;
        if (pcAsBlack.isSelected() && !game.whiteToMove()) return;

        Piece p = game.board().get(clicked);

        if (selected == null) {
            if (p != null && p.isWhite() == game.whiteToMove()) {
                selected = clicked;
                legalForSelected = new ArrayList<>();
                for(Move m : game.getLegalMoves()) {
                    if (m.from.equals(selected)) {
                        legalForSelected.add(m.to);
                    }
                }
            }
        } else {
            // Se o movimento for legal
            Move playerMove = new Move(selected, clicked);
            List<Move> allLegalMoves = game.getLegalMoves();
            boolean isLegal = allLegalMoves.contains(playerMove);
            
            if (isLegal) {
                Character promo = null;
                Piece moving = game.board().get(selected);
                if (moving instanceof Pawn && game.isPromotion(selected, clicked)) {
                    promo = askPromotion();
                }
                lastFrom = selected;
                lastTo = clicked;

                game.move(selected, clicked, promo);

                selected = null;
                legalForSelected.clear();

                refresh();
                maybeAnnounceEnd();
                maybeTriggerAI();
            } else if (p != null && p.isWhite() == game.whiteToMove()) {
                // Nova seleção de peça
                selected = clicked;
                legalForSelected = new ArrayList<>();
                for(Move m : game.getLegalMoves()) {
                    if (m.from.equals(selected)) {
                        legalForSelected.add(m.to);
                    }
                }
            } else {
                selected = null;
                legalForSelected.clear();
            }
        }
        refresh();
    }

    private Character askPromotion() {
        String[] opts = {"Rainha", "Torre", "Bispo", "Cavalo"};
        int ch = JOptionPane.showOptionDialog(
                this,
                "Escolha a peça para mudar:",
                "Mudar",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opts,
                opts[0]
        );
        return switch (ch) {
            case 1 -> 'R';
            case 2 -> 'B';
            case 3 -> 'N';
            default -> 'Q';
        };
    }

    // ----------------- IA (Minimax com Poda Alpha-Beta) -----------------
    
    private void maybeTriggerAI() {
        if (game.isGameOver()) return;
        if (!pcAsBlack.isSelected()) return;
        if (game.whiteToMove()) return;

        aiThinking = true;
        status.setText("Vez: Pretas — Adversário pensando...");

        new SwingWorker<Move, Void>() {
            @Override
            protected Move doInBackground() {
                // Tenta encontrar uma jogada no livro de aberturas
                Move openingMove = getOpeningBookMove();
                
                if (openingMove != null) {
                    return openingMove;
                }
                
                // Se não houver, usa a avaliação Minimax
                return findBestMove();
            }

            @Override
            protected void done() {
                Move bestMove;
                try {
                    bestMove = get();
                } catch (Exception e) {
                    // Em caso de erro, usa um movimento aleatório
                    bestMove = getRandomMove();
                }

                if (bestMove != null && !game.isGameOver() && !game.whiteToMove()) {
                    lastFrom = bestMove.from;
                    lastTo = bestMove.to;
                    Character promo = null;
                    Piece moving = game.board().get(bestMove.from);
                    if (moving instanceof Pawn && game.isPromotion(bestMove.from, bestMove.to)) {
                        promo = 'Q';
                    }
                    game.move(bestMove.from, bestMove.to, promo);
                }
                aiThinking = false;
                refresh();
                maybeAnnounceEnd();
            }
        }.execute();
    }
    
    // Encontra o melhor movimento usando Minimax
    private Move findBestMove() {
        int depth = ((Integer) depthSpinner.getValue()).intValue();
        List<Move> allMoves = game.getLegalMoves();
        if (allMoves.isEmpty()) return null;

        // Prioriza xeque-mate imediato
        for(Move move : allMoves) {
            Game tempGame = game.clone();
            tempGame.move(move.from, move.to, null);
            if (tempGame.isGameOver() && tempGame.inCheck(!tempGame.whiteToMove())) {
                return move;
            }
        }

        int bestScore = Integer.MIN_VALUE;
        Move bestMove = null;

        for (Move move : allMoves) {
            Game tempGame = game.clone();
            tempGame.move(move.from, move.to, null); // Move a peça
            
            // Chama o minimax para o próximo nível (vez do jogador)
            int score = minimax(tempGame, depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false);
            
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        return bestMove;
    }

    // Algoritmo Minimax com Poda Alpha-Beta
    private int minimax(Game boardState, int depth, int alpha, int beta, boolean isMaximizingPlayer) {
        if (depth == 0 || boardState.isGameOver()) {
            return evaluateBoard(boardState);
        }

        if (isMaximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;
            for (Move move : boardState.getLegalMoves()) {
                Game tempGame = boardState.clone();
                tempGame.move(move.from, move.to, null);
                int eval = minimax(tempGame, depth - 1, alpha, beta, false);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) {
                    break;
                }
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (Move move : boardState.getLegalMoves()) {
                Game tempGame = boardState.clone();
                tempGame.move(move.from, move.to, null);
                int eval = minimax(tempGame, depth - 1, alpha, beta, true);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) {
                    break;
                }
            }
            return minEval;
        }
    }
    
    // Avalia a pontuação do tabuleiro
    private int evaluateBoard(Game game) {
        if (game.isGameOver()) {
            if (game.inCheck(game.whiteToMove())) {
                return game.whiteToMove() ? -999999 : 999999; // Xeque-mate
            } else {
                return 0; // Empate
            }
        }
        
        int score = 0;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = game.board().get(new Position(r,c));
                if (p != null) {
                    int pieceValue = getPieceValue(p);
                    // Adiciona valor de posição (controle do centro)
                    int posValue = getPositionValue(r, c, p.isWhite());
                    score += p.isWhite() ? (pieceValue + posValue) : -(pieceValue + posValue);
                }
            }
        }

        // Adiciona um bônus se o oponente estiver em xeque
        if (game.inCheck(!game.whiteToMove())) {
            score += 500;
        }
        
        return score;
    }

    private int getPieceValue(Piece p) {
        if (p == null) return 0;
        return switch (p.getSymbol().toUpperCase()) {
            case "P" -> 100;
            case "N" -> 320;
            case "B" -> 330;
            case "R" -> 500;
            case "Q" -> 900;
            case "K" -> 20000;
            default -> 0;
        };
    }
    
    // Adiciona valor para controle do centro do tabuleiro
    private int getPositionValue(int r, int c, boolean isWhite) {
        // Pontuação extra para as 4 casas centrais
        if ((r == 3 || r == 4) && (c == 3 || c == 4)) {
            return 30;
        }
        // Pontuação extra para as 12 casas ao redor
        if ((r >= 2 && r <= 5) && (c >= 2 && c <= 5)) {
            return 10;
        }
        return 0;
    }
    
    private Move getRandomMove() {
        List<Move> moves = game.getLegalMoves();
        if (moves.isEmpty()) return null;
        return moves.get(new Random().nextInt(moves.size()));
    }
    
    // --- Lógica do Livro de Aberturas ---
    private void loadOpeningBook() {
        addOpeningSequence("e2-e4", "e7-e5", "g1-f3", "b8-c6", "f1-b5"); // Ruy López
        addOpeningSequence("e2-e4", "c7-c5", "g1-f3", "d7-d6", "d2-d4"); // Defesa Siciliana
        addOpeningSequence("c2-c4", "e7-e5", "b1-c3"); // Abertura Inglesa
        addOpeningSequence("e2-e4", "e7-e6", "d2-d4", "d7-d5"); // Defesa Francesa
    }

    private void addOpeningSequence(String... moves) {
        StringBuilder historyBuilder = new StringBuilder();
        for (int i = 0; i < moves.length - 1; i++) {
            historyBuilder.append(moves[i]).append(",");
            openingBook.put(historyBuilder.toString(), convertMoveString(moves[i + 1]));
        }
    }

    private Move convertMoveString(String moveString) {
        String[] parts = moveString.split("-");
        String fromStr = parts[0];
        String toStr = parts[1];
        
        Position from = new Position(8 - (fromStr.charAt(1) - '0'), fromStr.charAt(0) - 'a');
        Position to = new Position(8 - (toStr.charAt(1) - '0'), toStr.charAt(0) - 'a');
        
        return new Move(from, to);
    }
    
    private Move getOpeningBookMove() {
        StringBuilder currentHistory = new StringBuilder();
        for(String moveStr : game.history()) {
            // Converte a notação "a2 -> a4" para "a2-a4,"
            String converted = moveStr.replace(" -> ", "-") + ",";
            currentHistory.append(converted);
        }
        return openingBook.get(currentHistory.toString());
    }
    
    private void refresh() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                boolean light = (r + c) % 2 == 0;
                Color base = light ? LIGHT_SQ : DARK_SQ;
                JButton b = squares[r][c];
                b.setBackground(base);
                b.setBorder(null);
                b.setToolTipText(null);
            }
        }

        if (lastFrom != null) squares[lastFrom.getRow()][lastFrom.getColumn()].setBorder(BORDER_LASTMOVE);
        if (lastTo != null) squares[lastTo.getRow()][lastTo.getColumn()].setBorder(BORDER_LASTMOVE);

        // Destaca todos os movimentos legais quando em xeque, OU apenas os da peça selecionada
        if (game.inCheck(game.whiteToMove())) {
            for (Move m : game.getLegalMoves()) {
                squares[m.to.getRow()][m.to.getColumn()].setBorder(new MatteBorder(2, 2, 2, 2, HILITE_CHECK_LEGAL));
            }
        } else if (selected != null) {
            squares[selected.getRow()][selected.getColumn()].setBorder(BORDER_SELECTED);
            for (Position d : legalForSelected) {
                squares[d.getRow()][d.getColumn()].setBorder(BORDER_LEGAL);
            }
        }

        int iconSize = computeSquareIconSize();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = game.board().get(new Position(r, c));
                JButton b = squares[r][c];

                if (p == null) {
                    b.setIcon(null);
                    b.setText("");
                    continue;
                }

                char sym = p.getSymbol().charAt(0);
                ImageIcon icon = ImageUtil.getPieceIcon(p.isWhite(), sym, iconSize);
                if (icon != null) {
                    b.setIcon(icon);
                    b.setText("");
                } else {
                    b.setIcon(null);
                    b.setText(toUnicode(p.getSymbol(), p.isWhite()));
                }
            }
        }

        String side = game.whiteToMove() ? "Brancas" : "Pretas";
        String chk = game.inCheck(game.whiteToMove()) ? " — Xeque!" : "";
        if (aiThinking) chk = " — Adversário pensando...";
        status.setText("Vez: " + side + chk);

        StringBuilder sb = new StringBuilder();
        var hist = game.history();
        for (int i = 0; i < hist.size(); i++) {
            if (i % 2 == 0) sb.append((i / 2) + 1).append('.').append(' ');
            sb.append(hist.get(i)).append(' ');
            if (i % 2 == 1) sb.append('\n');
        }
        history.setText(sb.toString());
        history.setCaretPosition(history.getDocument().getLength());
    }

    private void maybeAnnounceEnd() {
        if (!game.isGameOver()) return;
        String msg;
        if (game.inCheck(game.whiteToMove())) {
            msg = "Xeque-mate! " + (game.whiteToMove() ? "Brancas" : "Pretas") + " estão em mate.";
        } else {
            msg = "Empate por afogamento (stalemate).";
        }
        JOptionPane.showMessageDialog(this, msg, "Fim de Jogo", JOptionPane.INFORMATION_MESSAGE);
    }

    private String toUnicode(String sym, boolean white) {
        return switch (sym) {
            case "K" -> white ? "\u2654" : "\u265A";
            case "Q" -> white ? "\u2655" : "\u265B";
            case "R" -> white ? "\u2656" : "\u265C";
            case "B" -> white ? "\u2657" : "\u265D";
            case "N" -> white ? "\u2658" : "\u265E";
            case "P" -> white ? "\u2659" : "\u265F";
            default -> "";
        };
    }

    private int computeSquareIconSize() {
        JButton b = squares[0][0];
        int w = Math.max(1, b.getWidth());
        int h = Math.max(1, b.getHeight());
        int side = Math.min(w, h);
        if (side <= 1) return 64;
        return Math.max(24, side - 8);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChessGUI::new);
    }
}
