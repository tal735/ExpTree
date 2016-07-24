package test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Exp {
	public Exp parent;
	public String value;
	public Type type; //'L'(ogic) , 'E'(quality)
	public String rValue, lValue;
	public String op;
	public LinkedList<Exp> children;
	public boolean hasConditions, hasDependents;
	
	public Map<String, String> depMap = new HashMap<String, String>();
	public String expCondStr = "";
	
	public static enum Type {
	    LOGICAL_TYPE, OPEN_PARENTHESIS, CONDITION_TYPE, DEPENDENT_TYPE
	}
	
	public Exp(){
		parent = null;
		value=null;
		type=null;
		lValue = rValue = op = null;
		children=new LinkedList<Exp>();
		hasConditions = hasDependents = false;
	}
	
	public boolean hasChild(String lValue){
		for (Exp child : children){
			if(!child.type.equals(Exp.Type.LOGICAL_TYPE) && child.lValue.equals(lValue)){
				return true;
			}
		}
		return false;
	}
}