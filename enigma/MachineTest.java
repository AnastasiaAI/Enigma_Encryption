package enigma;

import org.junit.Test;
import static enigma.TestUtils.*;
import java.util.ArrayList;
import java.util.Arrays;
import static org.junit.Assert.*;



public class MachineTest {

    @Test
    public void testDoubleStep() {
        Alphabet ac = new CharacterRange('A', 'D');
        Rotor one = new Reflector("R1", new Permutation("(AC) (BD)", ac));
        Rotor two = new MovingRotor("R2", new Permutation("(ABCD)", ac), "C");
        Rotor three = new MovingRotor("R3", new Permutation("(ABCD)", ac), "C");
        Rotor four = new MovingRotor("R4", new Permutation("(ABCD)", ac), "C");
        String setting = "AAA";
        Rotor[] machineRotors = {one, two, three, four};
        String[] rotors = {"R1", "R2", "R3", "R4"};
        Machine mach = new Machine(ac, 4, 3,
                new ArrayList<>(Arrays.asList(machineRotors)));
        mach.insertRotors(rotors);
        mach.setRotors(setting);
        ArrayList<String> expected = new ArrayList<String>(20);
        expected.add("AAAA");
        expected.add("AAAB");
        expected.add("AAAC");
        expected.add("AABD");
        expected.add("AABA");
        expected.add("AABB");
        expected.add("AABC");
        expected.add("AACD");
        expected.add("ABDA");
        expected.add("ABDB");
        expected.add("ABDC");
        expected.add("ABAD");
        expected.add("ABAA");
        expected.add("ABAB");
        expected.add("ABAC");
        expected.add("ABBD");
        expected.add("ABBA");
        expected.add("ABBB");
        expected.add("ABBC");
        expected.add("ABCD");
        expected.add("ACDA");
        expected.add("ACDB");
        expected.add("ACDC");
        expected.add("ACAD");
        expected.add("ACAA");
        expected.add("ACAB");
        expected.add("ACAC");
        expected.add("ACBD");
        expected.add("ACBA");
        expected.add("ACBB");
        expected.add("ACBC");
        expected.add("ACCD");
        expected.add("ADDA");
        expected.add("ADDB");
        expected.add("ADDC");
        expected.add("ADAD");
        expected.add("ADAA");


        for (String check: expected) {

            assertEquals(check, getSetting(ac, machineRotors));
            mach.rotate();
        }

    }

    /** Helper method to get the String representation
     * of the current Rotor settings */
    private String getSetting(Alphabet alph, Rotor[] machineRotors) {
        String currSetting = "";
        for (Rotor r : machineRotors) {
            currSetting += alph.toChar(r.setting());
        }
        return currSetting;
    }
}

