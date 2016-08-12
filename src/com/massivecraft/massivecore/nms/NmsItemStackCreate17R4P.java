package com.massivecraft.massivecore.nms;

import java.lang.reflect.Constructor;
import org.bukkit.inventory.ItemStack;

import com.massivecraft.massivecore.particleeffect.ReflectionUtils.PackageType;
import com.massivecraft.massivecore.util.ReflectionUtil;

public class NmsItemStackCreate17R4P extends NmsItemStackCreate
{
	// -------------------------------------------- //
	// INSTANCE & CONSTRUCT
	// -------------------------------------------- //
	
	private static NmsItemStackCreate17R4P i = new NmsItemStackCreate17R4P();
	public static NmsItemStackCreate17R4P get () { return i; }
	
	// -------------------------------------------- //
	// FIELDS
	// -------------------------------------------- //
	
	// net.minecraft.server.ItemStack
	private Class<?> classNmsItemStack;
	
	// org.bukkit.craftbukkit.inventory.CraftItemStack
	private Class<?> classCraftItemStack;
	
	// org.bukkit.craftbukkit.inventory.CraftItemStack(net.minecraft.server.ItemStack)
	private Constructor<?> constructorCraftItemStack;
	
	// -------------------------------------------- //
	// SETUP
	// -------------------------------------------- //
	
	@Override
	public void setup() throws Throwable
	{
		this.classNmsItemStack = getClassCraftItemStack();
		this.classCraftItemStack = PackageType.CRAFTBUKKIT_INVENTORY.getClass("CraftItemStack");
		this.constructorCraftItemStack = ReflectionUtil.getConstructor(this.classCraftItemStack, this.classNmsItemStack);
	}
	
	// -------------------------------------------- //
	// CREATE
	// -------------------------------------------- //
	
	@Override
	public ItemStack create()
	{
		return ReflectionUtil.invokeConstructor(this.constructorCraftItemStack, (Object)null);
	}
	
	// -------------------------------------------- //
	// UTIL
	// -------------------------------------------- //
	
	public static Class<?> getClassCraftItemStack() throws ClassNotFoundException
	{
		try {
			Class.forName("thermos.Thermos");
			return PackageType.MINECRAFT_ITEM.getClass("ItemStack");
		} catch (ClassNotFoundException e) {
			return PackageType.MINECRAFT_SERVER.getClass("ItemStack");
		}
	}
	
	public static Class<?> getClassCraftItemStackCatch()
	{
		try
		{
			return getClassCraftItemStack();
		}
		catch (Throwable t)
		{
			return null;
		}
	}
	
}
