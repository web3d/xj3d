package nodetest;

/**
 * Requirements of a 'test' wrapper for an X3DField.
 */
public interface tX3DField {

    /**
     * Success flag
     */
    boolean SUCCESS = true;

    /**
     * Failure flag
     */
    boolean FAIL = false;

    /**
     * Execute a 'smoke' test.
     *
     * @return results, <code>true</code> for pass, <code>false</code> for fail
     */
    boolean smoke();

    /**
     * Return the field value in an encoded string
     *
     * @param source the identifier of the source of the value to encode
     * @param encode the identifier of encoding scheme
     * @return the field value in an encoded string
     */
    String encode(tValue source, tEncode encode);
}
