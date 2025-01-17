package galaxyraiders.core.game

import galaxyraiders.Config
import galaxyraiders.core.physics.Point2D
import galaxyraiders.ports.RandomGenerator
import galaxyraiders.ports.ui.Controller
import galaxyraiders.ports.ui.Controller.PlayerCommand
import galaxyraiders.ports.ui.Visualizer
import kotlin.system.measureTimeMillis

const val MILLISECONDS_PER_SECOND: Int = 1000
const val PESO_NAVE = 9
const val PESO_ASTEROIDE = 1

object GameEngineConfig {
    private val config = Config(prefix = "GR__CORE__GAME__GAME_ENGINE__")

    val frameRate = config.get<Int>("FRAME_RATE")
    val spaceFieldWidth = config.get<Int>("SPACEFIELD_WIDTH")
    val spaceFieldHeight = config.get<Int>("SPACEFIELD_HEIGHT")
    val asteroidProbability = config.get<Double>("ASTEROID_PROBABILITY")
    val coefficientRestitution = config.get<Double>("COEFFICIENT_RESTITUTION")

    val msPerFrame: Int = MILLISECONDS_PER_SECOND / this.frameRate
}

@Suppress("TooManyFunctions")
class GameEngine(
    val generator: RandomGenerator,
    val controller: Controller,
    val visualizer: Visualizer,
) {
    val field = SpaceField(
        width = GameEngineConfig.spaceFieldWidth,
        height = GameEngineConfig.spaceFieldHeight,
        generator = generator
    )

    var playing = true

    fun execute() {
        while (true) {
            val duration = measureTimeMillis { this.tick() }

            Thread.sleep(
                maxOf(0, GameEngineConfig.msPerFrame - duration)
            )
        }
    }

    fun execute(maxIterations: Int) {
        repeat(maxIterations) {
            this.tick()
        }
    }

    fun tick() {
        this.processPlayerInput()
        this.updateSpaceObjects()
        this.renderSpaceField()
    }

    fun processPlayerInput() {
        this.controller.nextPlayerCommand()?.also {
            when (it) {
                PlayerCommand.MOVE_SHIP_UP ->
                    this.field.ship.boostUp()
                PlayerCommand.MOVE_SHIP_DOWN ->
                    this.field.ship.boostDown()
                PlayerCommand.MOVE_SHIP_LEFT ->
                    this.field.ship.boostLeft()
                PlayerCommand.MOVE_SHIP_RIGHT ->
                    this.field.ship.boostRight()
                PlayerCommand.LAUNCH_MISSILE ->
                    this.field.generateMissile()
                PlayerCommand.PAUSE_GAME ->
                    this.playing = !this.playing
            }
        }
    }

    fun updateSpaceObjects() {
        if (!this.playing) return
        this.handleCollisions()
        this.moveSpaceObjects()
        this.trimSpaceObjects()
        this.generateAsteroids()
    }

    fun handleCollisions() {
        val listRemoval: MutableList<SpaceObject> = arrayListOf()

        this.field.spaceObjects.forEachPair {
                (first, second) ->
            if (first.impacts(second) && first.type != "Explosion" && second.type != "Explosion") {
                first.collideWith(second, GameEngineConfig.coefficientRestitution)
                val collidePointX = (
                    first.center.x * PESO_NAVE +
                        second.center.x * PESO_ASTEROIDE
                    ) / (PESO_ASTEROIDE + PESO_NAVE)
                val collidePointY = (
                    first.center.y * PESO_NAVE +
                        second.center.y * PESO_ASTEROIDE
                    ) / (PESO_ASTEROIDE + PESO_NAVE)

                val collidePoint = Point2D(collidePointX, collidePointY)
                this.field.generateExplosion(collidePoint)
                listRemoval.add(first)
                listRemoval.add(second)

                // this.field.spaceObjects = this.field.spaceObjects.filter{it != first && it != second}
            }
        }
        this.field.spaceObjects = this.field.spaceObjects.filter { listRemoval.contains(it) }
    }

    fun moveSpaceObjects() {
        this.field.moveShip()
        this.field.moveAsteroids()
        this.field.moveMissiles()
    }

    fun trimSpaceObjects() {
        this.field.trimAsteroids()
        this.field.trimMissiles()
        this.field.trimExplosions()
        this.field.updateExplosions()
    }

    fun generateAsteroids() {
        val probability = generator.generateProbability()

        if (probability <= GameEngineConfig.asteroidProbability) {
            this.field.generateAsteroid()
        }
    }

    fun renderSpaceField() {
        this.visualizer.renderSpaceField(this.field)
    }
}

fun <T> List<T>.forEachPair(action: (Pair<T, T>) -> Unit) {
    for (i in 0 until this.size) {
        for (j in i + 1 until this.size) {
            action(Pair(this[i], this[j]))
        }
    }
}
