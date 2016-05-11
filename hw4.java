/* Name:

   UID:

   Others With Whom I Discussed Things:

   Other Resources I Consulted:
   
*/

// import lists and other data structures from the Java standard library
import java.util.*;

// PROBLEM 1

// a type for arithmetic expressions
interface Exp {
    double eval(); 	                       // Problem 1a
    List<Instr> compile(); 	               // Problem 1c
}

class Num implements Exp {
    protected double val;

    public Num(double val) { this.val = val; }

    //public boolean equals(Object o) { return (o instanceof Num) && ((Num)o).val == this.val; }

    public String toString() { return "" + val; }

    public double eval() { return val; }

    public List<Instr> compile() {
        List<Instr> instrs = new LinkedList<Instr>();
        instrs.add(new Push(this.val));
        return instrs;
    }
}

class BinOp implements Exp {
    protected Exp left, right;
    protected Op op;

    public BinOp(Exp left, Op op, Exp right) { 
        this.left = left;
        this.right = right;
        this.op = op;
    }
/*
    public boolean equals(Object o) {
    	if(!(o instanceof BinOp))
    		return false;
    	BinOp b = (BinOp) o;
    	return this.left.equals(b.left) && this.op.equals(b.op) &&
		    	this.right.equals(b.right);
    }
*/
    public String toString() {
		return "BinOp(" + left + ", " + op + ", " + right + ")";
    }

    public double eval() {
        return op.calculate(left.eval(), right.eval());
    }

    public List<Instr> compile() {
        List<Instr> instrs = new LinkedList<Instr>();
        instrs.addAll(left.compile());
        instrs.addAll(right.compile());
        instrs.add(new Calculate(this.op));
        return instrs;
    }
}

// a representation of four arithmetic operators
enum Op {
    PLUS { public double calculate(double a1, double a2) { return a1 + a2; } },
    MINUS { public double calculate(double a1, double a2) { return a1 - a2; } },
    TIMES { public double calculate(double a1, double a2) { return a1 * a2; } },
    DIVIDE { public double calculate(double a1, double a2) { return a1 / a2; } };

    abstract double calculate(double a1, double a2);
}

// a type for arithmetic instructions
interface Instr {
    void executeInstr(Stack<Double> st);
}


class Push implements Instr {
    protected double val;

    public Push(double val) { this.val = val; } 

    public void executeInstr(Stack<Double> st) {
        st.push(val);
    }

	//public boolean equals(Object o) { return (o instanceof Push) && ((Push)o).val == this.val; }

    public String toString() {
		return "Push " + val;
    }

}

class Calculate implements Instr {
    protected Op op;

    public Calculate(Op op) { this.op = op; }

    public void executeInstr(Stack<Double> st) {
        double op1, op2;
        if(!st.empty()){
            op2 = st.pop();
            if(!st.empty()) {
                op1 = st.pop();
                double res = op.calculate(op1, op2);
                st.push(res);
            }
        }    
    }
/*
    public boolean equals(Object o) { return (o instanceof Calculate) && 
    						  ((Calculate)o).op.equals(this.op); }
*/
    public String toString() {
		return "Calculate " + op;
    }    
}

class Instrs {
    protected List<Instr> instrs;

    public Instrs(List<Instr> instrs) { this.instrs = instrs; }

    public double execute() { // Problem 1b
        Stack<Double> myStack = new Stack<Double>();

        for(Instr instr : instrs) { 
            instr.executeInstr(myStack);
        }
        return myStack.pop();
    }  
}


class CalcTest {
    public static void main(String[] args) {
	   // a test for Problem 1a
		Exp exp =
	     	new BinOp(new BinOp(new Num(1.0), Op.PLUS, new Num(2.0)),
		    	  	  Op.TIMES,
		       	  new Num(3.0));
		assert(exp.eval() == 9.0);

        Exp exp1 = new Num(1.0);
        assert(exp1.eval() == 1.0);

        Exp exp2 = new BinOp(exp, Op.MINUS, exp1);
        assert(exp2.eval() == 8.0);

        Exp exp3 = new BinOp(new Num(2.0), Op.PLUS, new Num(3.0));
        assert(exp3.eval() == 5.0);

        Exp exp4 = new BinOp(new Num(2.0), Op.MINUS, new Num(3.0));
        assert(exp4.eval() == -1.0);

        Exp exp5 = new BinOp(new Num(2.0), Op.TIMES, new Num(3.0));
        assert(exp5.eval() == 6.0);

        Exp exp6 = new BinOp(new Num(2.0), Op.DIVIDE, new Num(3.0));
        assert(exp6.eval() == 0.66666666666666663);

        Exp exp7 = new BinOp(new BinOp(new Num(1.0), Op.PLUS, new Num(2.0)), Op.TIMES, new Num(3.0));
        assert(exp7.eval() == 9.0);

        Exp exp8 = new BinOp(new Num(3.0), Op.TIMES, new BinOp(new Num(1.0), Op.PLUS, new Num(2.0)));
        assert(exp8.eval() == 9.0);

        Exp exp9 = new BinOp(new BinOp(new Num(1.0), Op.TIMES, new Num(3.0)), Op.PLUS, new BinOp(new Num(2.0), Op.TIMES, new Num(3.0)));
        assert(exp9.eval() == 9.0);

        Exp exp10 = new BinOp(new Num(5.0), Op.TIMES, new BinOp(new Num(2.0), Op.MINUS, new Num(3.0)));
        assert(exp10.eval() == -5.0);

        Exp exp11 = new BinOp(new Num(5.0), Op.DIVIDE, new BinOp(new Num(2.0), Op.MINUS, new Num(4.0)));
        assert(exp11.eval() == -2.5);


		// a test for Problem 1b
		List<Instr> is = new LinkedList<Instr>();
		is.add(new Push(1.0));
		is.add(new Push(2.0));
		is.add(new Calculate(Op.PLUS));
		is.add(new Push(3.0));
		is.add(new Calculate(Op.TIMES));
		Instrs instrs = new Instrs(is);
		assert(instrs.execute() == 9.0);

        List<Instr> is1 = new LinkedList<Instr>();
        is1.add(new Push(1.0));
        Instrs instrs1 = new Instrs(is1);
        assert(instrs1.execute() == 1.0);

		// a test for Problem 1c
		assert(exp.compile().equals(is));
        assert(exp1.compile().equals(is1));
    }
}


// PROBLEM 2

// the type for a set of strings
interface StringSet {
    int size();
    boolean contains(String s);
    void add(String s);
}

// an implementation of StringSet using a linked list
class ListStringSet implements StringSet {
    protected SNode head;

    public ListStringSet() { this.head = new SEmpty(); }

    public int size() { return head.size(); }

    public boolean contains(String s) { return head.contains(s); }
   
    public void add(String s) { head = head.add(s); }
}

// a type for the nodes of the linked list
interface SNode {
    int size();
    boolean contains(String s);
    SNode add(String s);
}

// represents an empty node (which ends a linked list)
class SEmpty implements SNode {
    public SEmpty() {}
    public int size() { return 0; }
    public boolean contains(String s) { return false; }
    public SNode add(String s) { return new SElement(s, new SEmpty()); } 
}

// represents a non-empty node
class SElement implements SNode {
    protected String elem;
    protected SNode next;

    public SElement(String element, SNode nextNode) { 
        this.elem = element; 
        this.next = nextNode; 
    }

    public int size() { return 1 + next.size(); }

    public boolean contains(String s) { 
        if(s.compareTo(this.elem) == 0)
            return true;
        else if(s.compareTo(this.elem) < 0 )
            return false;
        else
            return this.next.contains(s);
    } 

    public SNode add(String s) {
        if(s.compareTo(this.elem) == 0)
            return this;
        else if(s.compareTo(this.elem) < 0) {
            SElement newNode = new SElement(s, this);
            return newNode;
        }
        else
            this.next = this.next.add(s);
            return this; 
    }
}

class SMain {
    public static void main(String[] args) {
        StringSet set = new ListStringSet();
        set.add("A");
        set.add("A");
        set.add("A");
        set.add("A");
        set.add("B");
        set.add("C");
        set.add("D");
        set.add("E");
        System.out.println(set.size());
        System.out.println(set.contains("D"));
        System.out.println(set.contains("Z"));
    }
}


// Problem 2b

interface Set<T> {
    int size();
    boolean contains(T value);
    void add(T value);
}

class ListSet<T> implements Set<T> {
    protected Node<T> head;
    protected Comparator<T> comparator; 

    public ListSet(Comparator<T> comp) { 
        this.head = new Empty<T>();
        this.comparator = comp; 
    }

    public int size() { return head.size(); }
    public boolean contains(T value) { return head.contains(value, comparator); }
    public void add(T value) { 
        head = head.add(value, this.comparator);
    }

}


interface Node<T> {
    int size();
    boolean contains(T value, Comparator<T> comp);
    Node<T> add(T value, Comparator<T> comp);
}

class Empty<T> implements Node<T> {
    public Empty() {}
    public int size() { return 0; }
    public boolean contains(T value, Comparator<T> comp) { return false; }
    public Node<T> add(T value, Comparator<T> comp) { return new Element<T>(value, new Empty<T>()); }
}

class Element<T> implements Node<T> {
    protected T elem;
    protected Node<T> next;

    public Element(T element, Node<T> nextNode) {
        this.elem = element;
        this.next = nextNode;
    }

    public int size() { return 1 + this.next.size(); }

    public boolean contains(T value, Comparator<T> comp) {
        if(comp.compare(value, this.elem) == 0)
            return true;
        else if(comp.compare(value, this.elem) < 0)
            return false;
        else  
            return this.next.contains(value, comp);
    }

    public Node<T> add(T value, Comparator<T> comp) {
        if(comp.compare(value, this.elem) == 0)
            return this;
        else if(comp.compare(value, this.elem) < 0) {
            Element<T> newNode = new Element<T>(value, this);
            return newNode;
        }
        else {
            this.next = this.next.add(value, comp);
            return this;
        }
    }
}


class Main {
    public static void main(String[] args) {
        Set<String> set = new ListSet<String>((String s1, String s2) -> s2.compareTo(s1));
        set.add("A");
        set.add("A");
        set.add("A");
        set.add("A");
        set.add("B");
        set.add("C");
        set.add("D");
        set.add("E");
        System.out.println(set.size());
        System.out.println(set.contains("D"));
        System.out.println(set.contains("G"));
    }
}



