package io.github.contractautomata.care.exampleWithoutCare;

import io.github.contractautomata.catlib.automaton.label.CALabel;
import io.github.contractautomata.catlib.automaton.label.Label;
import io.github.contractautomata.catlib.automaton.label.action.Action;

import java.util.List;

/**
 * a CALabel annotated with parameter type and return value type for matching types
 * 
 * @author Davide Basile
 *
 */
public class TypedCALabelExample extends CALabel{
	private final Class<?> param;
	private final Class<?> value;

	public TypedCALabelExample(CALabel label, Class<?> param, Class<?> value) {
		super(label.getContent());
		this.param=param;
		this.value=value;
	}

	public TypedCALabelExample(List<Action> la) {
		super(la);
		this.param=null;
		this.value=null;
	}
	@Override
	public boolean match(Label<Action> label) {
		if (label instanceof TypedCALabelExample)
		{
			TypedCALabelExample typedlabel = (TypedCALabelExample) label;
			return super.match(label) && 
					typedlabel.param.isAssignableFrom(this.value) &&
					this.param.isAssignableFrom(typedlabel.value);
		}
		else
			throw new IllegalArgumentException();
	}


}
