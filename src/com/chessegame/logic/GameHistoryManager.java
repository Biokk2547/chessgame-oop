package com.chessegame.logic;

import com.chessegame.model.Board;
import com.chessegame.model.MoveRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * GameHistoryManager implementation using a custom Doubly-LinkedList data structure.
 * Allows stepping back/forward through move history all the way from the beginning of the game.
 */
public class GameHistoryManager {

    /**
     * Doubly-LinkedList Node containing a MoveRecord payload, position key, and pointers to previous and next nodes.
     */
    public static class Node {
        private final MoveRecord move;
        private final String positionKey;
        private Node prev;
        private Node next;

        public Node(MoveRecord move, String positionKey) {
            this.move = move;
            this.positionKey = positionKey;
            this.prev = null;
            this.next = null;
        }

        public Node(MoveRecord move) {
            this(move, null);
        }

        public MoveRecord getMove() { return move; }
        public String getPositionKey() { return positionKey; }
        public Node getPrev() { return prev; }
        public Node getNext() { return next; }
    }

    // Dummy head node representing initial board state at start of game
    private final Node head = new Node(null);
    private Node current = head;
    private String initialPositionKey = "";

    public void setInitialPositionKey(String key) {
        this.initialPositionKey = (key != null) ? key : "";
    }

    public String getInitialPositionKey() {
        return initialPositionKey;
    }

    /**
     * Records a new move at the current position in the Doubly-LinkedList.
     * Truncates any forward redo branch and links the new node.
     */
    public void recordMove(MoveRecord move) {
        recordMove(move, null);
    }

    public void recordMove(MoveRecord move, String positionKey) {
        Node newNode = new Node(move, positionKey);
        current.next = newNode;
        newNode.prev = current;
        current = newNode;
    }

    /**
     * Calculates how many times the specified position has appeared up to the current state.
     */
    public int getRepetitionCount(String positionKey) {
        if (positionKey == null || positionKey.isEmpty()) return 0;
        int count = 0;
        if (positionKey.equals(initialPositionKey)) {
            count++;
        }
        Node temp = head.next;
        while (temp != null) {
            if (positionKey.equals(temp.positionKey)) {
                count++;
            }
            if (temp == current) break;
            temp = temp.next;
        }
        return count;
    }

    /**
     * Checks if the current board position has occurred 3 or more times (FIDE Threefold Repetition).
     */
    public boolean isThreefoldRepetition(String currentPositionKey) {
        return getRepetitionCount(currentPositionKey) >= 3;
    }

    public boolean canUndo() {
        return current != null && current != head;
    }

    public boolean canRedo() {
        return current != null && current.next != null;
    }

    /**
     * Undoes the current move, reverting board state and stepping backwards via prev pointer.
     */
    public MoveRecord undo(Board board) {
        if (!canUndo()) return null;
        MoveRecord move = current.move;
        board.undoMoveRecord(move);
        current = current.prev;
        return move;
    }

    /**
     * Redoes the next move, stepping forward via next pointer and executing move on board.
     */
    public MoveRecord redo(Board board) {
        if (!canRedo()) return null;
        current = current.next;
        MoveRecord move = current.move;
        board.moveRecord(move.getFrom(), move.getTo(), move.getMovedPiece().getColor(), move.getPromotedPiece());
        return move;
    }

    /**
     * Resets move history to start of game.
     */
    public void clear() {
        head.next = null;
        current = head;
    }

    /**
     * Retrieves all moves up to the current active node in the Doubly-LinkedList.
     */
    public List<MoveRecord> getHistory() {
        List<MoveRecord> list = new ArrayList<>();
        Node temp = head.next;
        while (temp != null) {
            list.add(temp.move);
            if (temp == current) break;
            temp = temp.next;
        }
        return list;
    }

    /**
     * Retrieves full game trajectory from head to tail in the Doubly-LinkedList.
     */
    public List<MoveRecord> getFullTrajectory() {
        List<MoveRecord> list = new ArrayList<>();
        Node temp = head.next;
        while (temp != null) {
            list.add(temp.move);
            temp = temp.next;
        }
        return list;
    }

    public List<String> getFormattedMoveHistory() {
        List<String> moves = new ArrayList<>();
        List<MoveRecord> list = getHistory();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i % 2 == 0) {
                if (sb.length() > 0) moves.add(sb.toString());
                sb = new StringBuilder();
                sb.append((i / 2 + 1)).append(". ").append(list.get(i).getNotation());
            } else {
                sb.append("   ").append(list.get(i).getNotation());
            }
        }
        if (sb.length() > 0) moves.add(sb.toString());
        return moves;
    }

    public Node getCurrentNode() {
        return current;
    }

    public Node getHeadNode() {
        return head;
    }

    /**
     * Returns the last move made up to the current state in history (1 move back / last turn),
     * or null if at initial starting position.
     */
    public MoveRecord getLastMove() {
        if (current != null && current != head) {
            return current.getMove();
        }
        return null;
    }
}
