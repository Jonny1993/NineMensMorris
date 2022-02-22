package com.example.ninemensmorris

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import java.io.ObjectInputStream
import java.io.ObjectOutputStream


/* The game board.xml positions
 *
 * 00           01           02
 *     03       04       05
 *         06   07   08
 * 09  10  11        12  13  14
 *         15   16   17
 *     18       19       20
 * 21           22           23
 *
 */
class NMMRules {
    companion object {
        const val BLUE_TURN = 1
        const val RED_TURN = 2
        const val EMPTY_SPACE = 0
        const val BLUE_PIECE = 4
        const val RED_PIECE = 5
    }
    private val SAVE_FILE = "saveGame"
    private lateinit var game : GameSave
    private var board = IntArray(24)
    var pieceHighlighted = false
    var from = -1
    var bluePieces = 3 //3 for testing
    var redPieces = 3
    var turn = RED_TURN
    var pieceJustMoved=false
    var gameOver=false
    var pieceToBeRemoved=false

    fun newGame(){
        board = IntArray(24)
        bluePieces = 9
        redPieces = 9
        turn = RED_TURN
        from = -1
        pieceHighlighted = false
        pieceJustMoved=false
        gameOver=false
        pieceToBeRemoved=false
        game = GameSave(board, pieceHighlighted, from, bluePieces, redPieces, turn, pieceJustMoved, gameOver, pieceToBeRemoved)
    }

    fun saveGame(context: Context): Boolean{
        game = GameSave(board, pieceHighlighted, from, bluePieces, redPieces, turn, pieceJustMoved, gameOver, pieceToBeRemoved)
        val fOutputStream = context.openFileOutput(SAVE_FILE, MODE_PRIVATE)
        val objOutputStream = ObjectOutputStream(fOutputStream)
        objOutputStream.writeObject(game)
        objOutputStream.close()
        fOutputStream.close()
        return true
    }

    fun loadGame(context: Context): Boolean{
        return if(context.fileList().contains(SAVE_FILE)) {
            val fInputSteam = context.openFileInput(SAVE_FILE)
            val objInputStream = ObjectInputStream(fInputSteam)
            game = objInputStream.readObject() as GameSave
            Log.d("LoadedGame", "$game")
            if (!game.board.all { it == 0 }) {
                board = game.board
                bluePieces = game.bluePieces
                redPieces = game.redPieces
                turn = game.turn
                from = game.from
                pieceHighlighted = game.pieceHighlighted
                pieceJustMoved = game.pieceJustMoved
                gameOver = game.gameOver
                pieceToBeRemoved = game.pieceToBeRemoved
                objInputStream.close()
                fInputSteam.close()
            }
            true
        }
        else false
    }

    /**
     * Returns true if a move is successful
     */

    fun legalMove(to: Int,  color: Int): Boolean {
        pieceJustMoved=false
        if(gameOver){
            Log.d("Clicked","Game already Over!")
            return false
        }
        return if (color == turn) {
            Log.d("Clicked", "turn is $turn")
            if (turn == RED_TURN) {
                if (redPieces > 0 && board[to] == EMPTY_SPACE) {
                    markSpot(to, RED_PIECE)
                }
                else{
                    return if(redPieces <= 0) movePiece(to, RED_PIECE) else false
                }
            } else {
                if (bluePieces > 0 && board[to] == EMPTY_SPACE) {
                    markSpot(to, BLUE_PIECE)
                }
                else{
                    return if(bluePieces <= 0) movePiece(to, BLUE_PIECE) else false
                }
            }
        } else {
            false
        }
    }

    private fun markSpot(spot: Int, piece: Int): Boolean{
        board[spot] = piece
        if(canRemove(spot)){
            pieceToBeRemoved=true
            Log.d("Clicked","player may take piece")
            if(piece== RED_PIECE) redPieces-- else bluePieces--
            return true
        }
        turn = if(piece == RED_PIECE){
            redPieces--
            BLUE_TURN
        } else{
            bluePieces--
            RED_TURN
        }
        return true
    }

    private fun movePiece(to: Int, piece: Int): Boolean {
        return if (board[to] == EMPTY_SPACE && pieceHighlighted) {
            Log.d("Clicked", "Empty space clicked while $turn has a tile that is highlighted ")
            val valid = isValidMove(to, from)
            if (valid) {
                board[to] = piece
                //Added by us
                board[from] = EMPTY_SPACE
                pieceHighlighted = false
                pieceJustMoved=true
                if(canRemove(to)){
                    pieceToBeRemoved=true
                    Log.d("Clicked","player may take piece")
                }
                else   turn = if (piece == RED_PIECE) BLUE_TURN else RED_TURN
                //end
                true
            } else {
                //Added by us
                Log.d("Clicked", "invalid")
                from = -1
                pieceHighlighted = false
                //end
                false
            }
        }else if(from > -1 && board[to] == board[from] && pieceHighlighted){ //deselect player piece
            from = -1
            pieceHighlighted = false
            true
        }else {
            //Added by us
            if(!pieceHighlighted && board[to] == piece) {
                from = to
                pieceHighlighted=true
                Log.d("Clicked","pieceHighlighted")
                true
            }
            //end
            else{
                pieceHighlighted = false
                from = -1
                false
            }
        }
    }

    /**
     * Returns true if position "to" is part of three in a row.
     */
    private fun canRemove(to: Int): Boolean {
        if ((to == 0 || to == 1 || to == 2) && board[0] == board[1] && board[1] == board[2]) {
            return true
        } else if ((to == 3 || to == 4 || to == 5)
                && board[3] == board[4] && board[4] == board[5]) {
            return true
        } else if ((to == 6 || to == 7 || to == 8)
                && board[6] == board[7] && board[7] == board[8]) {
            return true
        } else if ((to == 9 || to == 10 || to == 11)
                && board[9] == board[10] && board[10] == board[11]) {
            return true
        } else if ((to == 12 || to == 13 || to == 14)
                && board[12] == board[13] && board[13] == board[14]) {
            return true
        } else if ((to == 15 || to == 16 || to == 17)
                && board[15] == board[16] && board[16] == board[17]) {
            return true
        } else if ((to == 18 || to == 19 || to == 20)
                && board[18] == board[19] && board[19] == board[20]) {
            return true
        } else if ((to == 21 || to == 22 || to == 23)
                && board[21] == board[22] && board[22] == board[23]) {
            return true
        } else if ((to == 0 || to == 9 || to == 21)
                && board[0] == board[9] && board[9] == board[21]) {
            return true
        } else if ((to == 3 || to == 10 || to == 18)
                && board[3] == board[10] && board[10] == board[18]) {
            return true
        } else if ((to == 6 || to == 11 || to == 15)
                && board[6] == board[11] && board[11] == board[15]) {
            return true
        } else if ((to == 1 || to == 4 || to == 7)
                && board[1] == board[4] && board[4] == board[7]) {
            return true
        } else if ((to == 8 || to == 12 || to == 17)
                && board[8] == board[12] && board[12] == board[17]) {
            return true
        } else if ((to == 5 || to == 13 || to == 20)
                && board[5] == board[13] && board[13] == board[20]) {
            return true
        } else if ((to == 2 || to == 14 || to == 23)
                && board[2] == board[14] && board[14] == board[23]) {
            return true
        } else if ((to == 16 || to == 19 || to == 22)
                && board[16] == board[19] && board[19] == board[22]) {
            return true
        }
        return false
    }

    /**
     * Request to remove a marker for the selected player.
     * Returns true if the marker was successfully removed
     */
    fun remove(from: Int, color: Int): Boolean {
        pieceJustMoved=false
        return if ((board[from] == color && !canRemove(from))
                || (board[from] == color && millsOnly(color))){
            board[from] = EMPTY_SPACE
            pieceToBeRemoved=false
            if(win(if(turn==RED_TURN) BLUE_PIECE else RED_PIECE)){
                gameOver = true
                return true
            }
            turn=3-turn //2 becomes 1 and so on
            true
        } else{
            false
        }
    }

    private fun millsOnly(color: Int): Boolean{
        for(piece in board.indices){
            if(board(piece)==color){
                if(!canRemove(piece)) return false
            }
        }
        return true
    }
    /**
     * Returns number of pieces of a given color on the board
     */
    private fun piecesOnBoard(color: Int): Int{
        var count = 0
        board.forEach {if(it==color) count++}
        return count
    }

    /**
     * Returns true if the current player has won.
     * This function is called when a player removes an opponent's piece
     */
    private fun win(opponent: Int): Boolean {
        val opponentPieces = piecesOnBoard(opponent)
        return (opponent==BLUE_PIECE && bluePieces < 3 && opponentPieces < 3) || (opponent== RED_PIECE && redPieces < 3 && opponentPieces < 3)
    }

    /**
     * Returns EMPTY_SPACE = 0 BLUE_PIECE = 4 RED_PIECE = 5
     */
    fun board(from: Int): Int {
        return board[from]
    }

    /**
     * Check whether this is a legal move.
     */
    private fun isValidMove(to: Int, from: Int): Boolean {
        if (board[to] != EMPTY_SPACE) return false
        if(turn==RED_TURN && piecesOnBoard(RED_PIECE) == 3) return true //flying when player only has 3 pieces left
        if(turn==BLUE_TURN && piecesOnBoard(BLUE_PIECE) == 3) return true
        when (to) {
            0 -> return from == 1 || from == 9
            1 -> return from == 0 || from == 2 || from == 4
            2 -> return from == 14|| from == 1
            3 -> return from == 4 || from == 10
            4 -> return from == 1 || from == 3 || from == 5 || from == 7
            5 -> return from == 4 || from == 13
            6 -> return from == 11 || from == 7
            7 -> return from == 4 || from == 6 || from == 8
            8 -> return from == 7 || from == 12
            9 -> return from == 0 || from == 10 || from == 21
            10 -> return from == 3 || from == 9 || from == 11 || from == 18
            11 -> return from == 6 || from == 15 || from == 10
            12 -> return from == 8 || from == 13 || from == 17
            13 -> return from == 12 || from == 14 ||  from == 5 || from == 20
            14 -> return from == 2 || from == 13 || from == 23
            15 -> return from == 11 || from == 16
            16 -> return from == 15 || from == 17 || from == 19
            17 -> return from == 12 || from == 16
            18 -> return from == 10 || from == 19
            19 -> return from == 16 || from == 20 || from == 22 || from == 18
            20 -> return from == 13 || from == 19
            21 -> return from == 9 || from == 22
            22 -> return from == 19 || from == 21 || from == 23
            23 -> return from == 14 || from == 22
        }
        return false
    }
}