
// MyAgent vs RandomAgent		10 vs 0
// MyAgent vs BeginnerAgent		 9 vs 1
// MyAgent vs IntermediateAgent		10 vs 0
// MyAgent vs AdvancedAgent		 9 vs 1
// MyAgent vs BrilliantAgent		 8 vs 2
// MyAgent vs MyAgent			 5 vs 5
// MyAgent vs MyRecursiveAgent		 7 vs 3

import java.util.Random;

public class MyAgent extends Agent {
    Random r;

    /**
     * Constructs a new agent, giving it the game and telling it whether it is Red or Yellow.
     *
     * @param game The game the agent will be playing.
     * @param iAmRed True if the agent is Red, False if the agent is Yellow.
     */
    public MyAgent(Connect4Game game, boolean iAmRed) {
        super(game, iAmRed);
        r = new Random();
    }

    /**
     * The move method is run every time it is this agent's turn in the game. You may assume that
     * when move() is called, the game has at least one open slot for a token, and the game has not
     * already been won.
     *
     * By the end of the move method, the agent should have placed one token into the game at some
     * point.
     *
     * After the move() method is called, the game engine will check to make sure the move was
     * valid. A move might be invalid if:
     * - No token was place into the game.
     * - More than one token was placed into the game.
     * - A previous token was removed from the game.
     * - The color of a previous token was changed.
     * - There are empty spaces below where the token was placed.
     *
     * If an invalid move is made, the game engine will announce it and the game will be ended.
     *
     */
    public void move() {
        int columnNumber;

        // iCanWin iWillLose prepareToWin preventToLose cluster random  wins / rounds
        //    x        x          -             -          -       x      14 / 20
        //    x        x          x             -          -       x      16 / 20
        //    x        x          -             x          -       x      17 / 20
        //    x        x          x             x          -       x      13 / 20
        //    x        x          -             -          x       x      14 / 20
        //    x        x          x             -          x       x      15 / 20
        //    x        x          -             x          x       x      16 / 20
        //    x        x          x             x          x       x      14 / 20

        // other improvements:
        //    i do not check if my new move will enable the opponent to win
        //    it would really help a lot when i would check that
        //    f.i. fill a structure columnsToAvoid
        
        // check if i can win
        if ((columnNumber = iCanWin()) == -1) {
            // i can not win, check if opponent is close to winning
            if ((columnNumber = iWillLose()) == -1) {
                // they can not win either, lets try to interfere my opponent a bit
                if ((columnNumber = preventToLose()) == -1) {
                    // all that is left is doing some random move
                    columnNumber = randomMove();
                }
            }
        }
        moveOnColumn(columnNumber);
    }

    /**
     * Drops a token into a particular column so that it will fall to the bottom of the column.
     * If the column is already full, nothing will change.
     *
     * @param columnNumber The column into which to drop the token.
     */
    public void moveOnColumn(int columnNumber) {
        int lowestEmptySlotIndex = getLowestEmptyIndex(myGame.getColumn(columnNumber)); // Find the top empty slot in
                                                                                        // the column
                                                                                        // If the column is full,
                                                                                        // lowestEmptySlot will be -1
        if (lowestEmptySlotIndex > -1) // if the column is not full
        {
            Connect4Slot lowestEmptySlot = myGame.getColumn(columnNumber).getSlot(lowestEmptySlotIndex); // get the slot
                                                                                                         // in this
                                                                                                         // column at
                                                                                                         // this index
            if (iAmRed) // If the current agent is the Red player...
            {
                lowestEmptySlot.addRed(); // Place a red token into the empty slot
            } else // If the current agent is the Yellow player (not the Red player)...
            {
                lowestEmptySlot.addYellow(); // Place a yellow token into the empty slot
            }
        }
    }

    /**
     * Returns the index of the top empty slot in a particular column.
     *
     * @param column The column to check.
     * @return the index of the top empty slot in a particular column; -1 if the column is already full.
     */
    public int getLowestEmptyIndex(Connect4Column column) {
        int lowestEmptySlot = -1;
        for (int i = 0; i < column.getRowCount(); i++) {
            if (!column.getSlot(i).getIsFilled()) {
                lowestEmptySlot = i;
            }
        }
        return lowestEmptySlot;
    }

    /**
     * Returns a random valid move. If your agent doesn't know what to do, making a random move
     * can allow the game to go on anyway.
     * Prefer center for the first attempt...
     *
     * @return a random valid move.
     */
    public int randomMove() {
        int column = r.nextInt(myGame.getColumnCount() / 2) + myGame.getColumnCount() / 2 - 1;
        while (getLowestEmptyIndex(myGame.getColumn(column)) == -1) {
            column = r.nextInt(myGame.getColumnCount());
        }

        return column;
    }

    /**
     * Returns the column with the most neighbors
     *
     * @return the column with the most neighbors
     */
    public int clusterMove(char color) {
        char[][] board = myGame.getBoardMatrix();

        int bestColumn = -1;
        int max = -1;

        for (int column = 0; column < myGame.getColumnCount(); column++) {
            int row = getLowestEmptyIndex(myGame.getColumn(column));
            if (row != -1) {
                int count = 0;
                for (int columnInc = -1; columnInc <= 1; ++columnInc) {
                    for (int rowInc = -1; rowInc <= 1; ++rowInc) {
                        int rowNow = row + rowInc;
                        int columnNow = column + columnInc;
                        if (rowNow >= 0 && rowNow < myGame.getRowCount() && columnNow >= 0 && columnNow < myGame.getColumnCount()
                                && board[row + rowInc][column + columnInc] == color) {
                            ++count;
                        }
                    }
                }
                // only cluster when 2..5 same colors around
                if (count >= 2 && count <= 5 && count > max) {
                    bestColumn = column;
                    max = count;
                }
            }
        }

        return bestColumn;
    }

    public int cluster() {
        return clusterMove(iAmRed ? 'R' : 'Y');
    }

    public int decluster() {
        return clusterMove(iAmRed ? 'Y' : 'R');
    }

    /**
     * Returns the column that would allow a player to win.
     *
     * @return the column that would allow a player to win.
     */
    public int check4Slots(char[][] board, int row, int column, int rowInc, int columnInc, char color, int maxBlankPositions) {
        int emptyColumn = -1;
        for (int i = 0; i < 4; ++i) {
            int rowNow = row + i * rowInc;
            int columnNow = column + i * columnInc;
            if (rowNow < 0 || rowNow >= myGame.getRowCount() || columnNow < 0 || columnNow >= myGame.getColumnCount()) {
                return -1;
            }
            char colorNow = board[rowNow][columnNow];
            if (colorNow == 'B') {
                // is it a reachable blank?
                if (getLowestEmptyIndex(myGame.getColumn(columnNow)) != rowNow) {
                    return -1;
                }
                // max nr of blanks reached?
                if (maxBlankPositions <= 0) {
                    return -1;
                }
                --maxBlankPositions;
                // this might be a good candidate!
                emptyColumn = columnNow;
            } else if (colorNow != color) {
                // opponent color in this row? can't win
                return -1;
            }
        }
        return emptyColumn;
    }

    /**
     * Returns the column that would allow a player to win.
     *
     * @return the column that would allow a player to win.
     */
    public int columnToWin(char color, int maxBlankPositions) {
        char[][] board = myGame.getBoardMatrix();
        int winningColumn = -1;

        for (int column = 0; column < myGame.getColumnCount(); column++) {
            for (int row = 0; row < myGame.getRowCount(); row++) {
                // top to bottom (column = constant)
                if ((winningColumn = check4Slots(board, row, column, 1, 0, color, maxBlankPositions)) != -1) return winningColumn;
                // left to right (row = constant)
                if ((winningColumn = check4Slots(board, row, column, 0, 1, color, maxBlankPositions)) != -1) return winningColumn;
                // upperleft to lowerright
                if ((winningColumn = check4Slots(board, row, column, 1, 1, color, maxBlankPositions)) != -1) return winningColumn;
                // upperright to lowerleft
                if ((winningColumn = check4Slots(board, row, column, 1, -1, color, maxBlankPositions)) != -1) return winningColumn;
            }
        }

        return -1;
    }

    /**
     * Returns the column that would allow the agent to win.
     *
     * You might want your agent to check to see if it has a winning move available to it so that
     * it can go ahead and make that move. Implement this method to return what column would
     * allow the agent to win.
     *
     * @return the column that would allow the agent to win.
     */
    public int iCanWin() {
        return columnToWin(iAmRed ? 'R' : 'Y', 1);
    }

    public int prepareToWin() {
        return columnToWin(iAmRed ? 'R' : 'Y', 2);
    }


    /**
     * Returns the column that would allow the opponent to win.
     *
     * You might want your agent to check to see if the opponent would have any winning moves
     * available so your agent can block them. Implement this method to return what column should
     * be blocked to prevent the opponent from winning.
     *
     * @return the column that would allow the opponent to win.
     */
    public int iWillLose() {
        return columnToWin(iAmRed ? 'Y' : 'R', 1);
    }

    public int preventToLose() {
        return columnToWin(iAmRed ? 'Y' : 'R', 2);
    }

    /**
     * Returns the name of this agent.
     *
     * @return the agent's name
     */
    public String getName() {
        return "My Agent";
    }
}
