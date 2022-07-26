package galaxyraiders.core.game

import galaxyraiders.core.physics.Point2D
import galaxyraiders.core.physics.Vector2D

const val RADIUS_EXPLOSION = 5.0
const val MASS_EXPLOSION = 1000000.0
open class Explosion(
    position: Point2D,
    var duration: Int
) :
    SpaceObject("Explosion", '*', position, Vector2D(0.0, 0.0), RADIUS_EXPLOSION, MASS_EXPLOSION)
