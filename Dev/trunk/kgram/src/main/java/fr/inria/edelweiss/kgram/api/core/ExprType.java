package fr.inria.edelweiss.kgram.api.core;

public interface ExprType {
	
	public static int UNDEF = -1;

	// abstract type
	public static int ANY 	= 1;
	public static int ALTER	= 2;
	
	// type
	public static int CONSTANT 	= 6;
	public static int VARIABLE 	= 7;
	public static int BOOLEAN 	= 8;
	public static int TERM 		= 9;
	public static int FUNCTION 	= 10;
	

	
	// boolean
	public static int AND = 11;
	public static int OR = 12;
	public static int NOT = 13;
	
	// function
	public static int BOUND = 14;
	public static int COUNT = 15;
	// ?x in (1, 2)
	public static int LIST = 16;

	public static int ISNUMERIC = 19;
	public static int MIN = 20;
	public static int MAX = 21;
	public static int AVG = 22;
	public static int SUM = 23;
	public static int ISURI = 24;
	public static int ISBLANK = 25;
	public static int ISLITERAL = 26;
	public static int LANG = 27;
	public static int LANGMATCH = 28;
	public static int REGEX = 29;

	public static int DATATYPE = 30;
	public static int CAST = 31;
	public static int SELF = 32;
	public static int DEBUG = 33;
	public static int EXTERNAL = 34;
	public static int EXTERN = 35;
	public static int KGRAM = 36;
	public static int SQL = 37;
	public static int XPATH = 38;
	public static int SKIP = 39;
	
	public static int LENGTH = 40;
	public static int UNNEST = 41;
	public static int EXIST = 42;
	public static int STRDT = 43;
	public static int STRLANG = 44;
	public static int BNODE = 45;
	public static int COALESCE = 46;
	public static int IF = 47;
	public static int SYSTEM = 48;
	public static int GROUPCONCAT = 49;
	public static int SAMPLE = 50;
	
	public static int STRLEN 	= 51;
	public static int SUBSTR 	= 52;
	public static int UCASE 	= 53;
	public static int LCASE 	= 54;
	public static int ENDS 		= 55;
	public static int STARTS 	= 56;
	public static int CONTAINS 	= 57;
	public static int ENCODE 	= 58;
	public static int CONCAT 	= 59; 
	
	public static int YEAR 		= 60; 
	public static int MONTH 	= 61; 
	public static int DAY 		= 62; 
	public static int HOURS 	= 63;
	public static int MINUTES 	= 64;
	public static int SECONDS 	= 65;
	public static int TIMEZONE 	= 66;
	public static int NOW 		= 67;
	
	public static int ABS 		= 68;
	public static int FLOOR 	= 69;
	public static int ROUND 	= 70;
	public static int CEILING 	= 71;
	public static int RANDOM 	= 72;

	public static int HASH 		= 73;
	public static int URI 		= 74;
	public static int TZ 		= 75;
	public static int STR 		= 76;

	public static int STRBEFORE  = 77;
	public static int STRAFTER 	 = 78;
	public static int STRREPLACE = 79;
	public static int FUUID 	 = 80;
	public static int STRUUID 	 = 81;
	public static int XSDSTRING 	 = 82;



	
	// term
	public static int TEQ 	= 101;
	public static int TNEQ 	= 102;
	public static int TLE 	= 103;
	public static int TGE 	= 104;
	public static int TLT 	= 105;
	public static int TGT 	= 106;

	public static int EQNE 	= 109;
	
	public static int EQ 	= 110;
	public static int NE 	= 111;
	public static int NEQ 	= 111;
	
	public static int GL    = 112;
	public static int LE 	= 113;
	public static int GE 	= 114;
	public static int LT 	= 115;
	public static int GT 	= 116;
	
	public static int PLUS 	= 117;
	public static int MINUS = 118;
	public static int MULT 	= 119;
	
	public static int DIV 	= 120;
	
	public static int CONT 	= 121; // ~
	public static int START = 122; // ^
	public static int IN 	= 123; 
	
	
	// extension
	
	public static int DISPLAY 	= 200;
	public static int NUMBER  	= 201;
	public static int SIM 	  	= 202;
	public static int EXTEQUAL	= 203;
	public static int EXTCONT   = 204;
	public static int PROCESS   = 205;
	public static int ENV   	= 206;
	public static int DEPTH 	= 207;
	public static int GRAPH 	= 208;
	public static int NODE 		= 209;
	public static int GET 		= 210;
	public static int SET 		= 211;
	public static int LOAD 		= 212;
	public static int PATHNODE 	= 213;
	public static int GROUPBY 	= 214;
	public static int PSIM 		= 215;
	public static int GETP		= 216;
	public static int SETP 		= 217;
	public static int PWEIGHT 	= 218;
	public static int ANCESTOR 	= 219;
		
	

	public static int TURTLE 	= 227;
	public static int LEVEL 	= 228;
	public static int INDENT 	= 229;
	public static int PPURI 	= 230;
	public static int URILITERAL 	= 231;
	public static int VISITED 	= 232;
	public static int AGGAND 	= 233;
	public static int PROLOG 	= 234;
	public static int WRITE 	= 235;
	public static int FOCUS_NODE 	= 236;
	public static int XSDLITERAL 	= 237;
	public static int QNAME 	= 238;
        
	public static int STL_DEFAULT 	= 239;
	public static int STL_DEFINE 	= 240;
	public static int STL_NL 	= 241;
	public static int STL_PREFIX 	= 242;
	public static int STL_AGGREGATE = 243;
	public static int STL_CONCAT    = 244;
	public static int STL_GROUPCONCAT= 245;
	public static int STL_AND       = 246;
	public static int STL_NUMBER    = 247;
	public static int STL_LOAD      = 248;
	public static int STL_IMPORT    = 249;
	public static int STL_PROCESS 	= 250;
        
        
        public static int APPLY_TEMPLATES           = 251;
	public static int APPLY_TEMPLATES_WITH      = 252;
	public static int APPLY_TEMPLATES_ALL       = 253;
	public static int APPLY_TEMPLATES_WITH_ALL  = 254;
	public static int APPLY_TEMPLATES_GRAPH     = 255;
	public static int APPLY_TEMPLATES_WITH_GRAPH= 256;
        public static int APPLY_TEMPLATES_NOGRAPH   = 257;
	public static int APPLY_TEMPLATES_WITH_NOGRAPH= 258;        
	public static int CALL_TEMPLATE             = 259;
	public static int CALL_TEMPLATE_WITH        = 260;


	public static int ISSKOLEM 	= 270;
	public static int SKOLEM 	= 271;


}
