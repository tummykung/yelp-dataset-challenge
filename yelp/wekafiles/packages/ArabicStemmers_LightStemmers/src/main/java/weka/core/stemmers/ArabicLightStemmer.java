/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package weka.core.stemmers;

import org.apache.lucene.analysis.ar.ArabicNormalizer;
import org.apache.lucene.analysis.ar.ArabicStemmer;
import weka.core.RevisionUtils;

/**
 *
 * @author Motaz K. Saad, email: motaz.saad@gmail.com
 */
public class ArabicLightStemmer  implements Stemmer{

    public String stem(String word) {

        ArabicNormalizer arabicNorm = new ArabicNormalizer();
        char[] c = word.toCharArray();
        int len = c.length;
        len = arabicNorm.normalize(c, len);
        char[] normalizedWord = new char[len];
        for (int i = 0; i < len; i++) {
            normalizedWord[i] = c[i];
        }



        ArabicStemmer araLightStemmer = new ArabicStemmer();
        len = araLightStemmer.stem(normalizedWord, len);
        char[] lightWord = new char[len];

        for (int i = 0; i < len; i++) {
            lightWord[i] = normalizedWord[i];
        }

        StringBuffer sbuf = new StringBuffer();
        sbuf.append(lightWord);
       

        return sbuf.toString();
    }

    public String getRevision() {
        return RevisionUtils.extract("$Revision: 1.3 $");
    }


        /**
   * Runs the stemmer with the given options
   *
   * @param args      the options
   */
  public static void main(String[] args) {

    try {
      Stemming.useStemmer(new ArabicLightStemmer(), args);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

}
