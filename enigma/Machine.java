package enigma;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Scanner;

import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author Anastasia Sukhorebraya
 */
class Machine {

    /** ALPHABET shared by ALL rotors. */
    private final Alphabet _alphabet;
    /** Number of ROTORS int. */
    private int _numRotors;
    /** Number of PAWLS int. */
    private int _numPawls;
    /** A HashMap of ALL rotors. */
    private HashMap<String, Object> mapRotorObjects;
    /**List to keep track of all rotors being used. */
    private ArrayList<Rotor> _rotorsInUse;
    /**Permutation to store the plugboard. */
    private Permutation _plugboard;


    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available _rotorsInUse. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        if (pawls >= numRotors) {
            throw new EnigmaException("Can't have PAWLS >= ROTORS.");
        }

        mapRotorObjects = new HashMap<String, Object>();
        _rotorsInUse = new ArrayList<Rotor>();
        _rotorsInUse = new ArrayList<Rotor>();


        _alphabet = alpha;
        _numRotors = numRotors;
        _numPawls = pawls;

        for (Rotor r : allRotors) {
            mapRotorObjects.put(r.name(), r);
        }
    }

    /** Getter for @return Rotor objects in use. */
    public ArrayList rotorsInUse() {
        return _rotorsInUse;
    }

    /** Getter for @return _numRotors _numRotors. */
    int numRotors() {
        return _numRotors;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _numPawls;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        if (_rotorsInUse.size() > numRotors()) {
            throw new EnigmaException("Too many rotors to insert");
        }
        for (int i = 0; i < rotors.length; i++) {
            String rotor = rotors[i];

            Rotor setup = (Rotor) mapRotorObjects.get(rotor);
            if (setup == null) {
                throw new EnigmaException("Rotor name doesn't exist.");
            }
            _rotorsInUse.add(setup);
        }
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 upper-case letters. The first letter refers to the
     *  leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        char[] initialPosition = setting.toCharArray();

        for (int i = 0; i < initialPosition.length; i++) {
            if (_rotorsInUse.size() <= i + 1) {
                break;
            }
            Rotor current = (Rotor) _rotorsInUse.get(i + 1);
            int next = i + 1;
            int staticRotors = numRotors() - _numPawls;

            if (current.rotates()) {
                if (staticRotors > next) {
                    throw new EnigmaException("Can't move NonMoving Rotor.");
                }
            } else {
                if (next != 0 && current.reflecting()) {
                    throw new EnigmaException(""
                            + "Only the 0th rotor can be a reflector.");
                } else if (staticRotors < i) {
                    throw new EnigmaException("N rotors must come after M's.");
                }
            }
            current.set(initialPosition[i]);
        }
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {

        rotate();
        int result = _plugboard.permute(c);

        for (int i = _rotorsInUse.size() - 1; i >= 0; i -= 1) {
            Rotor currPerm = (Rotor) _rotorsInUse.get(i);
            result = currPerm.convertForward(result);

        }
        for (int i2 = 1; i2 < _rotorsInUse.size(); i2++) {
            Rotor currInv = (Rotor) _rotorsInUse.get(i2);
            result = currInv.convertBackward(result);

        }
        result = _plugboard.invert(result);
        return result;
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {

        String input = "";
        Scanner spaceOmitted = new Scanner(msg);
        String prev = "";
        while (spaceOmitted.hasNext()) {
            prev = spaceOmitted.next();
            input += prev;
        }

        char[] upperArray = input.toUpperCase().toCharArray();
        if (upperArray.length == 0) {
            return null;
        } else if (upperArray[0] == '*') {
            return null;
        }

        int len = upperArray.length + upperArray.length / 5;
        char[] outArray = new char[len];

        for (int iOut = 0, iMsg = 0, i = 0; iOut < outArray.length; iOut++) {
            if (i == 0 || i % 5 != 0) {
                Alphabet a = _alphabet;
                outArray[iOut] = a.toChar(convert(a.toInt(upperArray[iMsg])));
                iMsg += 1;
                i += 1;
            } else {
                outArray[iOut] = ' ';
                i = 0;
            }

        }
        String output = new String(outArray);
        if (outArray[len - 1] == ' ') {
            output = output.substring(0, len - 1);
        }

        return output;
    }

    /** Makes rotors go forward. */
    public void rotate() {
        int currPawl = _numPawls;
        int size = _rotorsInUse.size();
        int r = size - 1;
        Rotor right = _rotorsInUse.get(r);
        r -= 1;
        Rotor left = _rotorsInUse.get(r);
        boolean rightAtNotch = left.atNotch();
        boolean meMove = right.atNotch();
        boolean clas = left.getClass().equals(MovingRotor.class);
        boolean edge = false;


        right.advance();
        currPawl -= 1;
        int reflectorPawl = 0;


        if (rightAtNotch) {
            edge = size == 2 && _numPawls == 2;
            if (currPawl >= 2) {
                left.advance();
                while (currPawl != reflectorPawl && rightAtNotch) {
                    r -= 1;
                    currPawl -= 1;
                    left = _rotorsInUse.get(r);
                    rightAtNotch = left.atNotch();
                    left.advance();
                }
            } else if (edge) {
                left.advance();
            }
        } else if (!rightAtNotch && meMove) {
            left.advance();
        }
    }

    /** startOvers rotors. */
    void startOver() {
        _rotorsInUse = new ArrayList<Rotor>();
    }
}

