package enigma;

import static enigma.EnigmaException.*;

/** Class that represents a rotating rotor in the enigma machine.
 *  @author Anastasia Sukhorebraya
 */
class MovingRotor extends Rotor {

    /** A rotor named NAME whose permutation in its default setting is
     *  PERM, and whose notches are at the positions indicated in NOTCHES.
     *  The Rotor is initally in its 0 setting (first character of its
     *  alphabet).
     */
    MovingRotor(String name, Permutation perm, String notches) {
        super(name, perm);
        _notches = notches;

    }


    /** Returns true iff I am positioned to allow the rotor to my left
     *  to advance. */
    @Override
    boolean atNotch() {

        for (char notch: _notches.toCharArray()) {
            if (position() == permutation().alphabet().toInt(notch)) {
                return true;
            }
        }
        return false;
    }

    @Override
    boolean rotates() {
        return true;
    }

    @Override
    void advance() {
        int next = position() + 1;
        set(permutation().wrap(next));
    }

    /** ADDITIONAL FIELDS HERE, AS NEEDED. */
    private String _notches;
}
