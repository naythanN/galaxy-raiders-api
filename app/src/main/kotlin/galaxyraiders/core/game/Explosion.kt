package galaxyraiders.core.game

import galaxyraiders.core.physics.Object2D
import galaxyraiders.core.physics.Point2D
import galaxyraiders.core.physics.Vector2D

open class Explosion(
  var is_triggered: Boolean,
  position: Point2D,
  var duration: Int
) :
  SpaceObject("Explosion", '*', position, Vector2D(0.0, 0.0), 0.0, 0.0)
