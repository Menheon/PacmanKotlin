package org.pondar.pacmankotlin

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

// Custom view for the game.
class GameView : View {
    private lateinit var game: Game

    // Height and width of the game view.
    private var h: Int = 0
    private var w: Int = 0

    fun setGame(game: Game) {
        this.game = game
    }

    /* The next 3 constructors are needed for the Android view system,
	when we have a custom view.
	 */
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    // Draw functionality for the canvas.
    override fun onDraw(canvas: Canvas) {
        // Here we get the height and weight.
        h = height
        w = width
        // Update the size for the canvas to the game.
        game.setSize(h, w)

        // Initialize coins, cherries and enemies if not already initialized.
        if (!game.isPickupItemsInitialized) {
            game.initializePickupItems(game.coinAmount, game.coins, R.drawable.gold_coin)
            game.initializePickupItems(game.cherryAmount, game.cherries, R.drawable.cherry)
        }
        if (!game.isGhostsInitialized)
            game.initializeGhosts()

        // Making a new paint object
        val paint = Paint()

        // Loop through the list of gold coins and draw them on the canvas.
        for (coin in game.coins) {
            if (!coin.isConsumed) {
                canvas.drawBitmap(
                    coin.bitmap,
                    coin.coordX.toFloat(),
                    coin.coordY.toFloat(),
                    paint
                )
            }
        }
        // Loop through the list of cherries and draw them on the canvas.
        for (cherry in game.cherries) {
            if (!cherry.isConsumed) {
                canvas.drawBitmap(
                    cherry.bitmap,
                    cherry.coordX.toFloat(),
                    cherry.coordY.toFloat(),
                    paint
                )
            }
        }
        // Loop through the list of ghosts and draw them on the canvas.
        for (ghost in game.ghosts) {
            if (!ghost.isConsumed) {
                canvas.drawBitmap(
                    ghost.bitmap,
                    ghost.coordX.toFloat(),
                    ghost.coordY.toFloat(),
                    paint
                )
            }
        }
        // Draw Pacman on the canvas.
        if (!game.isConsumed) {
            canvas.drawBitmap(
                game.pacBitmap, game.pacX.toFloat(),
                game.pacY.toFloat(), paint
            )
        }
        game.doCollisionCheck()
        super.onDraw(canvas)
    }
}