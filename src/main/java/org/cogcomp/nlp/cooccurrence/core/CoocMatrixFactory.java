package org.cogcomp.nlp.cooccurrence.core;

import org.cogcomp.nlp.cooccurrence.lexicon.IncrementalIndexedLexicon;

import java.io.*;

/**
 * TODO: I plan on providing abstraction for TermDocMatrix and CoocMatrix, this is why this class exists
 */
public class CoocMatrixFactory {

    /**
     * Create an instance of {@Link ImmutableTermTermMatrix} from Term-Document
     * Matrix. The result term cooc matrix is just T-D matrix times its transpose.
     * @param tdmat Term-Document Matrix
     * @return the corresponding Term-Term cooc matrix
     */
    public static ImmutableTermTermMatrix createCoocMatrixFromTermDocMatrix(ImmutableTermDocMatrix tdmat) {
        return new ImmutableTermTermMatrix(tdmat);
    }
    /**
     * Load an instance of {@link ImmutableTermDocMatrix} from previous save.
     *
     * @param dirpath directory that contains the saved matrix data
     * @param name name of the matrix
     * @return An instance of {@link ImmutableTermDocMatrix}.
     * @throws IOException when fails to read save
     */
    public static ImmutableTermDocMatrix createTermDocMatFromSave(String dirpath, String name) throws IOException {
        return new ImmutableTermDocMatrix(dirpath, name);
    }

    /**
     * Load an instance of {@link IncrementalIndexedLexicon} from previous save.
     *
     * @param lexPath path to the saved lexicon
     * @return An loaded instance of {@link IncrementalIndexedLexicon} from previous save
     * @throws IOException when fails to read save
     */
    public static IncrementalIndexedLexicon createIndexedLexiconFromSave(String lexPath) throws IOException {
        return new IncrementalIndexedLexicon(lexPath);
    }
}
