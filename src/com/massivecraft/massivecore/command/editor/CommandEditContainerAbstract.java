package com.massivecraft.massivecore.command.editor;

import java.util.AbstractMap.SimpleImmutableEntry;

import org.bukkit.ChatColor;

import java.util.Collection;
import java.util.List;

import com.massivecraft.massivecore.MassiveException;
import com.massivecraft.massivecore.collections.MassiveList;
import com.massivecraft.massivecore.command.requirement.RequirementEditorPropertyCreated;
import com.massivecraft.massivecore.command.type.Type;
import com.massivecraft.massivecore.command.type.TypeNullable;
import com.massivecraft.massivecore.mson.Mson;
import com.massivecraft.massivecore.util.ContainerUtil;
import com.massivecraft.massivecore.util.Txt;

public abstract class CommandEditContainerAbstract<O, V> extends CommandEditAbstract<O, V>
{
	// -------------------------------------------- //
	// CONSTRUCT
	// -------------------------------------------- //
	
	public CommandEditContainerAbstract(EditSettings<O> settings, Property<O, V> property)
	{
		// Super
		super(settings, property, true);
		
		// Aliases
		String alias = this.createCommandAlias();
		this.setAliases(alias);
		
		// Desc
		this.setDesc(alias + " " + this.getPropertyName());
		
		// Requirements
		this.addRequirements(RequirementEditorPropertyCreated.get(true));
	}
	
	// -------------------------------------------- //
	// OVERRIDE
	// -------------------------------------------- //
	
	@Override
	public void perform() throws MassiveException
	{
		// Create
		V container = this.getProperty().getRaw(this.getObject());
		List<Object> elements = this.getValueType().getContainerElementsOrdered(container);
		
		// Alter
		try
		{
			this.alter(elements);
		}
		catch (MassiveException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new MassiveException().addMsg("<b>%s", e.getMessage());
		}
		
		// After
		elements = this.getValueType().getContainerElementsOrdered(elements);
		V after = this.getValueType().createNewInstance();
		ContainerUtil.addElements(after, elements);
		
		// Apply
		this.attemptSet(after);
	}
	
	@Override
	public String createCommandAlias()
	{
		// Split at uppercase letters
		String name = this.getClass().getSimpleName();
		name = name.substring("CommandEditContainer".length());
		final String[] words = name.split("(?=[A-Z])");
		String alias = Txt.implode(words, "");
		alias = Txt.lowerCaseFirst(alias);
		return alias;
	}
	
	// -------------------------------------------- //
	// ATTEMPT SET
	// -------------------------------------------- //
	
	@Override
	public Mson attemptSetNochangeMessage()
	{
		return mson(
			this.getProperty().getDisplayNameMson(),
			" for ",
			this.getObjectVisual(),
			" not changed."	
			).color(ChatColor.GRAY);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void attemptSetPerform(V after)
	{
		V before = this.getInheritedValue();
		String descProperty = this.getProperty().getDisplayName();
		
		// Apply
		// We set the new property value.
		this.getProperty().setValue(this.getObject(), after);
		
		// Create messages
		List<String> messages = new MassiveList<>();
		
		messages.add(Txt.parse("%s<silver> for %s<silver> edited:", descProperty, this.getObjectVisual()));
		
		// Note: The result of getAdditions is not actually V, but the implementation doesn't care.
		Collection<Object> additions = ContainerUtil.getAdditions(before, after);
		if ( ! additions.isEmpty())
		{
			messages.add(Txt.parse("<k>Additions: %s", this.getValueType().getVisual((V) additions)));
		}
		
		// Note: The result of getDeletions is not actually V, but the implementation doesn't care.
		Collection<Object> deletions = ContainerUtil.getDeletions(before, after);
		if ( ! deletions.isEmpty())
		{
			messages.add(Txt.parse("<k>Deletions: %s", this.getValueType().getVisual((V) deletions)));
		}
		
		message(messages);
	}
	
	// -------------------------------------------- //
	// ABSTRACT
	// -------------------------------------------- //
	
	public abstract void alter(List<Object> elements) throws MassiveException;
	
	// -------------------------------------------- //
	// PARAMETER
	// -------------------------------------------- //
	
	public boolean isCollection()
	{
		Type<V> type = this.getValueType();
		if (type.isContainerCollection()) return true;
		if (type.isContainerMap()) return false;
		throw new RuntimeException("Neither Collection nor Map.");
	}
	
	public void addParametersElement(boolean strict)
	{
		Type<Object> innerType = this.getValueInnerType();
		
		if (this.isCollection())
		{
			this.addParameter(innerType, true);
		}
		else
		{
			Type<Object> keyType = innerType.getInnerType(0);
			Type<Object> valueType = innerType.getInnerType(1);
			if (strict)
			{
				this.addParameter(keyType);
				this.addParameter(valueType);
			}
			else
			{
				this.addParameter(null, TypeNullable.get(keyType, "any", "all"), keyType.getTypeName(), "any");
				this.addParameter(null, TypeNullable.get(valueType, "any", "all"), valueType.getTypeName(), "any");
			}
		}
	}
	
	public Object readElement() throws MassiveException
	{
		if (this.isCollection())
		{
			return this.readArg();
		}
		else
		{
			Object key = this.readArg();
			Object value = this.readArg();
			return new SimpleImmutableEntry<Object, Object>(key, value);
		}
	}

}
