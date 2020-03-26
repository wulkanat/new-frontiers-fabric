package de.wulkanat.new_frontiers.world.dimension

import de.wulkanat.new_frontiers.MOD_ID
import de.wulkanat.new_frontiers.extensions.server.getWorldsRaw
import de.wulkanat.new_frontiers.extensions.server.world.getWorldGenerationProgressListener
import de.wulkanat.new_frontiers.extensions.server.world.getWorldSaveHandler
import net.fabricmc.fabric.api.dimension.v1.FabricDimensionType
import net.minecraft.block.pattern.BlockPattern
import net.minecraft.entity.Entity
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.SecondaryServerWorld
import net.minecraft.server.world.ServerChunkManager
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.world.Heightmap
import net.minecraft.world.World
import net.minecraft.world.dimension.Dimension
import net.minecraft.world.dimension.DimensionType

abstract class DynamicDimension(world: World, dimension: DimensionType, f: Float) : Dimension(world, dimension, f) {
    lateinit var dimType: DimensionType

    override fun getType(): DimensionType {
        return dimType
    }

    companion object {
        private fun <T : DynamicDimension> register(name: String, dimension: (world: World, type: DimensionType) -> T): FabricDimensionType {
            lateinit var type2: FabricDimensionType
            type2 = FabricDimensionType.builder()
                .defaultPlacer { oldEntity: Entity, destinationWorld: ServerWorld, _: Direction?, _: Double, _: Double ->
                    BlockPattern.TeleportTarget(
                        Vec3d(
                            destinationWorld.getTopPosition(
                                Heightmap.Type.WORLD_SURFACE,
                                BlockPos.ORIGIN
                            )
                        ),
                        oldEntity.velocity,
                        oldEntity.yaw.toInt()
                    )
                }
                .factory { world, type ->
                    val dim = dimension(world, type)
                    dim.dimType = type2
                    dim
                }
                .skyLight(false)
                .buildAndRegister(Identifier(MOD_ID, name))

            return type2
        }

        fun <T : DynamicDimension> createDimension(id: String, server: MinecraftServer, dimension: (world: World, type: DimensionType) -> T): Boolean {
            DimensionType.byId(Identifier(MOD_ID, id)) ?: run {
                println("Creating dimension '$id'")

                val world = server.getWorld(DimensionType.OVERWORLD)

                val type2 = this.register(id, dimension)
                type2.create(world)

                val worlds = server.getWorldsRaw()
                worlds.put(
                    type2, SecondaryServerWorld(
                        world,
                        server,
                        server.workerExecutor,
                        world.getWorldSaveHandler(),
                        type2,
                        server.profiler,
                        (world!!.chunkManager as ServerChunkManager).threadedAnvilChunkStorage.getWorldGenerationProgressListener()
                    )
                )

                return@createDimension true
            }

            return false
        }
    }
}