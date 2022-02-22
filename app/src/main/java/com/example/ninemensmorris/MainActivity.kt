package com.example.ninemensmorris

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

class MainActivity : AppCompatActivity() {
    private var model = NMMRules()
    private val pieces = ArrayList<ImageView>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        for(i in 0 until 24){
                pieces.add(findViewById(resources.getIdentifier("point_$i", "id", this.packageName)))
        }
    }

    override fun onStop() {
        super.onStop()
        model.saveGame(this)
        Log.d("onStop", "onStop called!")
    }

    override fun onStart() {
        super.onStart()
        Log.d("onStart", "onStart called!")
        if(model.loadGame(this)){
            updateViewOnLoad()
            updatePlayerStats()
        }
    }

    fun onNewGameClicked(view: View){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Warning!")
        builder.setMessage("Are you sure you want to start a new game?")
        builder.setPositiveButton("Yes"){_, _ ->
            model.newGame()
            updateViewNewGame()
        }
        builder.setNegativeButton("No"){_, _ ->}
        val dialog = builder.create()
        dialog.show()
    }

    private fun updateViewNewGame(){
        pieces.forEach{it.setImageResource(R.drawable.empty_spot)}
        updatePlayerStats()
    }

    fun onTileClicked(view: View){
        val tile = view as ImageView
        val imageName = resources.getResourceName(view.id)
        val position = imageName.substringAfter("_").toInt()
        Log.d("Clicked", "The clicked image was: $imageName")
        //pieces[position].setImageResource(R.drawable.blue_piece) //for testing
        if(model.pieceToBeRemoved){
            val opponent = if(model.turn==NMMRules.BLUE_TURN)
                NMMRules.RED_PIECE
            else NMMRules.BLUE_PIECE
            if(model.remove(position,opponent)) updatePlayerStats()
            else Toast.makeText(this, "Move not allowed!", Toast.LENGTH_SHORT).show()
        }
        else{
            if(model.legalMove(position, model.turn)) {
                updatePlayerStats()
            }else{
                if(!model.gameOver) Toast.makeText(this, "Move not allowed!", Toast.LENGTH_SHORT).show()
            }
        }
        updateView(tile)
        if(model.gameOver) {
            displayWinMessage()
        }
    }

    private fun displayWinMessage(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Congratulations!")
        builder.setMessage(if(model.turn==NMMRules.BLUE_TURN)"Blue has won the game!" else "Red has won the game!" + " Starting a new game.")
        builder.setPositiveButton("Ok"){_, _ ->
            model.newGame()
            updateViewNewGame()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun animateMovement(tile: ImageView){
        //Reading coordinates of "from" tile
        val fromNr=model.from
        val idTileFrom = resources.getIdentifier("point_$fromNr", "id", this.packageName)
        val fromTileImage=findViewById<ImageView>(idTileFrom)
        val animImg=findViewById<ImageView>(resources.getIdentifier("point_25", "id", this.packageName))
        //Storing position of hidden image (To retrieve it later so that it stays out of the board.xml.xml)
        val posXY = IntArray(2)
        animImg.getLocationOnScreen(posXY)
        val oldX = posXY[0]
        val oldY = posXY[1]
        //Setting hidden image to take the same position of from tile
        animImg.x=fromTileImage.x
        animImg.y=fromTileImage.y

        //Animating
        val movementXAnimator= ObjectAnimator.ofFloat(animImg,"x",tile.left.toFloat())
        val movementYAnimator= ObjectAnimator.ofFloat(animImg,"y",tile.top.toFloat())
        val moveAnimSet=AnimatorSet()
        moveAnimSet.playTogether(movementXAnimator,movementYAnimator)
        moveAnimSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                //Make hidden image visible before animation is performed
                val tileTo = resources.getResourceName(tile.id).substringAfter("_").toInt()
                animImg.setImageResource(if(model.board(tileTo) == NMMRules.RED_PIECE) R.drawable.red_piece else R.drawable.blue_piece)
                animImg.visibility=VISIBLE
            }

            override fun onAnimationRepeat(animation: Animator) {
            }

            override fun onAnimationEnd(animation: Animator) {
                //Make image hidden and place it back in its place so it doesn't block other tiles
                animImg.x=oldX.toFloat()
                animImg.y=oldY.toFloat()
                animImg.visibility=INVISIBLE
                Log.d("Clicked","AnimationEnd")
            }

            override fun onAnimationCancel(animation: Animator) {
            }
        })
        moveAnimSet.start()
    }

    private fun updateView(tile: ImageView){
        if(model.pieceJustMoved) animateMovement(tile)
        var name: String
        for(i in 0 until 24) {
            name = "point_$i"
            val id = resources.getIdentifier(name, "id", this.packageName)
            val image = findViewById<ImageView>(id)
            when(model.board(i)){
                NMMRules.RED_PIECE ->
                    if(model.pieceHighlighted && model.turn==NMMRules.RED_TURN)
                        tile.setImageResource(R.drawable.red_highlighted)
                    else image.setImageResource(R.drawable.red_piece)
                NMMRules.BLUE_PIECE ->
                    if(model.pieceHighlighted && model.turn==NMMRules.BLUE_TURN)
                        tile.setImageResource(R.drawable.blue_highlighted)
                    else image.setImageResource(R.drawable.blue_piece)
                else -> image.setImageResource(R.drawable.empty_spot)
            }
        }
    }

    private fun updateViewOnLoad(){
        var name: String
        for(i in 0 until 24) {
            name = "point_$i"
            val id = resources.getIdentifier(name, "id", this.packageName)
            val image = findViewById<ImageView>(id)
            when(model.board(i)){
                NMMRules.RED_PIECE -> image.setImageResource(R.drawable.red_piece)
                NMMRules.BLUE_PIECE -> image.setImageResource(R.drawable.blue_piece)
                else -> image.setImageResource(R.drawable.empty_spot)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updatePlayerStats(){
        val player = findViewById<TextView>(R.id.turn_text)
        val pieces = findViewById<TextView>(R.id.remaining_pieces)
        if(model.turn == NMMRules.BLUE_TURN){
            player.text = " BLUE"
            player.setTextColor(Color.BLUE)
            pieces.text = " ${model.bluePieces}"
        }
        else{
            player.text = " RED"
            player.setTextColor(Color.RED)
            pieces.text = " ${model.redPieces}"
        }
    }
}