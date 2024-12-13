package de.jackBeBack.dnc.data

import Transform

val circularAreaOfEffect = { distance: Int, source: Transform?, target: Transform? ->
    if (source == null || target == null) false else source.distanceTo(target) <= distance
}