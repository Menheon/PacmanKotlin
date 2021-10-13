package org.pondar.pacmankotlin

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.widget.TextView
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.pow
import kotlin.math.sqrt


// Class containing game logic.
class Game(private var context: Context, view: TextView) {
    private var pointsView: TextView = view
    private lateinit var gameView: GameView

    // Pacman bitmap, position and angle.
    var pacBitmap: Bitmap
    var pacX = 0
    var pacY = 0
    var currentBitmapRotationAngle = 0F
    var isConsumed = false

    // Directional values for pacman.
    val STILL = 0
    val UP = 1
    val RIGHT = 2
    val DOWN = 3
    val LEFT = 4
    var currentDirection = STILL

    // Pick-up items
    var isPickupItemsInitialized = false
    var coins = ArrayList<Entity>()
    val coinAmount = 10
    private val coinValue = 1
    var cherries = ArrayList<Entity>()
    val cherryAmount = 1
    private val cherryValue = 5
    var currentPoints = 0
    var maxPoints = 0
    private val spaceBetweenEntities = 100

    // Ghost enemies
    var ghosts = ArrayList<Ghost>()
    private val ghostAmount = 2
    private val ghostValue = 10
    var isGhostsInitialized = false

    // Height and width of screen.
    var h: Int = 0
    var w: Int = 0

    // Game status.
    var isGameWon = false
    var isGameOver = false
    var countDownTime = 0

    // Initializes the pacman bitmap on creation.
    init {
        pacBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.pacman)
        pacBitmap =
            Bitmap.createScaledBitmap(
                pacBitmap,
                pacBitmap.width / 2,
                pacBitmap.height / 2,
                false
            )
        maxPoints = cherryAmount * cherryValue + ghostAmount * ghostValue + coinAmount * coinValue
    }

    fun setGameView(view: GameView) {
        this.gameView = view
    }

    // Initialize the pickup items' coordinates.
    fun initializePickupItems(amount: Int, collection: ArrayList<Entity>, resourceId: Int) {
        for (i in 0 until amount) {
            var newItem = Entity(context, resourceId)
            val coordinates = getNewEntityCoordinates(newItem.bitmap)
            newItem.setCoordinates(coordinates[0], coordinates[1])
            collection.add(newItem)
        }
        isPickupItemsInitialized = true
    }

    // Initialize the ghosts' coordinates.
    fun initializeGhosts() {
        for (i in 0 until ghostAmount) {
            var ghost: Ghost = if (i % 2 == 0) {
                Ghost(context, R.drawable.ghost_normal_red)
            } else {
                Ghost(context, R.drawable.ghost_normal_blue)
            }
            val coordinates = getNewEntityCoordinates(ghost.bitmap)
            ghost.setCoordinates(coordinates[0], coordinates[1])
            ghosts.add(ghost)
        }
        isGhostsInitialized = true
    }

    // Find random coordinates with even space between other entities.
    private fun getNewEntityCoordinates(entityBitmap: Bitmap): ArrayList<Int> {
        val coordinates = ArrayList<Int>()
        var itemX = 0
        var itemY = 0
        var isValidCoords = false
        val rng = Random()
        while (!isValidCoords) {
            itemX = rng.nextInt(w - entityBitmap.width)
            itemY = rng.nextInt(h - entityBitmap.height)
            // Ensure that the items will be evenly placed.
            if (itemX % spaceBetweenEntities == 0 && itemY % spaceBetweenEntities == 0) {
                // Check if current entity is too close to the coins.
                for (c in coins) {
                    var distance = calculateDistance(
                        c.coordX / 2,
                        c.coordY / 2,
                        itemX / 2,
                        itemY / 2
                    )
                    isValidCoords = distance > spaceBetweenEntities
                    if (!isValidCoords) break
                }
                // Check if current entity is too close to the ghosts.
                if (isValidCoords) {
                    for (ghost in ghosts) {
                        var distance = calculateDistance(
                            ghost.coordX / 2,
                            ghost.coordY / 2,
                            itemX / 2,
                            itemY / 2
                        )
                        isValidCoords = distance > spaceBetweenEntities
                        if (!isValidCoords) break
                    }
                }
                // Check if current entity is too close to the cherries.
                if (isValidCoords) {
                    for (cherry in cherries) {
                        var distance = calculateDistance(
                            cherry.coordX / 2,
                            cherry.coordY / 2,
                            itemX / 2,
                            itemY / 2
                        )
                        isValidCoords = distance > spaceBetweenEntities
                        if (!isValidCoords) break
                    }
                }
                // Check if current coin is too close to the pacman.
                if (isValidCoords) {
                    var distance = calculateDistance(
                        pacX / 2,
                        pacY / 2,
                        itemX / 2,
                        itemY / 2
                    )
                    isValidCoords = distance > spaceBetweenEntities
                } else {
                    isValidCoords = true
                }
            }
        }
        if (isValidCoords) {
            coordinates.add(itemX)
            coordinates.add(itemY)
        }
        return coordinates
    }

    // Initializes a new game and resets values and properties.
    fun newGame() {
        // Reset pacman position, direction, rotation and image.
        pacX = 50
        pacY = 400
        isConsumed = false
        currentDirection = STILL
        rotatePacman(pacBitmap, 0F)
        pacBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.pacman)
        pacBitmap =
            Bitmap.createScaledBitmap(
                pacBitmap,
                pacBitmap.width / 2,
                pacBitmap.height / 2,
                false
            )

        // Reset game values.
        countDownTime = 30
        isGameWon = false
        isGameOver = false

        // Reset the points.
        currentPoints = 0
        coins = ArrayList()
        isPickupItemsInitialized = false
        pointsView.post {
            pointsView.text = context.resources.getString(R.string.points, currentPoints)
        }
        cherries = ArrayList()

        // Reset the ghosts.
        ghosts = ArrayList()
        isGhostsInitialized = false

        // Redraw screen.
        gameView.invalidate()
    }

    // Sets the size of the game view.
    fun setSize(h: Int, w: Int) {
        this.h = h
        this.w = w
    }

    // Rotates and flips the pac man bit map in the right direction.
    private fun rotatePacman(pacBitmap: Bitmap, angle: Float) {
        if (angle != currentBitmapRotationAngle) {
            // Create a matrix, with rotated values that will be applied to the pacman bitmap.
            var matrix = Matrix()
            val centerX = pacX / 2F
            val centerY = pacY / 2F
            if (angle == 180F) {
                matrix.postRotate(-currentBitmapRotationAngle) // Reset to default angle = 0F.
                matrix.postScale(-1F, 1F, centerX, centerY)
            } else if (currentBitmapRotationAngle == 180F) {
                // If pac man is already flipped to left, and needs to flip to right, no rotation is needed - only a flip.
                if (angle != 0F)
                    matrix.postRotate(-currentBitmapRotationAngle + angle)
                matrix.postScale(-1F, 1F, centerX, centerY)
            } else {
                matrix.postRotate(-currentBitmapRotationAngle + angle)
            }
            // Create and assign the rotated bitmap.
            this.pacBitmap = Bitmap.createBitmap(
                pacBitmap,
                0,
                0,
                pacBitmap.width,
                pacBitmap.height,
                matrix,
                false
            )
            currentBitmapRotationAngle = angle
        }
    }

    // Moves the pac man a specific amount of pixels to the right.
    fun movePacmanRight(pixels: Int) {
        if (pacX + pixels + pacBitmap.width < w) {
            rotatePacman(pacBitmap, 0F)
            // Move ghosts in opposite direction, only if pacman is actually moving.
            if (pacX + pixels != pacX)
                for (ghost in ghosts) {
                    ghost.moveGhostLeft(pixels)
                }
            pacX += pixels
            doCollisionCheck()
        }
    }

    // Moves the pac man a specific amount of pixels to the left.
    fun movePacmanLeft(pixels: Int) {
        if (pacX - pixels > 0) {
            rotatePacman(pacBitmap, 180F)
            // Move ghosts in opposite direction, only if pacman is actually moving.
            if (pacX - pixels != pacX)
                for (ghost in ghosts) {
                    ghost.moveGhostRight(pixels, w)
                }
            pacX -= pixels
            doCollisionCheck()
        }
    }

    // Moves the pac man a specific amount of pixels downwards.
    fun movePacmanDown(pixels: Int) {
        if (pacY + pixels + pacBitmap.height < h) {
            rotatePacman(pacBitmap, 90F)
            // Move ghosts in opposite direction, only if pacman is actually moving.
            if (pacY + pixels != pacY)
                for (ghost in ghosts) {
                    ghost.moveGhostUp(pixels)
                }
            pacY += pixels
            doCollisionCheck()
        }
    }

    // Moves the pac man a specific amount of pixels upwards.
    fun movePacmanUp(pixels: Int) {
        if (pacY - pixels > 0) {
            rotatePacman(pacBitmap, -90F)
            // Move ghosts in opposite direction, only if pacman is actually moving.
            if (pacY - pixels != pacY)
                for (ghost in ghosts) {
                    ghost.moveGhostDown(pixels, h)
                }
            pacY -= pixels
            doCollisionCheck()
        }
    }

    // Completes collision checks for the pac man with gold coins and enemies
    fun doCollisionCheck() {
        val oldPoints = currentPoints
        // Check for collision with the gold coins.
        for (coin in coins) {
            if (!coin.isConsumed) {
                val distance = calculateDistance(
                    pacX + pacBitmap.width / 2, pacY + pacBitmap.height / 2,
                    coin.coordX + coin.bitmap.width / 2, coin.coordY + coin.bitmap.height / 2
                )
                if (distance < coin.bitmap.width) {
                    coin.isConsumed = true
                    currentPoints += coinValue
                }
            }
        }
        // Check for collision with the ghosts.
        for (ghost in ghosts) {
            if (!ghost.isConsumed) {
                val distance = calculateDistance(
                    pacX + pacBitmap.width / 2,
                    pacY + pacBitmap.height / 2,
                    ghost.coordX + ghost.bitmap.width / 2,
                    ghost.coordY + ghost.bitmap.height / 2
                )
                if (distance < ghost.bitmap.width) {
                    if (!ghost.isVulnerable) {
                        isConsumed = true
                        isGameOver = true
                    } else {
                        ghost.isConsumed = true
                        currentPoints += ghostValue
                    }
                }
            }
        }
        // Check for collision with the cherry power-ups.
        for (cherry in cherries) {
            if (!cherry.isConsumed) {
                val distance = calculateDistance(
                    pacX + pacBitmap.width / 2,
                    pacY + pacBitmap.height / 2,
                    cherry.coordX + cherry.bitmap.width / 2,
                    cherry.coordY + cherry.bitmap.height / 2
                )
                if (distance < cherry.bitmap.width) {
                    cherry.isConsumed = true
                    currentPoints += cherryValue

                    for (ghost in ghosts) {
                        ghost.isVulnerable = true
                        var newBitmap = BitmapFactory.decodeResource(
                            context.resources,
                            R.drawable.ghost_vulnerable
                        )
                        ghost.bitmap =
                            Bitmap.createScaledBitmap(
                                newBitmap,
                                ghost.bitmap.width,
                                ghost.bitmap.height,
                                false
                            )
                    }
                }
            }
        }
        // Check if any points have been collected.
        if (oldPoints != currentPoints) {
            pointsView.post {
                pointsView.text =
                    context.resources.getString(R.string.points, currentPoints)
            }
            if (currentPoints >= maxPoints) { // TODO fix bug where it is possible to get more than max points.
                isGameWon = true
                isGameOver = true
            }
        }
    }

    // Calculates the distance between two 2D coordinates using the Pythagoras theorem.
    private fun calculateDistance(x1: Int, y1: Int, x2: Int, y2: Int): Float {
        val xDiff = (x2 - x1).toDouble()
        val yDiff = (y2 - y1).toDouble()
        return sqrt(xDiff.pow(2) + yDiff.pow(2)).toFloat()
    }
}