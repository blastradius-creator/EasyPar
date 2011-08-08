
import java.io.*;
import goldengine.*;

/*
 * Licensed Material - Property of Matthew Hawkins (hawkini@4email.net) 
 */
 
public class MyParser implements GPMessageConstants
{
 
    private interface SymbolConstants 
    {
       final int SYMBOL_EOF                  =  0;  // (EOF)
       final int SYMBOL_ERROR                =  1;  // (Error)
       final int SYMBOL_WHITESPACE           =  2;  // (Whitespace)
       final int SYMBOL_QUOTE                =  3;  // '"'
       final int SYMBOL_RPARAN               =  4;  // ')'
       final int SYMBOL_COMMA                =  5;  // ','
       final int SYMBOL_DOT                  =  6;  // '.'
       final int SYMBOL_SEMI                 =  7;  // ';'
       final int SYMBOL_EQ                   =  8;  // '='
       final int SYMBOL_ADDSUBPROGRAMLPARAN  =  9;  // 'addSubProgram('
       final int SYMBOL_CPU                  = 10;  // CPU
       final int SYMBOL_CPULPARAN            = 11;  // 'CPU('
       final int SYMBOL_IDENTIFIER           = 12;  // IDENTIFIER
       final int SYMBOL_NUMBER               = 13;  // NUMBER
       final int SYMBOL_PROGRAM              = 14;  // Program
       final int SYMBOL_PROGRAMLPARAN        = 15;  // 'Program('
       final int SYMBOL_RUNPROGRAMLPARAN     = 16;  // 'runProgram('
       final int SYMBOL_RUNSUBPROGRAMSLPARAN = 17;  // 'runSubPrograms('
       final int SYMBOL_CORES                = 18;  // <CORES>
       final int SYMBOL_EXPR                 = 19;  // <EXPR>
       final int SYMBOL_FILENAME             = 20;  // <FILENAME>
       final int SYMBOL_INSTRUCTIONS         = 21;  // <INSTRUCTIONS>
       final int SYMBOL_LISTITEM             = 22;  // <List Item>
       final int SYMBOL_STMT                 = 23;  // <STMT>
       final int SYMBOL_TYPE                 = 24;  // <TYPE>
    };

    private interface RuleConstants
    {
       final int RULE_INSTRUCTIONS                                                                      =  0;  // <INSTRUCTIONS> ::= <INSTRUCTIONS> <STMT>
       final int RULE_INSTRUCTIONS2                                                                     =  1;  // <INSTRUCTIONS> ::= <STMT>
       final int RULE_STMT_IDENTIFIER_EQ_SEMI                                                           =  2;  // <STMT> ::= <TYPE> IDENTIFIER '=' <EXPR> ';'
       final int RULE_STMT_ADDSUBPROGRAMLPARAN_IDENTIFIER_COMMA_IDENTIFIER_COMMA_IDENTIFIER_RPARAN_SEMI =  3;  // <STMT> ::= 'addSubProgram(' IDENTIFIER ',' IDENTIFIER ',' IDENTIFIER ')' ';'
       final int RULE_STMT_RUNPROGRAMLPARAN_IDENTIFIER_RPARAN_SEMI                                      =  4;  // <STMT> ::= 'runProgram(' IDENTIFIER ')' ';'
       final int RULE_STMT_RUNSUBPROGRAMSLPARAN_IDENTIFIER_RPARAN_SEMI                                  =  5;  // <STMT> ::= 'runSubPrograms(' IDENTIFIER ')' ';'
       final int RULE_TYPE_PROGRAM                                                                      =  6;  // <TYPE> ::= Program
       final int RULE_TYPE_CPU                                                                          =  7;  // <TYPE> ::= CPU
       final int RULE_EXPR_CPULPARAN_RPARAN                                                             =  8;  // <EXPR> ::= 'CPU(' <CORES> ')'
       final int RULE_EXPR_PROGRAMLPARAN_RPARAN                                                         =  9;  // <EXPR> ::= 'Program(' ')'
       final int RULE_EXPR_PROGRAMLPARAN_QUOTE_QUOTE_RPARAN                                             = 10;  // <EXPR> ::= 'Program(' '"' <FILENAME> '"' ')'
       final int RULE_CORES_COMMA                                                                       = 11;  // <CORES> ::= <CORES> ',' <List Item>
       final int RULE_CORES                                                                             = 12;  // <CORES> ::= <List Item>
       final int RULE_LISTITEM_NUMBER                                                                   = 13;  // <List Item> ::= NUMBER
       final int RULE_FILENAME_IDENTIFIER_DOT_IDENTIFIER                                                = 14;  // <FILENAME> ::= IDENTIFIER '.' IDENTIFIER
    };

   private static BufferedReader buffR;

    /***************************************************************
     * This class will run the engine, and needs a file called config.dat
     * in the current directory. This file should contain two lines,
     * The first should be the absolute path name to the .cgt file, the second
     * should be the source file you wish to parse.
     * @param args Array of arguments.
     ***************************************************************/
    public static void main(String[] args)
    {
       String textToParse = "", compiledGrammar = "";

       try
       {
           buffR = new BufferedReader(new FileReader(new File("./config.dat")));
           compiledGrammar = buffR.readLine();
           textToParse = buffR.readLine();

           buffR.close();
       }
       catch(FileNotFoundException fnfex)
       {
           System.out.println("Config File was not found.\n\n" +
                              "Please place it in the current directory.");
           System.exit(1);
       }
       catch(IOException ioex)
       {
          System.out.println("An error occured while reading config.dat.\n\n" +
                             "Please re-try ensuring the file can be read.");
          System.exit(1);
       }

       GOLDParser parser = new GOLDParser();

       try
       {
          parser.loadCompiledGrammar(compiledGrammar);
          parser.openFile(textToParse);
       }
       catch(ParserException parse)
       {
          System.out.println("**PARSER ERROR**\n" + parse.toString());
          System.exit(1);
       }

       boolean done = false;
       int response = -1;
       
       //my stuff
       AST.Node currentNode;
       String currentString = "";

       while(!done)
       {
          try
            {
                  response = parser.parse();
            }
            catch(ParserException parse)
            {
                System.out.println("**PARSER ERROR**\n" + parse.toString());
                System.exit(1);
            }

            switch(response)
            {
               case gpMsgTokenRead:
                   /* A token was read by the parser. The Token Object can be accessed
                      through the CurrentToken() property:  Parser.CurrentToken */
                   break;

               case gpMsgReduction:
                   /* This message is returned when a rule was reduced by the parse engine.
                      The CurrentReduction property is assigned a Reduction object
                      containing the rule and its related tokens. You can reassign this
                      property to your own customized class. If this is not the case,
                      this message can be ignored and the Reduction object will be used
                      to store the parse tree.  */

                      switch(parser.currentReduction().getParentRule().getTableIndex())
                      {
                         case RuleConstants.RULE_INSTRUCTIONS:
                            //<INSTRUCTIONS> ::= <INSTRUCTIONS> <STMT>
                            break;
                         case RuleConstants.RULE_INSTRUCTIONS2:
                            //<INSTRUCTIONS> ::= <STMT>
                        	 parser.currentReduction().getToken(0);
                            break;
                         case RuleConstants.RULE_STMT_IDENTIFIER_EQ_SEMI:
                            //<STMT> ::= <TYPE> IDENTIFIER '=' <EXPR> ';'
                            break;
                         case RuleConstants.RULE_STMT_ADDSUBPROGRAMLPARAN_IDENTIFIER_COMMA_IDENTIFIER_COMMA_IDENTIFIER_RPARAN_SEMI:
                            //<STMT> ::= 'addSubProgram(' IDENTIFIER ',' IDENTIFIER ',' IDENTIFIER ')' ';'
                            break;
                         case RuleConstants.RULE_STMT_RUNPROGRAMLPARAN_IDENTIFIER_RPARAN_SEMI:
                            //<STMT> ::= 'runProgram(' IDENTIFIER ')' ';'
                            break;
                         case RuleConstants.RULE_STMT_RUNSUBPROGRAMSLPARAN_IDENTIFIER_RPARAN_SEMI:
                            //<STMT> ::= 'runSubPrograms(' IDENTIFIER ')' ';'
                            break;
                         case RuleConstants.RULE_TYPE_PROGRAM:
                            //<TYPE> ::= Program
                            break;
                         case RuleConstants.RULE_TYPE_CPU:
                            //<TYPE> ::= CPU
                            break;
                         case RuleConstants.RULE_EXPR_CPULPARAN_RPARAN:
                            //<EXPR> ::= 'CPU(' <CORES> ')'
                        	 for(int i=0; i < parser.currentReduction().getTokenCount(); i++)
                        	 {
                        		 //System.out.println("TOKEN: " + parser.currentReduction().getToken(i) + " | " + parser.currentReduction().getToken(i).getData());
                        	 }
                            break;
                         case RuleConstants.RULE_EXPR_PROGRAMLPARAN_RPARAN:
                            //<EXPR> ::= 'Program(' ')'
                            break;
                         case RuleConstants.RULE_EXPR_PROGRAMLPARAN_QUOTE_QUOTE_RPARAN:
                            //<EXPR> ::= 'Program(' '"' <FILENAME> '"' ')'
                            break;
                         case RuleConstants.RULE_CORES_COMMA:
                            //<CORES> ::= <CORES> ',' <List Item>
                        	 System.out.println("Token: " + parser.currentToken().getData());
                        	 for(int i=0; i < parser.currentReduction().getTokenCount(); i++)
                        	 {
                        		 System.out.println("TOKEN: " + parser.currentReduction().getToken(i) + " | " + parser.currentReduction().getToken(i).getData());
                        	 }
                        	 currentString = currentString + ",";
                            break;
                         case RuleConstants.RULE_CORES:
                            //<CORES> ::= <List Item>
                        	 for(int i=0; i < parser.currentReduction().getTokenCount(); i++)
                        	 {
                        		 System.out.println("TOKEN: " + parser.currentReduction().getToken(i) + " | " + parser.currentReduction().getToken(i).getData());
                        	 }
                            break;
                         case RuleConstants.RULE_LISTITEM_NUMBER:
                            //<List Item> ::= NUMBER
                        	 /*for(int i=0; i < parser.currentReduction().getTokenCount(); i++)
                        	 {
                        		 System.out.println("TOKEN: " + parser.currentReduction().getToken(i) + " | " + parser.currentReduction().getToken(i).getData());
                        	 }*/
                        	 currentString += parser.currentReduction().getToken(0).getData();
                            break;
                         case RuleConstants.RULE_FILENAME_IDENTIFIER_DOT_IDENTIFIER:
                            //<FILENAME> ::= IDENTIFIER '.' IDENTIFIER
                            break;
                      }
                      System.out.println("Current String: " + currentString);

                          //Parser.Reduction = //Object you created to store the rule

                    // ************************************** log file
                    System.out.println("gpMsgReduction");
                    Reduction myRed = parser.currentReduction();
                    System.out.println(myRed.getParentRule().getText());
                    // ************************************** end log

                    break;

                case gpMsgAccept:
                    /* The program was accepted by the parsing engine */

                    // ************************************** log file
                    System.out.println("gpMsgAccept");
                    // ************************************** end log

                    done = true;

                    break;

                case gpMsgLexicalError:
                    /* Place code here to handle a illegal or unrecognized token
                           To recover, pop the token from the stack: Parser.PopInputToken */

                    // ************************************** log file
                    System.out.println("gpMsgLexicalError");
                    // ************************************** end log

                    parser.popInputToken();

                    break;

                case gpMsgNotLoadedError:
                    /* Load the Compiled Grammar Table file first. */

                    // ************************************** log file
                    System.out.println("gpMsgNotLoadedError");
                    // ************************************** end log

                    done = true;

                    break;

                case gpMsgSyntaxError:
                    /* This is a syntax error: the source has produced a token that was
                           not expected by the LALR State Machine. The expected tokens are stored
                           into the Tokens() list. To recover, push one of the
                              expected tokens onto the parser's input queue (the first in this case):
                           You should limit the number of times this type of recovery can take
                           place. */

                    done = true;

                    Token theTok = parser.currentToken();
                    System.out.println("Token not expected: " + (String)theTok.getData());

                    // ************************************** log file
                    System.out.println("gpMsgSyntaxError");
                    // ************************************** end log

                    break;

                case gpMsgCommentError:
                    /* The end of the input was reached while reading a comment.
                             This is caused by a comment that was not terminated */

                    // ************************************** log file
                    System.out.println("gpMsgCommentError");
                    // ************************************** end log

                    done = true;

                              break;

                case gpMsgInternalError:
                    /* Something horrid happened inside the parser. You cannot recover */

                    // ************************************** log file
                    System.out.println("gpMsgInternalError");
                    // ************************************** end log

                    done = true;

                    break;
            }
        }
        try
        {
              parser.closeFile();
        }
        catch(ParserException parse)
        {
            System.out.println("**PARSER ERROR**\n" + parse.toString());
            System.exit(1);
        }
    }
}
