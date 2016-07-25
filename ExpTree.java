package test;

import java.util.LinkedList;
import java.util.Queue;

public class ExpTree {

	static int curExpStrPos=0; //current character we are looking at in the exp string
	static int expLen;

	public static void main(String[] args) {
		//String exp = "y!='Please Specify' and ((x!='' and family='Ordeing') or x_what='WHAT123')";
		String exp = "(x!='' and z='1') or (x!='' and y='2')";
		//String exp = "x!='' or y!='' or (z!='' and k='6') or (w!='' and a='2')";

		System.out.println("Expression:\n\t"+exp);
		System.out.print("Creating Tree..\n");
		Exp root = createTree(exp);
		System.out.println("Done.");
		//System.out.println("Non-Optimized tree:");
		////printExpressionTree(root);
		System.out.print("Removing redundant logical nodes from tree...");
		removeRedundantLogicalNodes(root);
		System.out.println("Done.");
		//System.out.println("Done.\nOptimized tree:");
		//printExpressionTree(root);
		
		if(root.type.equals(Exp.Type.DEPENDENT_TYPE)){ //base case. e,g expression: y!=''
			root.depMap.put(root.lValue,"true");
		}else if (root.type.equals(Exp.Type.LOGICAL_TYPE)){
			findDep(root);
		}
		
		System.out.println("Generating XML ...");
		generateXMLcontent(root,0);
		System.out.println("\nPrinting Dependencies:");
		if(root.depMap.isEmpty()) System.out.println("NO DEPENDENCIES");
		else
			for(String key : root.depMap.keySet()){
				if(root.depMap.get(key).equals("")){
					root.depMap.put(key, "true");
				}
				System.out.println(key + "\t<-->\t" + root.depMap.get(key));
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
				node.hasConditions=true;
				if(node.expCondStr.equals("")){
					node.expCondStr = child.value;
				}else{
					node.expCondStr += " " + node.op + " " + child.value; 
				}
			}
			else if(child.type.equals(Exp.Type.DEPENDENT_TYPE)){
				node.hasDependents=true;
				node.depMap.put(child.lValue, "");
			}
			else{
				findDep(child);
			}
		}


		//now that all conditions have been traversed, update node condition
		if(!node.expCondStr.equals("") && !node.expCondStr.endsWith(")"))
			node.expCondStr = "(" + node.expCondStr + ")";

		//update each dependency in current node and parent node
		for(String depKey : node.depMap.keySet()){
			updateCondforDep(depKey, node);
			if(node.parent != null){
				updateCondforDepParent(depKey, node.depMap.get(depKey), node.parent);
			}
		}

		//update condition of parent
		if	(	node.type.equals(Exp.Type.LOGICAL_TYPE) && 
				!node.expCondStr.equals("") 			&&
				node.depMap.size()==0 					&&
				node.parent != null 					&& 
				node.parent.op.equals(OP.AND)
			)
		{
			if(node.parent.expCondStr.equals("")){
				node.parent.expCondStr = node.expCondStr;
			}else{
				node.parent.expCondStr = "(" + node.parent.expCondStr + ") " + node.parent.op + " " + node.expCondStr ;
			}
		}
	}

	private static void updateCondforDepParent(String depKey,String curCond, Exp parent) {
		if(parent.depMap.containsKey(depKey)){ 
			parent.depMap.put(depKey,  parent.depMap.get(depKey) + " " + parent.op + " " + curCond); 
		}else{
			parent.depMap.put(depKey, curCond);
		}
	}

	private static void updateCondforDep(String depKey, Exp node){
		if(!node.expCondStr.equals("") && node.op.equals(OP.AND)){ 
			if(node.depMap.containsKey(depKey) && !node.depMap.get(depKey).equals("")){
				node.depMap.put(depKey, "(" + node.depMap.get(depKey) + ") " + node.op + " "+ node.expCondStr);
			}else{
				node.depMap.put(depKey, node.expCondStr );
			}
		}
		else if(node.op.equals(OP.OR) && (node.hasConditions || node.hasDependents)){
			String cond_value = "not(";
			if(node.hasChild(depKey)){
				for(Exp child : node.children){
					if(child.type.equals(Exp.Type.LOGICAL_TYPE) || !child.lValue.equals(depKey)){
						cond_value += child.value + " " + node.op + " " ; //child.value should bring the full expression of the node
					}
				}
			}else{ //depKey came from deeper nodes
				for(Exp child : node.children){
					if(!child.type.equals(Exp.Type.LOGICAL_TYPE))
						cond_value += child.value + " " + node.op + " " ; //child.value should bring the full expression of the node
				}
			}
			cond_value=cond_value.substring(0,cond_value.length()-node.op.length()-2)+")"; //remove last unnecessary 'node.op' from cond_value and close with ')'
			if(!node.depMap.get(depKey).equals("")) 
				cond_value = cond_value + " " + "and" + " " + node.depMap.get(depKey);
			node.depMap.put(depKey,cond_value);
		}
	}

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
			System.out.println("<"+node.type + " value=\"" + node.value +"\"></"+node.type+">"); 
		}
	}

	private static Exp createTree(String exp) {
		
		boolean found_NOT_or_OPENPAR;
		int notLvl = 0;
		
		expLen = exp.length();
		
		Exp curNode = null ;//= root; //current node working on
		
		while(curExpStrPos<expLen){
			do{
				found_NOT_or_OPENPAR = false;
				skipWhiteSpaces(exp);
				//get NOT , NOT(
				if(exp.startsWith("not ", curExpStrPos) || exp.startsWith("not(", curExpStrPos)){
					found_NOT_or_OPENPAR = true;
					notLvl++;
					curExpStrPos+="not".length();
				}
				//skip ' ' , '\t'
				skipWhiteSpaces(exp);
				
				// '('
				while(curExpStrPos<expLen && exp.charAt(curExpStrPos)=='('){
					found_NOT_or_OPENPAR= true;
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
			}while(found_NOT_or_OPENPAR);
			
			if(curExpStrPos+1>=expLen) break; //end of expression
			//get left value, op ('=', '!=') , right value
			int opIndex = getOpIndex(exp);
			String lv = getLeftValue(exp, opIndex).trim(); 	//remove spaces at end. was without trim()
			//System.out.println("lv: " + lv);
			//System.out.println("opIndex: " + opIndex + ", exp.charAt("+opIndex+")="+exp.charAt(opIndex));
			String op = getOp(exp, notLvl).trim();					//was without trim()
			//System.out.println("op: " + op);
			String rv = getRightValue(exp).trim();			//was without trim()
			//System.out.println("rv: " + rv);
			System.out.println("lv: " + lv +"\n" + "op: " + op + "\n" + "rv: " + rv);

			//set new node
			Exp newNode = createNode(rv,lv,op,curNode);
			curNode = newNode;
			if(curExpStrPos>=expLen) break;

			//check for ')'
			skipWhiteSpaces(exp);
			while(curExpStrPos<expLen && exp.charAt(curExpStrPos)==')'){
				if(notLvl>0){
					notLvl--;
				}
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
			System.out.println("logicalOp: " + logicalOp);

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

	private static Exp createNode(String rv, String lv, String op, Exp curNode) {
		Exp newNode = new Exp();
		
		//put in lv the var, and in rv the value
		if(		lv.startsWith("\'") 						|| 
				(lv.charAt(0)>='0' && lv.charAt(0)<='9') 	||
				lv.equalsIgnoreCase("null")					
				){
			String temp = new String(lv);
			lv = rv;
			rv = temp;
		}
		
		newNode.lValue = lv; newNode.op=op; newNode.rValue = rv;
		//System.out.println("lv: " + lv + ", op: " + op + " ,rv: " + rv);
		//System.out.println("lv: " + lv +"\n" + "op: " + op + "\n" + "rv: " + rv);
		newNode.value = lv+" " + op+ " "+rv;
		newNode.type = getExpType(newNode.rValue, newNode.lValue, newNode.op);
		//System.out.println("Type of: " + newNode.value + " is " + newNode.type);
		newNode.parent = curNode;
		if(curNode!=null)
			curNode.children.add(newNode);
		return newNode;
	}

	private static String getLogicalOp(String exp) {
		skipWhiteSpaces(exp);

		if(exp.startsWith(OP.AND+" ", curExpStrPos) || exp.startsWith(OP.AND+"(", curExpStrPos)) {
			curExpStrPos+=OP.AND.length() ;return OP.AND;
		}
		if(exp.startsWith(OP.OR+" ", curExpStrPos) || exp.startsWith(OP.OR+"(", curExpStrPos)) {
			curExpStrPos+=OP.OR.length(); return OP.OR;
		}

		return null; //case of no logical op
	}

	private static String getRightValue(String exp) {
		skipWhiteSpaces(exp);
		int ind_WhiteSpace;
		int ind_openPar,ind_ClosePar;
		int endPos = 0 ;
		//what if starts with '?
		if(exp.charAt(curExpStrPos)=='\''){
			endPos = exp.indexOf('\'', curExpStrPos+1); //find second ' index of 'right-value'
		}else{
			ind_ClosePar = exp.indexOf(')', curExpStrPos);
			ind_WhiteSpace = exp.indexOf(' ', curExpStrPos);
			if(ind_WhiteSpace==-1 && ind_ClosePar==-1){ //last "VAR OP VAL" in expression, without ')' or ' ' to follow. example: z='3' or g=4
				endPos = exp.length()-1;
			}
			else if(ind_WhiteSpace!=-1 && (ind_WhiteSpace<ind_ClosePar || ind_ClosePar==-1)){
				endPos = ind_WhiteSpace;
			}else{
				//')' before whitespace
				//check if '(' appears
				ind_openPar = exp.indexOf('(', curExpStrPos); 
				if(ind_openPar!=-1 && ind_openPar<ind_ClosePar){
					int counter = numOfOccurs(exp, ind_ClosePar, '(');
					int lastCloseParIndex = curExpStrPos;
					while(counter>0){
						if(exp.charAt(lastCloseParIndex++)==')') counter--;
					}
					endPos = lastCloseParIndex-1;
				}else{
					endPos = ind_ClosePar-1;
				}
			}
		}
		
		String rightValue;
		//System.out.println("expLen: " + expLen +" ,endPos: " + endPos);
		rightValue = exp.substring(curExpStrPos, endPos+1);
		if(endPos+1==exp.length()){
			curExpStrPos = endPos;
		}else{
			curExpStrPos = endPos+1;
		}
		return rightValue;
	}

	private static int numOfOccurs(String exp, int endIndex, char c) {
		
		int counter = 0;
		for( int i=curExpStrPos; i<endIndex; i++ ) {
		    if( exp.charAt(i) == c ) {
		        counter++;
		    } 
		}
		return counter;
	}

	private static String getOp(String exp, int notLvl) {
		skipWhiteSpaces(exp);

		if(exp.startsWith(OP.NEQ, curExpStrPos)){
			curExpStrPos+=OP.NEQ.length();
			if(notLvl%2==1){
				return OP.EQ;
			}else{
				return OP.NEQ;
			}
		}
		else if(exp.startsWith(OP.EQ, curExpStrPos)){
			curExpStrPos+=OP.EQ.length();
			if(notLvl%2==1){
				return OP.NEQ;
			}else{
				return OP.EQ;
			}
		}
		else if(exp.startsWith(OP.DIFF, curExpStrPos)){
			curExpStrPos+=OP.DIFF.length();
			if(notLvl%2==1){
				return OP.EQ;
			}else{
				return OP.DIFF;
			}
		}
		else if(exp.startsWith(OP.GTE, curExpStrPos)){
			curExpStrPos+=OP.GTE.length();
			if(notLvl%2==1){
				return OP.LT;
			}else{
				return OP.GTE;
			}
		}
		else if(exp.startsWith(OP.GT, curExpStrPos)){
			curExpStrPos+=OP.GT.length();
			if(notLvl%2==1){
				return OP.LTE;
			}else{
				return OP.GT;
			}
		}
		else if(exp.startsWith(OP.LTE, curExpStrPos)){
			curExpStrPos+=OP.LTE.length();
			if(notLvl%2==1){
				return OP.GT;
			}else{
				return OP.LTE;
			}
		}
		else if(exp.startsWith(OP.LT, curExpStrPos)){
			curExpStrPos+=OP.LT.length();
			if(notLvl%2==1){
				return OP.GTE;
			}else{
				return OP.LT;
			}
		}
		else if(exp.startsWith(OP.CONTAINS, curExpStrPos)){
			curExpStrPos+=OP.CONTAINS.length();
			if(notLvl%2==1){
				return "not " + OP.CONTAINS;
			}else{
				return OP.CONTAINS;
			}
		}

		// should never reach here
		return null;
	}

	private static String getLeftValue(String exp, int opIndex) {
		skipWhiteSpaces(exp);
		String leftValue = exp.substring(curExpStrPos, opIndex);
		curExpStrPos = opIndex;
		return leftValue;
	}
	
	public static int getOpIndex(String exp){
		
		
		int[] opIndOptions =	{	exp.indexOf(OP.EQ, curExpStrPos), 
									exp.indexOf(OP.GT, curExpStrPos), 
									exp.indexOf(OP.LT, curExpStrPos),
									exp.indexOf(OP.CONTAINS, curExpStrPos)
								};
		
		int min = Integer.MAX_VALUE;
		for(int index : opIndOptions){
			if(index != -1 && index<min){
				min=index;
			}
		}
		
		if(exp.charAt(min-1)=='!' || exp.charAt(min-1)=='<' || exp.charAt(min-1)=='>'){
			min--;
		}
		
		return min;
	}
	
	private static void skipWhiteSpaces(String exp) {
		//space character
		while(exp.charAt(curExpStrPos)==' ' || exp.charAt(curExpStrPos)=='\t'){
			//System.out.println("skipped whitepace at: " + curExpStrPos);
			curExpStrPos++;
			if(curExpStrPos>=expLen) break;
		}
	}

	private static Exp.Type getExpType(String rValue, String lValue, String op){
		if	(	
					(
							op.equalsIgnoreCase(OP.NEQ) || 
							op.equalsIgnoreCase(OP.DIFF)
					 ) 
				&&
					(
						rValue.equalsIgnoreCase("''") 				|| 
						rValue.equalsIgnoreCase("'Please Specify'") || 
						rValue.equalsIgnoreCase("null") 			||
						lValue.equalsIgnoreCase("''") 				|| 
						lValue.equalsIgnoreCase("'Please Specify'") || 
						lValue.equalsIgnoreCase("null")
					)
			) 
			return Exp.Type.DEPENDENT_TYPE;
		else 
			return Exp.Type.CONDITION_TYPE;
	}
}

/*********************************************************
 * To do list:
 * 1. allow left side of exp to be value and not variable - DONE
 * 2. get expression of LOGICAL node - PENDING
 */
