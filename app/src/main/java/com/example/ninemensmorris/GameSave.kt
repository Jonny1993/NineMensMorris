package com.example.ninemensmorris

import java.io.Serializable

class GameSave(val board: IntArray,
               val pieceHighlighted: Boolean,
               val from: Int,
               val bluePieces: Int,
               val redPieces: Int,
               val turn: Int,
               val pieceJustMoved: Boolean,
               val gameOver: Boolean,
               val pieceToBeRemoved: Boolean): Serializable {
    override fun toString(): String {
        return "GameSave(board=${board.contentToString()}, pieceHighlighted=$pieceHighlighted, from=$from, bluePieces=$bluePieces, redPieces=$redPieces, turn=$turn, pieceJustMoved=$pieceJustMoved, gameOver=$gameOver, pieceToBeRemoved=$pieceToBeRemoved)"
    }
}