package enigma;

import static enigma.EnigmaException.error;

/** An alphabet of encodable characters.  Provides a mapping from characters
 *  to and from indices into the alphabet.
 *  @author P. N. Hilfinger
 */
class ExtraCredit extends Alphabet {

    /** An alphabet consisting of all characters between FIRST and LAST,
     *  inclusive. @param a a. */
    ExtraCredit(String a) {
        _a = a;
        if (a.isEmpty()) {
            throw error("empty range of characters");
        }
    }

    @Override
    int size() {
        return _a.length();
    }

    @Override
    boolean contains(char ch) {
        return _a.indexOf(ch) == -1;
    }

    @Override
    char toChar(int index) {
        return _a.charAt(index);
    }

    @Override
    int toInt(char ch) {
        return _a.indexOf(ch);
    }

    /** Range of characters in this Alphabet. */
    private String _a;

}

