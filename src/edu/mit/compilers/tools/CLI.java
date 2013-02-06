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

  /**
   * DEFAULT: produce default output.
   * SCAN: scan the input and stop.
   * PARSE: scan and parse input, and stop.
   * INTER: produce a high-level intermediate representation from the input,
   *        and stop. This is not one of the segment targets for Fall 2006,
   *        but you may wish to use it for your own purposes.
   * LOWIR: produce a low-level intermediate representation from input, and
   *        stop.
   * CFG: produce an image of the control flow graph
   * ASSEMBLY: produce assembly from the input.
   */
  public enum Action {DEFAULT, SCAN, PARSE, INTER, LOWIR, CFG, ASSEMBLY};

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
   * Vector of String containing the optimizations which could not be
   * parsed.  It is okay to complain about anything in this list, even
   * without the <tt>-debug</tt> flag.
   */
  public static Vector<String> extraopts;

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
   * to null, the target to DEFAULT, and the extras and extraopts
   * arrays to new empty Vectors.
   */
  static {
    outfile = null;
    infile = null;
    target = Action.DEFAULT;
    extras = new Vector<String>();
    extraopts = new Vector<String>();
  }

  /**
   * Parse the command-line arguments.  Sets all of the result fields
   * accordingly. <BR>
   *
   * <TT>-target <I>target</I></TT> sets the CLI.target field based
   * on the <I>target</I> specified. <BR>
   * <TT>scan</TT> or <TT>scanner</TT> specifies Action.SCAN
   * <TT>parse</TT> specifies Action.PARSE
   * <TT>inter</TT> specifies Action.INTER
   * <TT>lowir</TT> specifies Action.LOWIR
   * <TT>cfg</TT> specifies Action.CFG
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
    int context = 0;
    String ext = ".out";

    opts = new boolean[optnames.length];

    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-debug")) {
        context = 0;
        debug = true;
      } else if (args[i].equals("-opt")) {
        context = 1;
      } else if (args[i].equals("-o")) {
        context = 2;
      } else if (args[i].equals("-target")) {
        context = 3;
      } else if (context == 1) {
        boolean hit = false;
        for (int j = 0; j < optnames.length; j++) {
          if (args[i].equals("all") ||
              (args[i].equals(optnames[j]))) {
            hit = true;
            opts[j] = true;
          }
          if (args[i].equals("-" + optnames[j])) {
            hit = true;
            opts[j] = false;
          }
        }
        if (!hit) {
          extraopts.addElement(args[i]);
        }
      } else if (context == 2) {
        outfile = args[i];
        context = 0;
      } else if (context == 3) {
        // Process case insensitive.
        String argSansCase = args[i].toLowerCase();
        // accept "scan" and "scanner" due to handout mistake
        if (argSansCase.equals("scan") ||
            argSansCase.equals("scanner")) {
          target = Action.SCAN;
        } else if (argSansCase.equals("parse")) {
          target = Action.PARSE;
        } else if (argSansCase.equals("inter")) {
          target = Action.INTER;
        } else if (argSansCase.equals("lowir")) {
          target = Action.LOWIR;
        } else if (argSansCase.equals("assembly") ||
                   argSansCase.equals("codegen")) {
          target = Action.ASSEMBLY;
        } else {
          target = Action.DEFAULT; // Anything else is just default
        }
        context = 0;
      } else {
        extras.addElement(args[i]);
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

    // create outfile name
    switch (target) {
     case SCAN:
      ext = ".scan";
      break;
     case PARSE:
      ext = ".parse";
      break;
     case INTER:
      ext = ".ir";
      break;
     case LOWIR:
      ext = ".lowir";
      break;
     case CFG:
      ext = ".dot";
      break;
     case ASSEMBLY:
      ext = ".s";
      break;
     case DEFAULT:
     default:
      ext = ".out";
      break;
    }

    if (outfile == null && infile != null) {
      int dot = infile.lastIndexOf('.');
      int slash = infile.lastIndexOf('/');
      // Last dot comes after last slash means that the file
      // has an extention.  Note that the base case where dot
      // or slash are -1 also work.
      if (dot <= slash) {
        outfile = infile + ext;
      } else {
        outfile = infile.substring(0, dot) + ext;
      }
    }
  }
}
