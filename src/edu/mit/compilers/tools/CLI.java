package edu.mit.compilers.tools;

import java.util.Vector;

/**
 * A generic command-line interface for 6.035 compilers.  This class
 * provides command-line parsing for student projects.  It recognizes
 * the required <tt>-target</tt>, <tt>-debug</tt>, <tt>-opt</tt>, and
 * <tt>-o</tt> switches, and generates a name for input and output
 * files.
 *
 * @author  6.035 Staff (<tt>6.035-staff@mit.edu</tt>)
 */
public class CLI {

  public static void printUsage(String message) {
    System.err.println(message);
    System.err.println("Usage: run.sh [options] <filename>\n" +
"Summary of options:\n" +
"  -t <stage>              --target=<stage>           compile to the given stage\n" +
"  -o <outfile>            --output=<outfile>         write output to <outfile>\n" +
"  -O <(opt|-opt|all)...>  --opt=<(opt|-opt|all)...>  perform the listed optimizations\n" +
"  -d                      --debug                    print debugging information\n" +
"\n" +
"Long description of options:\n" +
"  -t <stage>          <stage> is one of \"scan\", \"parse\", \"inter\", or \"assembly\".\n" +
"  --target=<stage>    Compilation will proceed to the given stage and halt there.\n" +
"\n" +
"  -d                  Print debugging information.  If this option is not given,\n" +
"  --debug             then there will be no output to the screen on successful\n" +
"                      compilation.\n" +
"\n" +
"  -O <optspec>        Perform the listed optimizations.  <optspec> is a comma-\n" +
"  --opt=<optspec>     separated list of optimization names, or the special symbol\n" +
"                      \"all\", meaning all possible optimizations.  You may\n" +
"                      explicitly disable an optimization by prefixing its name\n" +
"                      with '-'.\n" +
"\n" +
"  -o <outfile>        Write output to <outfile>.  If this option is not given,\n" +
"  --output=<outfile>  output will be written to a file with the same base name as\n" +
"                      the input file and the extension changed according to the\n" +
"                      final stage executed.\n");
  }

  /**
   * DEFAULT: produce default output.
   * SCAN: scan the input and stop.
   * PARSE: scan and parse input, and stop.
   * INTER: produce a high-level intermediate representation from the input,
   *        and stop. This is not one of the segment targets for Fall 2006,
   *        but you may wish to use it for your own purposes.
   * ASSEMBLY: produce assembly from the input.
   */
    public enum Action {DEFAULT, ABOUT, SCAN, PARSE, INTER, ASSEMBLY};

  /**
   * Array indicating which optimizations should be performed.  If
   * a particular element is true, it indicates that the optimization
   * named in the optnames[] parameter to parse with the same index
   * should be performed.
   */
  public static boolean opts[];

  /**
   * Vector of String containing the command-line arguments which could
   * not otherwise be parsed.
   */
  public static Vector<String> extras;

  /**
   * Name of the file to put the output in.
   */
  public static String outfile;

  /**
   * Name of the file to get input from.  This is null if the user didn't
   * provide a file name.
   */
  public static String infile;

  /**
   * The target stage.  This should be one of the integer constants
   * defined elsewhere in this package.
   */
  public static Action target;

  /**
   * The debug flag.  This is true if <tt>-debug</tt> was passed on
   * the command line, requesting debugging output.
   */
  public static boolean debug;

  /**
   * Sets up default values for all of the
   * result fields.  Specifically, sets the input and output files
   * to null, the target to DEFAULT, and the extra array to a new
   * empty Vector.
   */
  static {
    outfile = null;
    infile = null;
    target = Action.DEFAULT;
    extras = new Vector<String>();
  }

  /**
   * Parse the command-line arguments.  Sets all of the result fields
   * accordingly. <BR>
   *
   * <TT>-t / --target= <I>target</I></TT> sets the CLI.target field based
   * on the <I>target</I> specified. <BR>
   * <TT>scan</TT> or <TT>scanner</TT> specifies Action.SCAN
   * <TT>parse</TT> specifies Action.PARSE
   * <TT>inter</TT> specifies Action.INTER
   * <TT>assembly</TT> or <TT>codegen</TT> specifies Action.ASSEMBLY
   *
   * The boolean array opts[] indicates which, if any, of the
   * optimizations in optnames[] should be performed; these arrays
   * are in the same order.
   *
   * @param args Array of arguments passed in to the program's Main
   *   function.
   * @param optnames Ordered array of recognized optimization names.  */
  public static void parse(String args[], String optnames[]) {
    String ext = ".out";
    String targetStr = "";

    opts = new boolean[optnames.length];

    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("--debug") || args[i].equals("-d")) {
        debug = true;
      } else if (args[i].startsWith("--outfile=")) {
          outfile = args[i].substring(10);
      } else if (args[i].equals("-o")) {
        if (i < (args.length - 1)) {
          outfile = args[i + 1];
          i++;
        } else {
          printUsage("No output file specified with option " + args[i]);
          throw new IllegalArgumentException("Incomplete option " + args[i]);
        }
      } else if (args[i].startsWith("--target=")) {
        targetStr = args[i].substring(9);
      } else if (args[i].equals("-t")) {
        if (i < (args.length - 1)) {
          targetStr = args[i + 1];
          i++;
        } else {
          printUsage("No target specified with option " + args[i]);
          throw new IllegalArgumentException("Incomplete option " + args[i]);
        }
      } else if (args[i].startsWith("--opt=") || args[i].equals("-O")) {
        String optsList[];
        if (args[i].equals("-O")) {
          if (i < (args.length - 1)) {
            optsList = args[i + 1].split(",");
            i++;
          } else {
            printUsage("No optimizations spceified with option " + args[i]);
            throw new IllegalArgumentException("Incomplete option " + args[i]);
          }
        } else {
          optsList = args[i].substring(6).split(",");
        }
        for (int j = 0; j < optsList.length; j++) {
          if (optsList[j].equals("all")) {
            for (int k = 0; k < opts.length; k++) {
              opts[k] = true;
            }
          } else {
            for (int k = 0; k < optnames.length; k++) {
              if (optsList[j].equals(optnames[k])) {
                opts[j] = true;
              } else if (optsList[j].charAt(0) == '-' || 
                         optsList[j].substring(1).equals(optnames[k])) {
                opts[j] = false;
              }
            }
          }
        }
      } else {
        extras.addElement(args[i]);
      }
    }

    if (!targetStr.equals("")) {
      targetStr = targetStr.toLowerCase();
      if (targetStr.equals("scan")) target = Action.SCAN;
      else if (targetStr.equals("parse")) target = Action.PARSE;
      else if (targetStr.equals("inter")) target = Action.INTER;
      else if (targetStr.equals("assembly")) target = Action.ASSEMBLY;
      else if (targetStr.equals("about")) {
	  printUsage("Test run successful. Command line parameters: ");
	  System.exit(0);
      }
 
      else {
        printUsage("Invalid target: " + targetStr);
        throw new IllegalArgumentException(targetStr);
      }
    }
  
    // grab infile and lose extra args
    int i = 0;
    while (infile == null && i < extras.size()) {
      String fn = (String) extras.elementAt(i);
      if (fn.charAt(0) != '-') {
        infile = fn;
        extras.removeElementAt(i);
      }
      i++;
    }

  }
}
