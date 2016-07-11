package test;

import java.util.LinkedList;
import java.util.Queue;

public class ExpTree {

	static int curExpStrPos=0; //current character we are looking at in the exp string
	static int expLen;
	
	public static void main(String[] args) {
		//String exp = " (y='2' or (z='3')  or (k='6')) or j='1'"; //"x='1' and (y='2' or z='3') or g='4'"
		//String exp = "($Focus:bug_product2part_info:part_info2part_num:family = 'Ordering' or $Focus:bug_product2part_info:part_info2part_num:family = 'Billing') and (release_rev!='') and (release_rev!='Please Specify') and (((reason='Hot Fix'  or reason='Hot Fix Sub Task') and x_act_patch_bundle!='' and x_act_patch_bundle!='Please Specify' and x_fixed_patch!='') or ((reason='Patch Bundle' or reason='Patch Bundle Sub Task' or reason='Rollup' or reason='Rollup Sub Task') and x_act_patch_bundle != '' and x_act_patch_bundle != 'Please Specify') or (reason!='Hot Fix' and reason!='Patch Bundle' and reason!='Hot Fix Sub Task' and reason!='Patch Bundle Sub Task' and reason!='Rollup' and reason!='Rollup Sub Task' )) or  ($Focus:bug_product2part_info:part_info2part_num:family = 'Billing' and x_realloc_reason !='Fix Problem')or ($Focus:bug_product2part_info:part_info2part_num:family != 'Ordering' and $Focus:bug_product2part_info:part_info2part_num:family != 'Billing')";
		String exp = "	($Focus:bug_domain2gbst_elm:title!='Please Specify' and $Focus:bug_product2part_info:part_info2part_num:family!='Foundation Management' or ($Focus:bug_product2part_info:part_info2part_num:family = 'Foundation Management') )and ($Focus:bug_product2part_info:part_info2part_num:family = 'Billing' and x_fix_src_files!='' and x_instructions!=''and x_fix_tgt_files!='' and x_correction_desc!='' and x_prob_desc!='' and x_main_component_changes!='' and x_xpi_topology_changes!='' and (x_perforce_id!='' or x_perforce_id_man!='') and ((x_roll_down_ind != '' and x_roll_down_ind != 'Please Specify' and (reason!='Release Rollup' and reason!='Release Rollup Sub Task' and reason!='Rollup' and reason!='Rollup Sub Task'))or (reason='Release Rollup' or reason='Release Rollup Sub Task' or reason='Rollup' or reason='Rollup Sub Task'))or ($Focus:bug_product2part_info:part_info2part_num:family = 'Ordering' and x_fix_src_files!='' and x_instructions!=''and x_fix_tgt_files!='' and x_correction_desc!='' and x_prob_desc!='')or ($Focus:bug_product2part_info:part_info2part_num:family = 'DES' and x_fix_src_files!='' and x_instructions!=''and x_fix_tgt_files!='' and x_correction_desc!='' and x_prob_desc!='')or ($Focus:bug_product2part_info:part_info2part_num:family = 'Enterprise Product Catalogue' and x_fix_src_files!='' and x_instructions!=''and x_fix_tgt_files!='' and x_correction_desc!='' and x_prob_desc!='') or ($Focus:bug_product2part_info:part_info2part_num:family = 'Enterprise Customer Hub' and x_fix_src_files!='' and x_instructions!=''and x_fix_tgt_files!='' and x_correction_desc!='' and x_prob_desc!='') or (($Focus:bug_product2part_info:part_info2part_num:family = 'Content RM' or 	$Focus:bug_product2part_info:part_info2part_num:family = 'OSS ARM & Planning' or 	$Focus:bug_product2part_info:part_info2part_num:family = 'OSS Activation' or 	$Focus:bug_product2part_info:part_info2part_num:family = 'OSS DIM' or 	$Focus:bug_product2part_info:part_info2part_num:family = 'OSS Network Rollout Solutions' or 	$Focus:bug_product2part_info:part_info2part_num:family = 'OSS Foundation' or 	$Focus:bug_product2part_info:part_info2part_num:family = 'OSS Service Fulfillment')  and release_rev !='' and release_rev !='Please Specify' and x_act_patch_bundle != '' and x_act_patch_bundle != 'Please Specify') or $Focus:bug_product2part_info:part_info2part_num:family != 'Billing' and $Focus:bug_product2part_info:part_info2part_num:family != 'Ordering' and $Focus:bug_product2part_info:part_info2part_num:family != 'Enterprise Product Catalogue' and $Focus:bug_product2part_info:part_info2part_num:family != 'Content RM' and $Focus:bug_product2part_info:part_info2part_num:family != 'Enterprise Customer Hub' and $Focus:bug_product2part_info:part_info2part_num:family != 'DES' and $Focus:bug_product2part_info:part_info2part_num:family != 'OSS ARM & Planning' and $Focus:bug_product2part_info:part_info2part_num:family != 'OSS Activation' and $Focus:bug_product2part_info:part_info2part_num:family != 'OSS DIM' and $Focus:bug_product2part_info:part_info2part_num:family != 'OSS Network Rollout Solutions' and $Focus:bug_product2part_info:part_info2part_num:family != 'OSS Foundation' and $Focus:bug_product2part_info:part_info2part_num:family != 'OSS Service Fulfillment')";
		//String exp = "($Focus:bug_product2part_info:part_info2part_num:family = 'Ordering' or 	$Focus:bug_product2part_info:part_info2part_num:family = 'Billing') and (release_rev!='') and (release_rev!='Please Specify') ";
		
		System.out.println("Expression:\n"+exp);
		System.out.print("Creating Tree..");
		Exp root = createTree(exp);
		System.out.println("Done.");
		//System.out.println("Non-Optimized tree:");
		//printExpressionTree(root);
		System.out.print("Removing redundant logical nodes from tree...");
		removeRedundantLogicalNodes(root);
		System.out.println("Done.");
		//System.out.println("Done.\nOptimized tree:");
		//printExpressionTree(root);
		findDep(root);
		System.out.println("Generating XML ...");
		generateXMLcontent(root,0);
		System.out.println("\nPrinting Dependencies:");
		if(root.depMap.isEmpty()) System.out.println("NO DEPENDENCIES");
		else
			for(String key : root.depMap.keySet()){
				System.out.println(key + " DEPENDS ON " + root.depMap.get(key));
			}
	}

	private static void printExpressionTree(Exp root) {
		Queue<Exp> queue = new LinkedList<Exp>();
		queue.add(root);
		int level = 0;
		while(!queue.isEmpty()){
			Exp e = queue.remove();
			if(e.children.size()>0){
				System.out.println("Level " + level + ":");
				System.out.println("Children of '" + e.value + "':");
				for (Exp child : e.children){
					System.out.println("\t " + child.value);
					queue.add(child);
				}
				level++;
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static void removeRedundantLogicalNodes(Exp root) {
		if(!root.type.equals(Exp.Type.LOGICAL_TYPE)) return;
		
		Exp curNode ;
		Queue<Exp> queue = new LinkedList<Exp>();
		queue.add(root);
		
		while(!queue.isEmpty()){
			curNode = queue.remove();
			boolean foundSameLogicalChild;
			do{
				foundSameLogicalChild = false;
				for(Exp child : (LinkedList<Exp>) curNode.children.clone()){
					boolean removeChild = false;
					if(child.type.equals(Exp.Type.LOGICAL_TYPE)){
						queue.add(child);
						if(child.op.equalsIgnoreCase(curNode.op)){
							foundSameLogicalChild = true;
								for(Exp cchild : child.children){
									cchild.parent = curNode;
									curNode.children.add(cchild);
									removeChild = true;
									if(cchild.type.equals(Exp.Type.LOGICAL_TYPE)) queue.add(cchild);
							}
						}
					}
					if(removeChild) curNode.children.remove(child);
				}
			}while(foundSameLogicalChild);
		}
	}

	private static void findDep(Exp node) {
		for(Exp child : node.children){
			if(child.type.equals(Exp.Type.CONDITION_TYPE)){
				if(node.expCondStr.equals("")){
					node.expCondStr = child.value;
				}else{
					node.expCondStr += " " + node.op + " " + child.value; 
				}
			}
			else if(child.type.equals(Exp.Type.DEPENDENT_TYPE)){
				node.depMap.put(child.lValue, "");
			}
			else{
				findDep(child);
			}
		}
		
		
		if(!node.expCondStr.equals("") && !node.expCondStr.endsWith(")"))
			node.expCondStr = "(" + node.expCondStr + ")";
		
		//System.out.println("node.expCondStr = " + node.expCondStr);
		
		for(String depKey : node.depMap.keySet()){
			updateCondforDep(depKey, node);
			if(node.parent != null){
				updateCondforDepParent(depKey, node.depMap.get(depKey), node.parent);
			}
		}
		
		if(node.type.equals(Exp.Type.LOGICAL_TYPE) && node.parent != null && node.parent.op.equals(OP.AND) && !node.expCondStr.equals("")){
			if(node.parent.expCondStr.equals("")){
				node.parent.expCondStr = node.expCondStr;
			}else{
				node.parent.expCondStr = "(" + node.parent.expCondStr + ") " + node.parent.op + " " + node.expCondStr ;
			}
		}
	}
	
	
	
	private static void updateCondforDepParent(String depKey,String curCond, Exp parent) {
		if(parent.depMap.containsKey(depKey) && !parent.expCondStr.equals("")){
			parent.depMap.put(depKey, "(" + curCond + ") " + parent.op + " " + parent.expCondStr);
			//System.out.println("PUT curCond+parentOP+parentCond: " + parent.depMap.get(depKey));
		}else{
			parent.depMap.put(depKey, curCond);
			//System.out.println("PUT curCond: " + parent.depMap.get(depKey));
		}
	}

	private static void updateCondforDep(String depKey, Exp node){
		if(!node.expCondStr.equals("")){
			if(node.depMap.containsKey(depKey) && !node.depMap.get(depKey).equals("")){
				node.depMap.put(depKey, node.depMap.get(depKey) + " " + node.op + " "+ node.expCondStr);
				//System.out.println("1 : " + node.depMap.get(depKey));
			}else{
				node.depMap.put(depKey, node.expCondStr );
				//System.out.println("2 : " + node.depMap.get(depKey));
			}
		}
	}
	
	//static int numTabs = 0;
	private static void generateXMLcontent(Exp node,int numOfTabs){
		
		for(int i = 0 ; i < numOfTabs; i ++)
			System.out.print('\t');
		
		
		if(node.type.equals(Exp.Type.LOGICAL_TYPE)){
			System.out.println("<"+node.value + ">");//+ " hasDependents=\"" + node.hasDependents + "\" hasConditions=\"" + node.hasConditions +"\">");
			numOfTabs++;
			for (Exp child : node.children){
				generateXMLcontent(child,numOfTabs);
			}
			numOfTabs--;
			for(int i = 0 ; i < numOfTabs; i ++)
				System.out.print('\t');
			System.out.println("</"+node.value+">");
		}else{
			System.out.println("<"+node.type + " value=\"" + node.value +"\"></"+node.type+">"); //"'1'"
		}
	}

	private static Exp createTree(String exp) {
		
		
		expLen = exp.length();

		Exp curNode = null ;//= root; //current node working on
		
		while(curExpStrPos<expLen){
			
			//skip ' ' , '\t'
			skipWhiteSpaces(exp);
			
			// '('
			while(curExpStrPos<expLen && exp.charAt(curExpStrPos)=='('){
				//System.out.println("found '(' at: " + curExpStrPos);
				curExpStrPos++;
				
				//Create a new OPEN_PARENTHESIS node
				Exp parenthesisNode = new Exp();
				parenthesisNode.value = "(";
				parenthesisNode.type = Exp.Type.OPEN_PARENTHESIS;
				
				if(curNode!=null){
					//curNode must be AND/OR here
					curNode.children.add(parenthesisNode);
					parenthesisNode.parent = curNode;
				}
				
				curNode = parenthesisNode;
				skipWhiteSpaces(exp);
			}
				
			//get left value, op ('=', '!=') , right value
			String lv = getLeftValue(exp).trim(); 		//remove spaces at end. was without trim()
			String op = getOp(exp).trim();				//was without trim()
			String rv = getRightValue(exp).trim();		//was without trim()
			//System.out.println("lv: " + lv +"\n" + "op: " + op + "\n" + "rv: " + rv);
			
			//set new node
			Exp newNode = new Exp();
			newNode.lValue = lv; newNode.op=op; newNode.rValue = rv;
			newNode.value = lv+op+rv;
			newNode.type = getExpType(newNode.rValue, newNode.lValue);
			//System.out.println("Type of: " + newNode.value + " is " + newNode.type);
			newNode.parent = curNode;
			if(curNode!=null)
				curNode.children.add(newNode);
			curNode = newNode;
			if(curExpStrPos>=expLen) break;
			
			//check for ')'
			skipWhiteSpaces(exp);
			while(curExpStrPos<expLen && exp.charAt(curExpStrPos)==')'){
				//System.out.println("found ')' at: " + curExpStrPos);
				curExpStrPos++;
				// go to first '(' parent
				if(curNode.parent != null){
					curNode = curNode.parent;
				}
				
				while(curNode.type != Exp.Type.OPEN_PARENTHESIS){
					curNode = curNode.parent;
				}
				
				//delete node, not needed '('
				Exp logical_child = curNode.children.getFirst();
				if(curNode.parent==null){
					logical_child.parent=null;
				}else{
					Exp logical_parent = curNode.parent;
					logical_child.parent=logical_parent;
					logical_parent.children.remove(curNode);
					logical_parent.children.add(logical_child);
				}
				curNode = logical_child;
				
				if(curExpStrPos>=expLen) break;
				skipWhiteSpaces(exp);
			}
			
			//get logical op ('AND','OR')
			if(curExpStrPos>=expLen) break;	//End.Of.String - logical op doesnt have to appear..
			String logicalOp = getLogicalOp(exp);
			//System.out.println("logicalOp: " + logicalOp);
			
			if(logicalOp!=null){
				//create this Logical node
				Exp logicalNode = new Exp();
				logicalNode.type = Exp.Type.LOGICAL_TYPE;
				logicalNode.value = logicalOp;
				logicalNode.op = logicalOp;
				
				if(curNode.parent!=null){
					if(curNode.parent.type.equals(Exp.Type.LOGICAL_TYPE)){
						//same logical op
						if(logicalOp.equalsIgnoreCase(curNode.parent.op)){
							//skip. no need to do anyhing
							curNode = curNode.parent;
						}
						else{ //different logical op
							Exp stmtNode = curNode;
							curNode = curNode.parent; //curNode == it's parent logical node
							if(logicalOp.equalsIgnoreCase(OP.AND)){ //current = 'OR', logicalOp = 'AND'
								//now curNode == 'OR' node
								curNode.children.add(logicalNode);
								logicalNode.parent = curNode;
								curNode.children.remove(stmtNode);
								logicalNode.children.add(stmtNode);
								stmtNode.parent = logicalNode;
							}
							else{	//current = 'AND', logicalOp == 'OR'
								//now curNode == 'AND' node
								if(curNode.parent!=null){
									logicalNode.parent=curNode.parent;
									curNode.parent.children.add(logicalNode);
									curNode.parent.children.remove(curNode);
								}
								logicalNode.children.add(curNode);
								curNode.parent=logicalNode;
							}
							
							curNode = logicalNode;
						}
					}
					else{ //parent.type != logical (should be '(' )
						logicalNode.parent = curNode.parent;
						curNode.parent.children.add(logicalNode);
						curNode.parent.children.remove(curNode);
						logicalNode.children.add(curNode);
						curNode.parent = logicalNode;
						curNode=logicalNode;
					}
				}else{ //curNode.parent == null
					curNode.parent = logicalNode;
					logicalNode.children.add(curNode);
					curNode=logicalNode;
				}
			}
		}
		
		
		//return root node
		while(curNode.parent!=null) curNode=curNode.parent;
		return curNode;
	}

	private static String getLogicalOp(String exp) {
		skipWhiteSpaces(exp);
		
		if(exp.startsWith(OP.AND+" ", curExpStrPos)) {curExpStrPos+=OP.AND.length() ;return OP.AND;}
		if(exp.startsWith(OP.OR+" ", curExpStrPos)) {curExpStrPos+=OP.OR.length(); return OP.OR;}
		
		return null; //case of no logical op
	}

	private static String getRightValue(String exp) {
		skipWhiteSpaces(exp);
		
		//find where right value ends
		int ind_WhiteSpace = exp.indexOf('\'', curExpStrPos+1); //find second ' index of 'right-value'
		int ind_ClosePar = exp.indexOf(')', curExpStrPos);
		//int ind_Tab = exp.indexOf('\t', curExpStrPos);
		
		int endOfRightValue = ind_WhiteSpace < ind_ClosePar ? ind_WhiteSpace : ind_ClosePar;
		
		if(ind_WhiteSpace==-1 && ind_ClosePar==-1){
			endOfRightValue=exp.length();
		}else{
			if(endOfRightValue==-1){
				if(ind_WhiteSpace!=-1) endOfRightValue = ind_WhiteSpace;
				else endOfRightValue = ind_ClosePar;
			}
		}
		
		String rightValue = exp.substring(curExpStrPos, endOfRightValue+1);
		
		curExpStrPos = endOfRightValue+1;
		
		return rightValue;
	}

	private static String getOp(String exp) {
		skipWhiteSpaces(exp);
		
		if(exp.startsWith(OP.NEQ, curExpStrPos)){
			curExpStrPos+=OP.NEQ.length();
			return OP.NEQ;
		}
		else if(exp.startsWith(OP.EQ, curExpStrPos)){
			curExpStrPos+=OP.EQ.length();
			return OP.EQ;
		}
		
		// should never reach here
		return null;
	}

	private static String getLeftValue(String exp) {
		skipWhiteSpaces(exp);
		
		int endOfLefttVal = exp.indexOf('=', curExpStrPos);
		
		if(exp.charAt(endOfLefttVal-1)=='!'){
			endOfLefttVal--;
		}
		
		
		int oldStartPos = curExpStrPos;
		curExpStrPos = endOfLefttVal;
		
		return exp.substring(oldStartPos, endOfLefttVal);
	}
	
	private static void skipWhiteSpaces(String exp) {
		//space character
		while(exp.charAt(curExpStrPos)==' ' || exp.charAt(curExpStrPos)=='\t'){
			//System.out.println("skipped whitepace at: " + curExpStrPos);
			curExpStrPos++;
			if(curExpStrPos>=expLen) break;
		}
	}

	private static Exp.Type getExpType(String rValue, String lValue){
		if	(		
				rValue.equalsIgnoreCase("''") 				|| 
				rValue.equalsIgnoreCase("'Please Specify'") || 
				rValue.equalsIgnoreCase("null") 			||
				lValue.equalsIgnoreCase("''") 				|| 
				lValue.equalsIgnoreCase("'Please Specify'") || 
				lValue.equalsIgnoreCase("null")
			) 
			return Exp.Type.DEPENDENT_TYPE;
		else 
			return Exp.Type.CONDITION_TYPE;
	}
}

/***
 * To do list:
 * 1. allow left side of exp to be value and not variable
 */
