package com.skyresourcesclassic.technology.block;

import com.skyresourcesclassic.References;
import com.skyresourcesclassic.SkyResourcesClassic;
import com.skyresourcesclassic.base.block.IMetaBlockName;
import com.skyresourcesclassic.registry.ModCreativeTabs;
import com.skyresourcesclassic.registry.ModGuiHandler;
import com.skyresourcesclassic.technology.tile.TileCombustionHeater;
import com.skyresourcesclassic.technology.tile.TilePoweredCombustionHeater;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CombustionHeaterBlock extends BlockContainer implements IMetaBlockName {
    public static final PropertyEnum<CombustionHeaterVariants> heaterVariant = PropertyEnum.create("variant",
            CombustionHeaterVariants.class);

    private String[] combustionHeaterTypes = new String[]{"wood", "iron", "steel", "dark_matter"};

    public CombustionHeaterBlock(String name, float hardness, float resistance) {
        super(Material.WOOD);
        this.setUnlocalizedName(References.ModID + "." + name);
        this.setCreativeTab(ModCreativeTabs.tabTech);
        this.setHardness(hardness);
        this.setResistance(resistance);
        this.setRegistryName(name);

        setDefaultState(blockState.getBaseState().withProperty(heaterVariant, CombustionHeaterVariants.WOOD));
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public Material getMaterial(IBlockState state) {
        switch (getMetaFromState(state)) {
            case 0:
                return Material.WOOD;
            case 1:
                return Material.IRON;
            case 2:
                return Material.IRON;
        }
        return Material.IRON;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        if (meta >= 2)
            return new TilePoweredCombustionHeater();
        return new TileCombustionHeater();
    }

    @Override
    public BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, heaterVariant);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(heaterVariant).ordinal();
    }

    @Override
    public int damageDropped(IBlockState state) {
        return getMetaFromState(state);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        if (meta >= CombustionHeaterVariants.values().length || meta < 0) {
            meta = 0;
        }
        return getDefaultState().withProperty(heaterVariant, CombustionHeaterVariants.values()[meta]);
    }

    @Override
    public void getSubBlocks(CreativeTabs par2, NonNullList<ItemStack> par3)
    {
        par3.add(new ItemStack(this, 1, 0));
        par3.add(new ItemStack(this, 1, 1));
        par3.add(new ItemStack(this, 1, 2));
        par3.add(new ItemStack(this, 1, 3));
    }

    public enum CombustionHeaterVariants implements IStringSerializable {
        WOOD, IRON, STEEL, DARKMATTER;

        @Override
        public String getName() {
            return name().toLowerCase(Locale.ROOT);
        }

    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if (this.getMetaFromState(state) < 2) {
            TileCombustionHeater te = (TileCombustionHeater) world.getTileEntity(pos);
            te.dropInventory();
        }

        super.breakBlock(world, pos, state);
    }

    public int getMaximumHeat(IBlockState state) {
        // Celsius
        // Because it's superior to F
        if (getMetaFromState(state) == 0) {
            return 100;
        } else if (getMetaFromState(state) == 1) {
            return 1538;
        } else if (getMetaFromState(state) == 2) {
            return 2750;
        } else if (getMetaFromState(state) == 3) {
            return 6040;
        }
        return 0;
    }

    @Override
    public String getSpecialName(ItemStack stack) {
        return combustionHeaterTypes[stack.getItemDamage()];
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos,
                                  EntityPlayer player) {
        return new ItemStack(Item.getItemFromBlock(this), 1, this.getMetaFromState(world.getBlockState(pos)));
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
                                    EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            if (getMetaFromState(state) < 2) {
                player.openGui(SkyResourcesClassic.instance, ModGuiHandler.CombustionHeaterGUI, world, pos.getX(), pos.getY(),
                        pos.getZ());
            } else {
                if (player.getHeldItemMainhand().isEmpty() && !player.isSneaking()) {
                    List<ITextComponent> toSend = new ArrayList();

                    TilePoweredCombustionHeater tile = (TilePoweredCombustionHeater) world.getTileEntity(pos);
                    toSend.add(new TextComponentString(TextFormatting.RED + "FE Stored: " + tile.getEnergyStored()
                            + " / " + tile.getMaxEnergyStored()));
                    toSend.add(new TextComponentString(TextFormatting.GOLD + "Current Heat: " + tile.currentHeatValue
                            + " / " + tile.getMaxHeat()));
                    if (tile.hasValidMultiblock())
                        toSend.add(new TextComponentString(TextFormatting.GREEN + "Valid Multiblock!"));
                    else
                        toSend.add(new TextComponentString(TextFormatting.RED + "Invalid Multiblock."));

                    for (ITextComponent text : toSend) {
                        player.sendMessage(text);
                    }

                }
            }
        }
        return true;
    }
}
