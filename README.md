This is a Minecraft mod project (using Forge Modloader), that adds a special portal mechanic to the game to vertically attach different dimensions onto each other.
This way it effectively "fake extends" the build height / depth.  
_A real extension of the build height is nothing a Forge mod could ever do as it would require a rewriting most of the game engine and therefore destroy all mod compatibility._

# How it works:
Upon world generation the top and/or bottom most block layer of configured dimensions is filled with special portal blocks.
These portal blocks are indestructable like bedrock and allow players and other entities to travel to the next lower or higher configured dimension when moving through them.

They also mimic the existence (or non existence) of solid blocks in the first two layers adjacent to the portal in the other dimension.
This includes the abillity to break and place blocks in these layers via interaction through the portal.
This is to make the transition more smooth and avoid the problems with travelling upwards through portals (immediately falling back when reaching the other side).

The primitivity of this mimicing (only distinguishing between solid or not) is mainly due to the very limited amount of information that can be stored in block states while using TileEntities would have a serious impact on performace and ram usage due to the massive amount of blocks and is therefore avoided.

# Configurability
Which dimensions are stacked on top of each other is freely configurable to give modpack developers freedom to design the world structure however they like. It is also be possible include dimensions added by other mods simply via dimension id.

The default setting just connects overworld and nether this way. There are additional world generation features planned to fill the upper half of the nether dimension with something.
