package enigma;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import static enigma.TestUtils.*;
import java.util.Scanner;
import java.util.NoSuchElementException;
import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author Anastasia Sukhorebraya
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            new Main(args).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Check ARGS and open the necessary files (see comment on main). */
    Main(String[] args) {
        if (args.length < 1 || args.length > 3) {
            throw error("Too Short command line input");
        }

        _configuration = getInput(args[0]);

        if (args.length > 1) {
            _input = getInput(args[1]);
        } else {
            _input = new Scanner(System.in);
        }

        if (args.length > 2) {
            _output = getOutput(args[2]);
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named @param nAame nAame. */
    private Scanner getInput(String nAame) {
        try {
            return new Scanner(new File(nAame));
        } catch (IOException excp) {
            throw error("can't open %s", nAame);
        }
    }

    /** Return a PrintStream writing to the file named @param nAme nAme. */
    private PrintStream getOutput(String nAme) {
        try {
            return new PrintStream(new File(nAme));
        } catch (IOException excp) {
            throw error("can't read %s", nAme);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _configuration and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        _allRotors = new ArrayList<Rotor>();
        Machine enigma = readConfig();
        String currLine = _input.nextLine();
        if (currLine == null) {
            throw new EnigmaException("No input");
        } else if (currLine.charAt(0) != '*') {
            throw new EnigmaException(""
                    + "Initial Setting must start with an asterisk");
        } else {
            String prev = "";
            while (_input.hasNextLine()) {
                if (!currLine.equals("")) {
                    if (currLine.charAt(0) == '*') {
                        enigma.startOver();
                        setUp(enigma, currLine.substring(1));
                    } else {
                        String converted = enigma.convert(currLine);
                        char[] chArray = converted.toCharArray();
                        _output.println(chArray);
                    }
                } else {
                    _output.println();
                }
                prev = currLine;
                currLine = _input.nextLine();
            }
        }
        if (currLine.isEmpty()) {
            _output.println();
        }

        String backToNormal = enigma.convert(currLine);

        if (backToNormal == null) {
            return;
        }
        char[] chNormal = backToNormal.toCharArray();
        _output.println(chNormal);
        _output.close();
    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _configuration. */
    private Machine readConfig() {
        try {

            String check = _configuration.next();
            if (check.contains("-")) {
                _alphabet = new CharacterRange(check.charAt(0),
                        check.charAt(2));
            } else {
                _alphabet =  new ExtraCredit(check);
            }
            if (!_configuration.hasNextInt()) {
                throw new EnigmaException("number of rotors not given");
            }
            int numRotsUsed = _configuration.nextInt();
            if (!_configuration.hasNextInt()) {
                throw new EnigmaException("number of pawls not given");
            }
            int movingRotors = _configuration.nextInt();

            position = _configuration.next();
            while (_configuration.hasNext()) {
                _allRotors.add(readRotor());
            }
            _configuration.close();
            return new Machine(_alphabet, numRotsUsed,
                    movingRotors, _allRotors);
        } catch (NoSuchElementException excp) {
            throw error("Cut-off input.");
        }
    }

    /** Return a rotor, reading its description from _configuration. */
    private Rotor readRotor() {
        try {
            String ch = position.toUpperCase();
            String setUp = _configuration.next();
            String result = "";
            String contin = _configuration.next();
            while (contin.charAt(0) == '(' && _configuration.hasNext()) {
                if (contin.charAt(contin.length() - 1) != ')') {
                    throw new EnigmaException("Improper rotor format");
                }
                result += contin;
                contin = _configuration.next();
            }
            if (contin.charAt(0) == '(' && !_configuration.hasNext()) {
                result += contin;
            }
            position = contin;
            Permutation permutation = new Permutation(result, _alphabet);
            if (setUp.charAt(0) == 'M') {
                String notches = setUp.substring(1);
                return new MovingRotor(ch, permutation, notches);
            } else if (setUp.charAt(0) == 'N') {
                return new FixedRotor(ch, permutation);
            } else if (setUp.charAt(0) == 'R') {
                return new Reflector(ch, permutation);
            } else {
                throw new EnigmaException("readRotor: error reading notch");
            }
        } catch (NoSuchElementException excp) {
            throw error("Missing description.");
        }
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {
        Scanner setting = new Scanner(settings);

        String[] insertRotors = new String[M.numRotors()];
        for (int i = 0; i < M.numRotors(); i++) {
            insertRotors[i] = setting.next();
        }

        for (int ind = 0; ind < insertRotors.length; ind++) {
            for (int ind2 = 0; ind2 < insertRotors.length; ind2++) {
                if (ind != ind2 && insertRotors[ind] == insertRotors[ind2]) {
                    throw new EnigmaException("Can't have Repeated Rotor");
                }
            }
        }

        M.insertRotors(insertRotors);
        String initialSetting = setting.next();
        M.setRotors(initialSetting);
        String plugboard = "";
        if (setting.hasNext()) {
            while (setting.hasNext()) {
                plugboard += setting.next();
            }
            M.setPlugboard(new Permutation(plugboard, _alphabet));
        } else {
            M.setPlugboard(new Permutation("(A)", _alphabet));
        }

        if (M.rotorsInUse().get(0).getClass() != Reflector.class) {
            throw new EnigmaException("First Rotor must be a Reflector");
        }
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        for (int i = 0; i < msg.length(); i += 5) {
            int cap = msg.length() - i;
            if (cap <= 6) {
                _output.println(msg.substring(i, i + cap));
            } else {
                _output.print(msg.substring(i, i + 5) + " ");
            }
        }
    }


    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. Just nextLine. */
    private Scanner _configuration;

    /** File for encoded/decoded messages. */
    private PrintStream _output;
    /** General type - Collection - of all given ROTORS. */
    private ArrayList<Rotor> _allRotors;

    /** NAME of curr Rotor. */
    private String name;

    /** Type and NOTCHES of curr Rotor. */
    private String notch;

    /** Tracker of readRotor POSITION. */
    private String position;
}

/**
 read A to Z vs notmal list for reconfig
 make new Alphabet class {
 lower_case handling
 } */
