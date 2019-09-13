# Demagnetize [![Demagnetize CurseForge Badge](http://cf.way2muchnoise.eu/full_demagnetize_magnets%20demagnetized.svg)](https://www.curseforge.com/minecraft/mc-mods/demagnetize)
***This is the 1.14 branch, see the 1.12 version [here](https://github.com/comp500/Demagnetize/tree/1.12)!***

Item magnets are very helpful when mining, however they can disrupt item-based automation, such as the Atomic Reconstructor from Actually Additions. The Demagnetizer is a solution to this, which prevents item magnets from functioning within an area around them.

Demagnetizers can also be filtered or redstone controlled, so that they only activate when necessary. The Advanced Demagnetizer has a larger range and more filter slots.

![Demagnetizer demo gif](https://i.imgur.com/T0QpQ6r.gif)

### Mod compatibility:
*Some of these mods are not on 1.14 yet, but they should support Demagnetize when they update*

- Thermal series
- Actually Additions
- Botania
- Draconic Evolution
- Ender IO
- Reliquary
- Simple Magnet
- Not Enough Items 1.8+
- Quantum Flux
- Pickle Tweaks
- Better Magnets
- Ring of Attraction
- Gobber

### For mod developers:
If your mod has an Item Magnet, do not pick up items if the `PreventRemoteMovement` NBT tag exists on the EntityItem.
If your mod has a item collector block (e.g. the Ranged Collector or the Vacuum Chest), do not pick up items if the `PreventRemoteMovement` NBT tag exists on the EntityItem, but if the `AllowMachineRemoteMovement` NBT tag exists, ignore the previous NBT tag. This is to ensure that Item Magnets do not pick up items on conveyors or demagnetized items, while also ensuring that item collector blocks are not restricted by the Demagnetizer.

For more information, see [this github issue](https://github.com/CoFH/Feedback/issues/1243#issuecomment-414012846).
