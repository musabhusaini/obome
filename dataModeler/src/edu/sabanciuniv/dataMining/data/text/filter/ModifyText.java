package edu.sabanciuniv.dataMining.data.text.filter;

import java.util.Enumeration;
import java.util.Vector;

import com.google.common.base.Function;

import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Range;
import weka.core.Capabilities.Capability;
import weka.filters.Filter;
import weka.filters.UnsupervisedFilter;

public class ModifyText
	extends Filter
	implements UnsupervisedFilter, OptionHandler {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3936486604965757458L;

	private static Function<String,String> passThroughFunction = new Function<String,String>() {
		@Override
		public String apply(String input) {
			return input;
		}
	};
	
	protected Function<String,String> modifierFunction = ModifyText.passThroughFunction; 
	protected Range selectAttributeIndices = new Range();	
	
	public void setModifierFunction(Function<String,String> modifier) {
		if (modifier == null) {
			modifier = ModifyText.passThroughFunction;
		}
		
		this.modifierFunction = modifier;
	}
	
	public Function<String,String> getModifierFunction() {
		return this.modifierFunction;
	}
	
	public String getAttributeIndices() {
		return this.selectAttributeIndices.getRanges();
	}
	
	public void setAttributeIndices(String rangeList) {
		this.selectAttributeIndices.setRanges(rangeList);
	}
	
	public void setAttributeIndicesArray(int [] attributes) {
		setAttributeIndices(Range.indicesToRangeList(attributes));
	}
	
	private void determineSelectedRange() {
		Instances inputFormat = getInputFormat();
		
		// Calculate the default set of fields to convert
		if (this.selectAttributeIndices == null) {
			StringBuffer fields = new StringBuffer();
			for (int j = 0; j < inputFormat.numAttributes(); j++) { 
				if (inputFormat.attribute(j).type() == Attribute.STRING) {
					fields.append((j + 1) + ",");
				}
			}
			
			this.selectAttributeIndices = new Range(fields.toString());
		}
		
		this.selectAttributeIndices.setUpper(inputFormat.numAttributes() - 1);

		// Prevent the user from converting non-string fields
		StringBuffer fields = new StringBuffer();
		for (int j = 0; j < inputFormat.numAttributes(); j++) { 
			if (this.selectAttributeIndices.isInRange(j) && inputFormat.attribute(j).isString()) {
				fields.append((j + 1) + ",");
			}
		}

		this.selectAttributeIndices.setRanges(fields.toString());
		this.selectAttributeIndices.setUpper(inputFormat.numAttributes() - 1);
	}
	
	@Override
	public Capabilities getCapabilities() {
		Capabilities result = super.getCapabilities();
	    result.disableAll();
	    
	    // attributes
	    result.enableAllAttributes();
		result.enable(Capability.STRING_ATTRIBUTES);
		result.enable(Capability.MISSING_VALUES);
		
		// class
		result.enableAllClasses();
	    result.enable(Capability.STRING_CLASS);
	    result.enable(Capability.MISSING_CLASS_VALUES);
	    result.enable(Capability.NO_CLASS);
	    
	    return result;
    }
	
	@Override
	public boolean setInputFormat(Instances instanceInfo) throws Exception {
		super.setInputFormat(instanceInfo);
		setOutputFormat(instanceInfo);
		return true;
	}
	
	@Override
	public boolean input(Instance instance) throws Exception {
	    if (this.getInputFormat() == null) {
	    	throw new IllegalStateException("No input instance format defined");
	    }
	    
	    if (this.m_NewBatch) {
			this.resetQueue();
			this.m_NewBatch = false;
	    }

	    Instance inst = (Instance) instance.copy();

	    this.determineSelectedRange();
	    int[] range = this.selectAttributeIndices.getSelection();
	    for (int i=0; i<range.length; i++) {
	    	if (this.getInputFormat().classIndex() == range[i]) {
	    		continue;
	    	}
	    	
	    	if (!this.getInputFormat().attribute(range[i]).isString()) {
	    		continue;
	    	}
	    	
	    	String modified = this.modifierFunction.apply(inst.stringValue(range[i]));
	    	inst.setValue(range[i], modified);
	    }
	    	    
	    push(inst);
	    return true;
	}  

	@Override
	public Enumeration<?> listOptions() {
		return new Vector<Option>().elements();
	}

	@Override
	public void setOptions(String[] options) throws Exception {
	}

	@Override
	public String[] getOptions() {
		return new String[]{};
	}
}