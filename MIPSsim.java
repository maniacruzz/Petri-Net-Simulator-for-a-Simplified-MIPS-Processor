/**
 * 
 */
/* On my honor, I have neither given nor received unauthorized aid on this assignment */
/**
 * @author Amogh Rao
 * Course : Embedded Systems
 * University of Florida
 * UFID: 13118639
 */
import java.util.HashMap;
import java.util.Queue;
import java.util.LinkedList;
import java.util.TreeSet;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

class Di{
    public String address;
    public String value;
    
	public Di(String address, String value) {
		this.address = address;
		this.value = value;
	}
	
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("<").append(address).append(",").append(value).append(">");
        return str.toString();
    }
}

class Ii{
    public String opcode;
    public String destReg;
    public String sourceOp1;
    public String sourceOp2;
    
	public Ii(String opcode, String destReg, String sourceOp1, String sourceOp2) {
		this.opcode = opcode;
		this.destReg = destReg;
		this.sourceOp1 = sourceOp1;
		this.sourceOp2 = sourceOp2;
	}
    
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("<").append(opcode).append(",").append(destReg).append(",").append(sourceOp1).append(",").append(sourceOp2).append(">");
        return str.toString();
    }
    
}

class Xi{
    public String regname;
    public String regval;
    
	public Xi(String regname, String regval) {
		this.regname = regname;
		this.regval = regval;
	}
	
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("<").append(regname).append(",").append(regval).append(">");
        return str.toString();
    }
    
    
}

public class MIPSsim {

	/**
	 * @param args
	 */
	
	// Initialize all data
    static Queue<Ii> INM = new LinkedList<>();
    static HashMap<String,Xi> RGF = new HashMap<String, Xi>();
    static HashMap<String,Di> DAM = new HashMap<String, Di>();
    static Queue<Ii> INB = new LinkedList<>();
    static Queue<Ii> LIB = new LinkedList<>();
    static Queue<Ii> AIB = new LinkedList<>();
    static Queue<Xi> ADB = new LinkedList<>();
    static Queue<Xi> REB = new LinkedList<>();

    
	public static void init(Queue<Ii> inst, HashMap<String,Xi> reg, HashMap<String,Di> data){

		final String instInput = "instructions.txt";
		final String regInput = "registers.txt";
		final String dataInput = "datamemory.txt";
		
	    //Init instructions
		try {
			BufferedReader br = new BufferedReader(new FileReader(instInput)); 
	        String line = br.readLine();
	        while (line != null) {
	        	
	        	String[] values = getparseInput(line);
	        	Ii instruction = new Ii(values[0], values[1], values[2], values[3]);
	        	inst.add(instruction);
	        	line = br.readLine();
	        }
	        br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Init Registers
		try {
			BufferedReader br = new BufferedReader(new FileReader(regInput)); 
	        String line = br.readLine();
	        while (line != null) {
	        	
	        	String[] values = getparseInput(line);
	        	Xi register = new Xi(values[0], values[1]);
	        	reg.put(values[0], register);
	        	line = br.readLine();
	        }
	        br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		//Init data memory
		try {
			BufferedReader br = new BufferedReader(new FileReader(dataInput)); 
	        String line = br.readLine();
	        while (line != null) {
	        	
	        	String[] values = getparseInput(line);
	        	Di datamem = new Di(values[0], values[1]);
	        	data.put(values[0], datamem);
	        	line = br.readLine();
	        }
	        br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
    public static String[] getparseInput(String line) {

    	return line.substring(line.indexOf("<") + 1, line.indexOf(">")).split(",");
    } 
	
    //Transition functions
    
    public static void write(Queue<Xi> reb, HashMap<String,Xi> rgf){
    	if (!reb.isEmpty()){
    		Xi reg=reb.remove();
    		rgf.put(reg.regname, reg);
    	}
    }
    
    public static void load(Queue<Xi> adb, HashMap<String,Di> dam, Queue<Xi> reb){
    	if (!adb.isEmpty()){
    		Xi adb_ele=adb.remove();
    		Di datamem_ele = dam.get(adb_ele.regval);
    		Xi reb_ele = new Xi(adb_ele.regname, datamem_ele.value);
    		reb.add(reb_ele);
    	}
    }
    
    public static void alu(Queue<Ii> aib , Queue<Xi> reb){
    	if (!aib.isEmpty()){
    		Ii inst = aib.remove();
    		String value=null;
    		switch (inst.opcode){
    			case "ADD" :   value = Integer.toString(Integer.parseInt(inst.sourceOp1)+Integer.parseInt(inst.sourceOp2)); 
    						   break;
    			case "SUB" :   value = Integer.toString(Integer.parseInt(inst.sourceOp1)-Integer.parseInt(inst.sourceOp2));
    				           break;
    			case "AND" :   value = Integer.toString(Integer.parseInt(inst.sourceOp1)&Integer.parseInt(inst.sourceOp2));
    				           break;
    			case "OR"  :   value = Integer.toString(Integer.parseInt(inst.sourceOp1)|Integer.parseInt(inst.sourceOp2));
    				           break;
    			default	   :   break;
    		}
    		Xi reb_ele = new Xi(inst.destReg, value);
    		reb.add(reb_ele);
    	} 
    }
    
    public static void addr(Queue<Ii> lib, Queue<Xi> adb){
    	if (!lib.isEmpty()){
    		Ii load_inst = lib.remove();
    		String address = Integer.toString(Integer.parseInt(load_inst.sourceOp1)+Integer.parseInt(load_inst.sourceOp2));
    		Xi adb_ele = new Xi(load_inst.destReg, address);
    		adb.add(adb_ele);
    	}    	
    }
    
    public static void issue(Queue<Ii> inb, Queue<Ii> lib, Queue<Ii> aib ){
    	if(!inb.isEmpty()){
    		Ii instruction = inb.remove();
    		if (instruction.opcode.equalsIgnoreCase("LD")){
        		//Issue1
    			lib.add(instruction);
    		}
    		else{
    			//Issue2
    			aib.add(instruction);    			
    		}
    	}
    }
    
    public static String read(String regname, HashMap<String,Xi> rgf){
    	String regvalue=(rgf.get(regname).regval);
    	return regvalue;
    }
    
    public static void decode(Queue<Ii> inm, Queue<Ii> inb){
    	if (!inm.isEmpty()){
    		Ii instruction = inm.remove();
    		instruction.sourceOp1 = read(instruction.sourceOp1, RGF);
    		instruction.sourceOp2 = read(instruction.sourceOp2, RGF);
    		inb.add(instruction);
    	}
    }
    
    public static void simPrint(int step, Queue<Ii> inm, Queue<Ii> inb, Queue<Ii> aib,
            Queue<Ii> lib, Queue<Xi> adb, Queue<Xi> reb, HashMap<String,Xi> rgf,
            HashMap<String,Di> dam){
		final String simOutput = "simulation.txt";
		String rgf_string="";
		String dam_string="";
		for (String key : new TreeSet<String>(rgf.keySet())){
			rgf_string+=rgf.get(key).toString()+",";
		}
		for (String key : new TreeSet<String>(dam.keySet())){
			dam_string+=dam.get(key).toString()+",";
		}		
        System.out.println("STEP " + step + ":");
        System.out.println("INM:" + inm.toString().substring(1, inm.toString().length()-1).replaceAll(" ", ""));
        System.out.println("INB:" + inb.toString().substring(1, inb.toString().length()-1).replaceAll(" ", ""));
        System.out.println("AIB:" + aib.toString().substring(1, aib.toString().length()-1).replaceAll(" ", ""));
        System.out.println("LIB:" + lib.toString().substring(1, lib.toString().length()-1).replaceAll(" ", ""));
        System.out.println("ADB:" + adb.toString().substring(1, adb.toString().length()-1).replaceAll(" ", ""));
        System.out.println("REB:" + reb.toString().substring(1, reb.toString().length()-1).replaceAll(" ", ""));
        System.out.println("RGF:" + rgf_string.substring(0, rgf_string.length()-1));
        System.out.println("DAM:" + dam_string.substring(0, dam_string.length()-1));  
        System.out.println();
    }
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		init(INM,RGF,DAM);
		String outputfile = "simulation.txt";
		
		//Print to file
        try {
            System.setOut(new PrintStream(new File(outputfile)));
        } catch (Exception e) {
        }
        
		simPrint(0, INM, INB, AIB, LIB, ADB, REB, RGF, DAM);
		int step=1;		
		while(!INM.isEmpty()||!INB.isEmpty()||!AIB.isEmpty()||!LIB.isEmpty()||!ADB.isEmpty()||!REB.isEmpty()){
			write(REB, RGF);
			load(ADB, DAM, REB);
			alu(AIB, REB);
			addr(LIB, ADB);
			issue(INB, LIB, AIB);
			decode(INM, INB);
			simPrint(step, INM, INB, AIB, LIB, ADB, REB, RGF, DAM);
			step++;
		}

	}

}
