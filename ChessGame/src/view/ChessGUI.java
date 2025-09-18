package view;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
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
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.swing.*;

// --- CLASSES DE LÓGICA DO JOGO (MANTIDAS) ---

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
// CORREÇÃO: Estrutura da classe ImageUtil
class ImageUtil {
    private static final Map<String, ImageIcon> cache = new HashMap<>();

    public static ImageIcon getPieceIcon(boolean isWhite, char symbol, int size) {
        // Chave de cache baseada na peça e no tamanho para evitar recarregar
        String key = (isWhite ? "w" : "b") + Character.toUpperCase(symbol) + "_" + size;
        if (cache.containsKey(key)) return cache.get(key);

        String imgName = (isWhite ? "w" : "b") + Character.toUpperCase(symbol) + ".png";
        java.net.URL url = null;
        
        try {
            // Tenta o caminho absoluto (Assumindo que sua pasta 'img' está na raiz do Classpath)
            url = ChessGUI.class.getResource("/img/" + imgName);

            if (url == null) {
                // Tenta um fallback sem a barra inicial, que funcionará se 'img' e 'view'
                // estiverem no mesmo nível no Classpath (que é como a sua estrutura 'src' sugere)
                url = ChessGUI.class.getResource("img/" + imgName);
            }

            if (url == null) {
                // Mensagem de erro de depuração aprimorada
                System.err.println("ERRO GRAVE: Não encontrei a imagem no classpath. Tentei: /img/" + imgName + " e img/" + imgName);
                return null;
            }

            // 🛑 CORREÇÃO ESSENCIAL 1: Use a 'url' encontrada e VÁRIAVEL 🛑
            // Em vez de usar um caminho fixo, use a URL que foi encontrada acima.
            ImageIcon icon = new ImageIcon(url);
            
            // 🛑 CORREÇÃO ESSENCIAL 2: Redimensiona a imagem 🛑
            // Garantindo que a peça se ajuste ao tamanho da casa (size)
            Image scaled = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(scaled);
            
            cache.put(key, scaledIcon);
            return scaledIcon;
            
        } catch (Exception e) {
            System.err.println("Erro ao carregar ou dimensionar ícone (" + imgName + "): " + e.getMessage());
            e.printStackTrace(); 
            return null;
        }
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
    
    // Valor da peça para a avaliação Minimax
    public abstract int getValue(); 

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
    public int getValue() { return 10; }
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
    public int getValue() { return 30; }
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
    public int getValue() { return 30; }
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
    public int getValue() { return 50; }
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
    public int getValue() { return 90; }
    public List<Position> getPseudoLegalMoves(Board board, Position from) {
        List<Position> moves = new ArrayList<>();
        // Rainha é a soma dos movimentos da Torre e do Bispo
        moves.addAll(new Rook(isWhite()).getPseudoLegalMoves(board, from));
        moves.addAll(new Bishop(isWhite()).getPseudoLegalMoves(board, from));
        return moves;
    }
}
class King extends Piece {
    public King(boolean isWhite) { super(isWhite, isWhite ? "K" : "k"); }
    public int getValue() { return 900; } // Valor alto para o Rei (não é capturado)
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
    
    // Construtor privado para clonagem
    private Board(boolean empty) {
        board = new Piece[8][8];
        // Se vazio, não faz setup. Usado para inicialização correta na clonagem.
    }

    // Clona o tabuleiro
    @Override
    public Board clone() {
        Board newBoard = new Board(true); // Cria um tabuleiro vazio
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
        // CORREÇÃO: Tipar a lista como List<Move>
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

                        // Se o Rei da cor atual NÃO estiver sob ataque no tabuleiro temporário, o movimento é legal
                        if (!tempGame.inCheck(whiteToMove)) {
                             legalMoves.add(new Move(from, to));
                        }
                    }
                }
            }
        }
        return legalMoves;
    }

    // Movimenta a peça, aplica promoção se necessário
    public void move(Position from, Position to, Character promo) {
        Piece piece = board.get(from);
        if (piece == null) return;

        // Se for promoção, a peça movida é substituída
        if (piece instanceof Pawn && isPromotion(from, to) && promo != null) {
            boolean isWhite = piece.isWhite();
            board.set(to, createPromotedPiece(isWhite, promo));
        } else {
            board.set(to, piece);
        }
        board.set(from, null);

        // Adiciona ao histórico (notação simplificada)
        String moveStr = from.toString() + to.toString();
        history.add(moveStr);

        // Alterna o turno
        whiteToMove = !whiteToMove;

        // Verifica se o jogo acabou
        if (getLegalMoves().isEmpty()) {
            isGameOver = true;
        }
    }
    
    // Auxiliar para criar a peça promovida
    private Piece createPromotedPiece(boolean isWhite, char promo) {
        return switch (Character.toUpperCase(promo)) {
            case 'R' -> new Rook(isWhite);
            case 'B' -> new Bishop(isWhite);
            case 'N' -> new Knight(isWhite);
            default -> new Queen(isWhite); // Padrão é Rainha
        };
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public boolean whiteToMove() {
        return whiteToMove;
    }

    // VERIFICAÇÃO DE XEQUE
    public boolean inCheck(boolean isWhite) {
        Position kingPos = findKing(isWhite);
        if (kingPos == null) return false;

        return isUnderAttack(kingPos, !isWhite);
    }
    
    // Verifica se uma posição está sob ataque de uma determinada cor (atacanteIsWhite)
    private boolean isUnderAttack(Position pos, boolean attackerIsWhite) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position from = new Position(r, c);
                Piece piece = board.get(from);
                if (piece != null && piece.isWhite() == attackerIsWhite) {
                    // Cuidado: Gerar movimentos pseudolegais da peça atacante e verificar se 'pos' está entre eles
                    // Isso é o suficiente para a maioria das peças, mas o Rei atacante deve ser tratado com cuidado
                    if (piece.getPseudoLegalMoves(board, from).contains(pos)) {
                        // Exceção do Peão: para ataques, só olhamos as capturas
                        if (piece instanceof Pawn) {
                             int dir = piece.isWhite() ? -1 : 1;
                             int[] captureCols = {-1, 1};
                             for (int dc : captureCols) {
                                 Position capturePos = new Position(from.getRow() + dir, from.getColumn() + dc);
                                 if (capturePos.equals(pos)) return true;
                             }
                             // Se o peão não ataca a posição, continue (para evitar falsos positivos)
                             continue;
                        }
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
        return null; 
    }

    public Board board() {
        return board;
    }

    public List<String> history() {
        return history;
    }

    // Verifica se um movimento é uma captura (para avaliação da IA)
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

// --- CLASSE PRINCIPAL ---

public class ChessGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    
    
    // --- Valores de Posição (Peça/Posição) para IA (Exemplo Simples) ---
    private static final int[][] PAWN_POS_TABLE = {
        {0, 0, 0, 0, 0, 0, 0, 0},
        {50, 50, 50, 50, 50, 50, 50, 50},
        {10, 10, 20, 30, 30, 20, 10, 10},
        {5, 5, 10, 25, 25, 10, 5, 5},
        {0, 0, 0, 20, 20, 0, 0, 0},
        {5, -5, -10, 0, 0, -10, -5, 5},
        {5, 10, 10, -20, -20, 10, 10, 5},
        {0, 0, 0, 0, 0, 0, 0, 0}
    };
    private static final int[][] KNIGHT_POS_TABLE = {
        {-50, -40, -30, -30, -30, -30, -40, -50},
        {-40, -20, 0, 5, 5, 0, -20, -40},
        {-30, 5, 10, 15, 15, 10, 5, -30},
        {-30, 0, 15, 20, 20, 15, 0, -30},
        {-30, 5, 15, 20, 20, 15, 5, -30},
        {-30, 0, 10, 15, 15, 10, 0, -30},
        {-40, -20, 0, 0, 0, 0, -20, -40},
        {-50, -40, -30, -30, -30, -30, -40, -50}
    };
    // CORREÇÃO: Adicionando as tabelas de posição que faltavam (apenas exemplos simples)
    private static final int[][] BISHOP_POS_TABLE = {
        {-20, -10, -10, -10, -10, -10, -10, -20},
        {-10, 0, 0, 0, 0, 0, 0, -10},
        {-10, 0, 5, 10, 10, 5, 0, -10},
        {-10, 5, 5, 10, 10, 5, 5, -10},
        {-10, 0, 10, 10, 10, 10, 0, -10},
        {-10, 10, 10, 10, 10, 10, 10, -10},
        {-10, 5, 0, 0, 0, 0, 5, -10},
        {-20, -10, -10, -10, -10, -10, -10, -20}
    };
    private static final int[][] ROOK_POS_TABLE = {
        {0, 0, 0, 0, 0, 0, 0, 0},
        {5, 10, 10, 10, 10, 10, 10, 5},
        {-5, 0, 0, 0, 0, 0, 0, -5},
        {-5, 0, 0, 0, 0, 0, 0, -5},
        {-5, 0, 0, 0, 0, 0, 0, -5},
        {-5, 0, 0, 0, 0, 0, 0, -5},
        {-5, 0, 0, 0, 0, 0, 0, -5},
        {0, 0, 0, 5, 5, 0, 0, 0}
    };
    private static final int[][] QUEEN_POS_TABLE = {
        {-20, -10, -10, -5, -5, -10, -10, -20},
        {-10, 0, 0, 0, 0, 0, 0, -10},
        {-10, 0, 5, 5, 5, 5, 0, -10},
        {-5, 0, 5, 5, 5, 5, 0, -5},
        {0, 0, 5, 5, 5, 5, 0, -5},
        {-10, 5, 5, 5, 5, 5, 0, -10},
        {-10, 0, 5, 0, 0, 0, 0, -10},
        {-20, -10, -10, -5, -5, -10, -10, -20}
    };
    private static final int[][] KING_MID_POS_TABLE = { // Meio Jogo
        {-30, -40, -40, -50, -50, -40, -40, -30},
        {-30, -40, -40, -50, -50, -40, -40, -30},
        {-30, -40, -40, -50, -50, -40, -40, -30},
        {-30, -40, -40, -50, -50, -40, -40, -30},
        {-20, -30, -30, -40, -40, -30, -30, -20},
        {-10, -20, -20, -20, -20, -20, -20, -10},
        {20, 20, 0, 0, 0, 0, 20, 20},
        {20, 30, 10, 0, 0, 10, 30, 20}
    };
    private static final int[][] KING_END_POS_TABLE = { // Final de Jogo
        {-50, -30, -30, -30, -30, -30, -30, -50},
        {-30, -10, -10, -10, -10, -10, -10, -30},
        {-30, -10, 20, 30, 30, 20, -10, -30},
        {-30, -10, 30, 40, 40, 30, -10, -30},
        {-30, -10, 30, 40, 40, 30, -10, -30},
        {-30, -10, 20, 30, 30, 20, -10, -30},
        {-30, -30, 0, 0, 0, 0, -30, -30},
        {-50, -30, -30, -30, -30, -30, -30, -50}
    };
    // ... incluir outras tabelas de posição (Bishop, Rook, Queen, King)

    // --- Config de cores/styles ---
    // AZUL GELO (CASA CLARA)
    private static final Color LIGHT_SQ = new Color(200, 220, 240); 
    // CINZA ESCURO (CASA ESCURA)
    private static final Color DARK_SQ = new Color(70, 80, 90);

    // Cores de DESTAQUE ajustadas para o fundo escuro/claro:
    private static final Color HILITE_SELECTED = new Color(0, 150, 255); 
    private static final Color HILITE_LEGAL = new Color(100, 255, 100);
    private static final Color HILITE_LASTMOVE = new Color(50, 100, 150); 
    private static final Color HILITE_CHECK_LEGAL = new Color(255, 165, 0); 

    // Definições de Borda:
    private static final Border BORDER_SELECTED = new MatteBorder(2, 2, 2, 2, HILITE_SELECTED);
    private static final Border BORDER_LEGAL = new MatteBorder(4, 4, 4, 4, HILITE_LEGAL); 
    private static final Border BORDER_LASTMOVE = new MatteBorder(2, 2, 2, 2, HILITE_LASTMOVE);

    private final Game game;

    private final JPanel boardPanel;
    private final JButton[][] squares = new JButton[8][8];

    private final JLabel status;
    private final JTextArea historyTextArea; // Renomeado para evitar conflito
    @SuppressWarnings("unused")
    private final JScrollPane historyScroll;

    private final JLabel scoreLabel;

    // Menu / controles
    private JCheckBoxMenuItem aiPlaysBlack; 
    private JComboBox<String> aiLevelCombo; 
    private JMenuItem newGameItem, quitItem;

    // Mapeamento Nível -> Profundidade Minimax 
    private static final Map<String, Integer> LEVEL_TO_DEPTH = Map.of(
        "Fácil (Prof. 1)", 1,
        "Médio (Prof. 2)", 2,
        "Difícil (Prof. 3)", 3,
        "Mestre (Prof. 4)", 4
    );
    private int currentAIDepth = 3; 

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
        historyTextArea = new JTextArea(14, 22);
        historyTextArea.setEditable(false);
        historyTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        historyScroll = new JScrollPane(historyTextArea); // JScrollPane usa o JTextArea

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

        // NOVO: Painel para o rótulo do histórico e da pontuação
        JPanel topRightPanel = new JPanel(new BorderLayout());
        topRightPanel.setOpaque(false);
        topRightPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));

        JLabel histLabel = new JLabel("Histórico de Movimentos:");
        histLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        histLabel.setForeground(new Color(30, 60, 120));

        // NOVO: Rótulo para a pontuação
        scoreLabel = new JLabel("Avaliação: 0");
        scoreLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        scoreLabel.setForeground(new Color(30, 60, 120));

        topRightPanel.add(histLabel, BorderLayout.NORTH);
        topRightPanel.add(scoreLabel, BorderLayout.SOUTH);

        rightPanel.add(topRightPanel, BorderLayout.NORTH);


        // painel do histórico com borda arredondada
        JScrollPane historyScrollPane = new JScrollPane(historyTextArea); // Usa o JTextArea
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

        newGameItem = new JMenuItem("Novo Jogo");
        newGameItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        newGameItem.addActionListener(e -> doNewGame());

        aiPlaysBlack = new JCheckBoxMenuItem("Adversário joga com as Pretas (IA)"); 
        aiPlaysBlack.setSelected(true); 
        aiPlaysBlack.addActionListener(e -> doNewGame()); // Inicia novo jogo ao mudar lado

        JMenu depthMenu = new JMenu("Nível da IA");
        
        String[] levels = LEVEL_TO_DEPTH.keySet().toArray(new String[0]);
        aiLevelCombo = new JComboBox<>(levels);
        aiLevelCombo.setSelectedItem("Difícil (Prof. 3)"); 
        aiLevelCombo.addActionListener(e -> {
            String selected = (String) aiLevelCombo.getSelectedItem();
            currentAIDepth = LEVEL_TO_DEPTH.getOrDefault(selected, 3);
            doNewGame(); 
        });
        depthMenu.add(aiLevelCombo);

        quitItem = new JMenuItem("Sair");
        quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        quitItem.addActionListener(e -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));

        gameMenu.add(newGameItem);
        gameMenu.addSeparator();
        gameMenu.add(aiPlaysBlack);
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
        cb.setSelected(aiPlaysBlack.isSelected());
        cb.addActionListener(e -> {
            aiPlaysBlack.setSelected(cb.isSelected());
            doNewGame();
        });
        panel.add(cb);

        panel.add(new JLabel("Nível IA:"));
        
        JComboBox<String> sideLevelCombo = new JComboBox<>(LEVEL_TO_DEPTH.keySet().toArray(new String[0]));
        sideLevelCombo.setSelectedItem(aiLevelCombo.getSelectedItem());
        sideLevelCombo.addActionListener(e -> {
            aiLevelCombo.setSelectedItem(sideLevelCombo.getSelectedItem());
            String selected = (String) sideLevelCombo.getSelectedItem();
            currentAIDepth = LEVEL_TO_DEPTH.getOrDefault(selected, 3);
            doNewGame();
        });
        panel.add(sideLevelCombo);

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
        if (aiPlaysBlack.isSelected() && !game.whiteToMove()) return; // Player só joga com as Brancas

        Piece p = game.board().get(clicked);

        if (selected == null) {
            if (p != null && p.isWhite() == game.whiteToMove()) {
                selected = clicked;
                legalForSelected = new ArrayList<>();
                // Encontra movimentos legais para a peça selecionada
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
                // Nova seleção de peça (se for da mesma cor)
                selected = clicked;
                legalForSelected = new ArrayList<>();
                // Encontra movimentos legais para a nova peça selecionada
                for(Move m : game.getLegalMoves()) {
                    if (m.from.equals(selected)) {
                        legalForSelected.add(m.to);
                    }
                }
            } else {
                // Clicou em uma casa inválida (ou na peça do oponente/casa vazia), deseleciona
                selected = null;
                legalForSelected.clear();
            }
        }
        refresh();
    }
    
    // ----------------- Utilitários da GUI -----------------
    
    private Character askPromotion() {
        // Opções de promoção
        String[] options = {"Rainha", "Torre", "Bispo", "Cavalo"};
        int choice = JOptionPane.showOptionDialog(this, 
            "Promover o peão para qual peça?", "Promoção de Peão",
            JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
            options, options[0]);

        return switch (choice) {
            case 0 -> 'Q'; // Queen (Rainha)
            case 1 -> 'R'; // Rook (Torre)
            case 2 -> 'B'; // Bishop (Bispo)
            case 3 -> 'N'; // Knight (Cavalo)
            default -> 'Q'; // Padrão
        };
    }
    
    /**
     * 
     */
    private void refresh() {
    // 🛑 Adicione esta verificação para evitar erros de divisão por zero ou redimensionamento
    if (boardPanel.getWidth() == 0 || boardPanel.getHeight() == 0) return;
    
    // 1. Cálculo Base do Tamanho (lado de uma casa)
    int size = Math.min(boardPanel.getWidth(), boardPanel.getHeight()) / 8;

    // 🛑 CORREÇÃO 1: Reduza um pouco o tamanho (ex: 5 pixels)
    // para garantir que a peça caiba dentro de qualquer borda aplicada
    int pieceSize = size - 5; 
    if (pieceSize < 1) pieceSize = 1; // Evita tamanhos negativos/zero
    
    // Altura da borda (para ajuste fino)
    @SuppressWarnings("unused")
    int borderThickness = 4; // Use o valor máximo da sua borda (e.g., BORDER_LEGAL)

    for (int r = 0; r < 8; r++) {
        for (int c = 0; c < 8; c++) {
            JButton b = squares[r][c];
            Position pos = new Position(r, c);
            Piece p = game.board().get(pos);

            // 🛑 CORREÇÃO 2: Garante que o botão não tenha texto (para centralizar o ícone) 🛑
            b.setText(null); 
            b.setHorizontalAlignment(SwingConstants.CENTER);
            b.setVerticalAlignment(SwingConstants.CENTER);
            
            // ... (Restante da lógica de cores e bordas - OK)
            // A lógica de bordas e cores está OK, mas vamos simplificar a aplicação da borda nula.

            boolean isLight = (r + c) % 2 == 0;
            Color baseColor = isLight ? LIGHT_SQ : DARK_SQ;
            Color bgColor = baseColor;
            Border border = BorderFactory.createEmptyBorder(); // Começa com borda vazia
            
            // ... (Lógica de Destaque de Último Lance, Xeque, Seleção e Movimento Legal)
            // (Esta seção fica como você a escreveu, pois está correta)
            
            // 1. Destaque de Último Lance
            if (pos.equals(lastFrom) || pos.equals(lastTo)) {
                border = BORDER_LASTMOVE;
            }
            // 2. Destaque de Xeque... (seu código continua)
            if (p instanceof King && game.inCheck(p.isWhite())) {
                bgColor = Color.RED.darker();
                if (legalForSelected.contains(pos)) {
                    bgColor = HILITE_CHECK_LEGAL; 
                }
            }
            // 3. Destaque de Seleção
            if (pos.equals(selected)) {
                border = BORDER_SELECTED;
            }
            // 4. Destaque de Movimento Legal
            if (legalForSelected.contains(pos) && !pos.equals(selected)) {
                if (game.board().get(pos) != null) {
                    bgColor = Color.ORANGE.darker();
                } else {
                    border = BORDER_LEGAL;
                }
            }


            // Aplica cores e bordas
            b.setBackground(bgColor);
            // 🛑 CORREÇÃO 3: Ajuste fino para a borda 🛑
            // Aplique o BorderFactory.createEmptyBorder() apenas se nenhuma borda de destaque foi aplicada
            if (border == null || border instanceof EmptyBorder) {
                b.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0)); 
            } else {
                b.setBorder(border);
            }


            // Aplica Ícone
            if (p != null) {
                // Use pieceSize (tamanho ajustado)
                ImageIcon icon = ImageUtil.getPieceIcon(p.isWhite(), p.getSymbol().charAt(0), pieceSize);
                b.setIcon(icon);
            } else {
                b.setIcon(null);
            }
        }
    }

        
        // Atualiza Status
        String turn = game.whiteToMove() ? "Brancas" : "Pretas";
        String statusText = "Sua Vez: " + turn;
        if (aiPlaysBlack.isSelected() && game.whiteToMove() == false) {
             statusText = "Turno da IA: Pretas";
        }
        if (game.isGameOver()) {
            statusText = "FIM DE JOGO!";
        } else if (game.inCheck(game.whiteToMove())) {
            statusText = "Xeque! Vez das " + turn;
        }
        status.setText(statusText);
        
        // Atualiza Histórico e Pontuação
        updateHistoryAndScore();
        
        boardPanel.revalidate();
        boardPanel.repaint();
    }
    
    private void updateHistoryAndScore() {
        StringBuilder sb = new StringBuilder();
        List<String> hist = game.history();
        for (int i = 0; i < hist.size(); i++) {
            if (i % 2 == 0) {
                sb.append((i / 2) + 1).append(". ");
            }
            sb.append(hist.get(i)).append(" ");
            if (i % 2 != 0) {
                sb.append("\n");
            }
        }
        historyTextArea.setText(sb.toString());
        // Rola para o fim
        historyTextArea.setCaretPosition(historyTextArea.getDocument().getLength());

        // Avaliação (mostra a avaliação do tabuleiro atual)
        int score = evaluateBoard(game.board(), game.whiteToMove());
        String scoreStr = (score > 0) ? "+" + (score / 10.0) : "" + (score / 10.0);
        scoreLabel.setText("Avaliação: " + scoreStr);
    }
    
    private void maybeAnnounceEnd() {
         if (game.isGameOver()) {
            String message = game.inCheck(!game.whiteToMove()) ? 
                "Xeque-Mate! " + (game.whiteToMove() ? "Pretas" : "Brancas") + " Venceram!" :
                "Empate por Afogamento.";
            JOptionPane.showMessageDialog(this, message, "FIM DE JOGO", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ----------------- Lógica da IA (Minimax) -----------------
    
    private void maybeTriggerAI() {
        // A IA só joga com as pretas e se for o turno dela
        if (aiPlaysBlack.isSelected() && !game.whiteToMove() && !game.isGameOver()) {
            aiThinking = true;
            status.setText("IA pensando...");

            // Executa a IA em uma thread separada para não travar a GUI
            new SwingWorker<Move, Void>() {
                @Override
                protected Move doInBackground() {
                    return getAIMove(currentAIDepth);
                }

                @Override
                protected void done() {
                    try {
                        Move bestMove = get();
                        if (bestMove != null) {
                            lastFrom = bestMove.from;
                            lastTo = bestMove.to;
                            // A IA não promove um peão, a não ser que seja para Rainha (simplificação)
                            Character promo = game.isPromotion(bestMove.from, bestMove.to) ? 'Q' : null;
                            game.move(bestMove.from, bestMove.to, promo);
                        }
                    } catch (Exception e) {
                        System.err.println("Erro na execução da IA: " + e.getMessage());
                    } finally {
                        aiThinking = false;
                        refresh();
                        maybeAnnounceEnd();
                    }
                }
            }.execute();
        }
    }
    
    private void loadOpeningBook() {
        // Exemplo de livro de aberturas (apenas para teste/exemplo)
        openingBook.put("e2e4", new Move(new Position(6, 4), new Position(4, 4)));
        openingBook.put("e2e4 e7e5", new Move(new Position(1, 6), new Position(3, 6))); // Nf3
        openingBook.put("e2e4 c7c5", new Move(new Position(6, 3), new Position(4, 3))); // d4
    }

    private Move getAIMove(int depth) {
        // Tenta movimento do livro de aberturas
        String currentHistory = String.join(" ", game.history());
        if (openingBook.containsKey(currentHistory)) {
            return openingBook.get(currentHistory);
        }

        // Se não houver livro, calcula o melhor lance
        // CORREÇÃO: Tipar a lista como List<Move>
        List<Move> allMoves = game.getLegalMoves();
        if (allMoves.isEmpty()) return null;

        // Otimização: priorizar capturas para o Alpha-Beta Pruning
        // CORREÇÃO DE ERRO: A lista 'allMoves' é do tipo List<Move>
        allMoves.sort(Comparator.comparingInt((Move m) -> game.isCaptura(m.to) ? 1 : 0).reversed());

        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;

        for (Move m : allMoves) {
            Game nextGame = game.clone();
            // A IA só move a peça, não pede promoção (simplificação, assume Queen)
            Character promo = nextGame.isPromotion(m.from, m.to) ? 'Q' : null;
            nextGame.move(m.from, m.to, promo);

            // Chamada Minimax
            int score = minimax(nextGame, depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, nextGame.whiteToMove());
            
            // Inverte o score para a perspectiva da cor que está movendo (preta)
            // Se for vez das Pretas (IA), minimizamos (score deve ser menor que o melhor score)
            // if (!game.whiteToMove()) score = -score; // Não precisa inverter se o Minimax é chamado corretamente

            if (score > bestScore) {
                bestScore = score;
                bestMove = m;
            }
        }
        return bestMove;
    }

    // Minimax com Poda Alpha-Beta
    private int minimax(Game current, int depth, int alpha, int beta, boolean maximizingPlayer) {
        if (depth == 0 || current.isGameOver()) {
            return evaluateBoard(current.board(), maximizingPlayer); // Retorna a avaliação do tabuleiro (a cor que está jogando)
        }

        // CORREÇÃO: Tipar a lista como List<Move>
        List<Move> moves = current.getLegalMoves();
        if (moves.isEmpty()) {
            return evaluateBoard(current.board(), maximizingPlayer);
        }

        // Otimização: priorizar capturas
        // CORREÇÃO DE ERRO: A lista 'moves' é do tipo List<Move>
        moves.sort(Comparator.comparingInt((Move m) -> current.isCaptura(m.to) ? 1 : 0).reversed());

        if (maximizingPlayer) { // MAX (para a IA que quer o maior score)
            int maxEval = Integer.MIN_VALUE;
            for (Move m : moves) {
                Game nextGame = current.clone();
                Character promo = nextGame.isPromotion(m.from, m.to) ? 'Q' : null;
                nextGame.move(m.from, m.to, promo);
                
                int eval = minimax(nextGame, depth - 1, alpha, beta, !maximizingPlayer);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) {
                    break; // Poda Beta
                }
            }
            return maxEval;
        } else { // MIN (para o humano que quer o menor score para a IA)
            int minEval = Integer.MAX_VALUE;
            for (Move m : moves) {
                Game nextGame = current.clone();
                Character promo = nextGame.isPromotion(m.from, m.to) ? 'Q' : null;
                nextGame.move(m.from, m.to, promo);
                
                int eval = minimax(nextGame, depth - 1, alpha, beta, !maximizingPlayer);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) {
                    break; // Poda Alpha
                }
            }
            return minEval;
        }
    }
    
    // Função de Avaliação Simples (Material + Posição)
    private int evaluateBoard(Board board, boolean whiteIsMaximizer) {
        int totalScore = 0;
        
        // Ponto final de jogo simples (baseado em peças restantes)
        int materialCount = 0;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board.get(new Position(r, c));
                if (p != null) materialCount += p.getValue();
            }
        }
        boolean isEndGame = materialCount < 300; // Limite simples
        
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position pos = new Position(r, c);
                Piece p = board.get(pos);
                if (p != null) {
                    int pieceValue = p.getValue();
                    int posValue = getPositionalValue(p, r, c, isEndGame);
                    
                    int pieceScore = pieceValue + posValue;
                    
                    if (p.isWhite()) {
                        totalScore += pieceScore;
                    } else {
                        totalScore -= pieceScore;
                    }
                }
            }
        }
        
        // O score final é do ponto de vista das Brancas (positivas = boas para Brancas)
        // Como a IA é Pretas, ela tentará minimizá-lo (torná-lo negativo)
        // O minimax trata a perspectiva da cor que está movendo, então o score é do ponto de vista do maximizador
        return totalScore; 
    }
    
    // Retorna o valor posicional da peça
    @SuppressWarnings("unused")
    private int getPositionalValue(Piece piece, int r, int c, boolean isEndGame) {
        int row = piece.isWhite() ? r : 7 - r; // Inverte para peças pretas
        int col = c;
        
        int[][] table;
        if (piece instanceof Pawn) table = PAWN_POS_TABLE;
        else if (piece instanceof Knight) table = KNIGHT_POS_TABLE;
        else if (piece instanceof Bishop) table = BISHOP_POS_TABLE;
        else if (piece instanceof Rook) table = ROOK_POS_TABLE;
        else if (piece instanceof Queen) table = QUEEN_POS_TABLE;
        else if (piece instanceof King) table = isEndGame ? KING_END_POS_TABLE : KING_MID_POS_TABLE;
        else return 0; // Outras peças (se houver)

        // Se a peça for preta, inverte o índice da linha na tabela, mantendo o valor da coluna
        if (!piece.isWhite()) {
            return table[7 - r][c];
        } else {
            return table[r][c];
        }
    }


    // ----------------- MAIN -----------------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChessGUI::new);
    }
}
