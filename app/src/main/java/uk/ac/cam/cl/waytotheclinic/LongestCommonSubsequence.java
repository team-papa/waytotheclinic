package uk.ac.cam.cl.waytotheclinic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LongestCommonSubsequence<T> {
    private List<T> s1;
    private List<T> s2;
    private List<T> lcs;

    public LongestCommonSubsequence(List<T> s1, List<T> s2) {
        this.s1 = s1;
        this.s2 = s2;
    }

    public List<T> getLCS() {
        if (lcs == null) {
            ArrayList<ArrayList<Integer>> dp = new ArrayList<>();

            // fill table up with 0s
            for (T c1 : s1) {
                ArrayList<Integer> newRow = new ArrayList<>();
                for (T c2 : s2) newRow.add(0);
                dp.add(newRow);
            }

            for (int i = 0; i < s1.size(); i++) {
                for (int j = 0; j < s2.size(); j++) {

                    if (s1.get(i).equals(s2.get(j))) {

                        if (i == 0 || j == 0) {
                            dp.get(i).set(j, 1);
                        } else {
                            int prevLen = Math.max(dp.get(i-1).get(j), dp.get(i).get(j-1));
                            dp.get(i).set(j, prevLen + 1);
                        }

                    } else {

                        if (i == 0 || j == 0) {

                            if (j != 0) {
                                dp.get(i).set(j, dp.get(i).get(j-1));
                            } else if (i != 0){
                                dp.get(i).set(j, dp.get(i-1).get(j));
                            }

                        } else {
                            int prevLen = Math.max(dp.get(i - 1).get(j), dp.get(i).get(j - 1));
                            dp.get(i).set(j, prevLen);
                        }
                    }
                }
            }

            // find LCS itself
            lcs = new ArrayList<>();

            int i = s1.size() - 1;
            int j = s2.size() - 1;

            int length = dp.get(i).get(j);

            while (length != 0) {
                // go left until impossible, then go up

                while (j > 0 && dp.get(i).get(j-1) == length) {
                    j--;
                    length = dp.get(i).get(j);
                }

                while (i > 0 && dp.get(i-1).get(j) == length) {
                    i--;
                    length = dp.get(i).get(j);
                }

                // add element to lcs
//                System.out.format("%d %d %s %s\n", i, j, s1.get(i), s2.get(j));
                lcs.add(s1.get(i));
                assert(s1.get(i).equals(s2.get(j)));

                if (i != 0) {
                    i--;
                } else if (j != 0) {
                    j--;
                }
                length= dp.get(i).get(j);

                if (i == 0 && j == 0) break;
            }

        }

        Collections.reverse(lcs);
        return lcs;
    }

    public static void main(String[] args) {
//        List<String> s1 = new ArrayList<String>(
//                Arrays.asList("grfygriusugrygtsrughsirgtiugriushgrsiugsrhrudhsghugrhgughsurglurgsiugrg".split("")));
//        List<String> s2 = new ArrayList<String>(
//                Arrays.asList("hbsgyrsyrsigyhdrsiigurushdiulghilsudrhiulgtrsdhusghlrshguhrusth".split("")));

        List<String> s1 = new ArrayList<String>(
                Arrays.asList("ABCDGH".split("")));
        List<String> s2 = new ArrayList<String>(
                Arrays.asList("AEDFHR".split("")));

        System.out.println(s1);
        System.out.println(s2);

        List<String> lcs = new LongestCommonSubsequence<String>(s1, s2).getLCS();
        System.out.println("Output: " + lcs.toString());
        System.out.println(lcs.size());



    }

}
