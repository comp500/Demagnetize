# Demagnetize
Demagnetizers prevent item magnets from various mods from picking up items around them. They can be filtered or redstone controlled. The Advanced Demagnetizer has a larger range and more filter slots.

![Demagnetizer demo gif](https://i.imgur.com/T0QpQ6r.gif)

#### For mod developers:
If your mod has an Item Magnet, do not pick up items if the PreventRemoteMovement NBT tag exists on the EntityItem.
If your mod has a item collector block (e.g. the Ranged Collector or the Vacuum Chest), do not pick up items if the PreventRemoteMovement NBT tag exists on the EntityItem, but if the AllowMachineRemoteMovement NBT tag exists, ignore the previous NBT tag.
