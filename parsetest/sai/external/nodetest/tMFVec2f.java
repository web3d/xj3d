package nodetest;

import org.web3d.x3d.sai.MFVec2f;
import org.web3d.x3d.sai.X3DFieldDefinition;
import org.web3d.x3d.sai.X3DFieldTypes;
import org.web3d.x3d.sai.X3DNode;

/**
 * Test wrapper for the MFVec2f X3DField type
 */
public class tMFVec2f implements tX3DField {

    /**
     * Default smoke test write value
     */
    public final static float[] smoke_value = new float[]{0.1f, 0.1f, 0.9f, 0.9f};

    /**
     * The field
     */
    final MFVec2f field;

    /**
     * The field name
     */
    final String fieldName;

    /**
     * The field access type
     */
    final int access;

    /**
     * The node name
     */
    final String nodeName;

    /**
     * The test controller
     */
    final tController control;

    /**
     * Constructor
     *
     * @param node the <code>X3DNode</code> this field belongs to
     * @param def the <code>X3DFieldDefinition</code> of the field
     * @param control
     */
    public tMFVec2f(final X3DNode node, final X3DFieldDefinition def, final tController control) {
        this.nodeName = node.getNodeName();
        this.fieldName = def.getName();
        this.field = (MFVec2f) node.getField(fieldName);
        this.access = def.getAccessType();
        this.control = control;
    }

    /**
     * Execute a 'smoke' test.
     *
     * @return results, <code>true</code> for pass, <code>false</code> for fail
     */
    @Override
    public boolean smoke() {
        float[] r_value;
        final float[] w_value = smoke_value;
        final int w_size = w_value.length;
        switch (access) {
            case X3DFieldTypes.INPUT_ONLY:
            case X3DFieldTypes.INITIALIZE_ONLY:
                field.setValue(w_size / 2, w_value);
                break;
            case X3DFieldTypes.OUTPUT_ONLY:
                r_value = new float[field.getSize() * 2];
                field.getValue(r_value);
                break;
            case X3DFieldTypes.INPUT_OUTPUT:
                //
                control.bufferUpdate();
                field.setValue(w_size / 2, w_value);
                control.flushUpdate();
                //
                final int r_size = field.getSize();
                if (r_size != w_size) {
                    control.logMessage(tMessageType.ERROR, new String[]{
                        nodeName + ":" + fieldName,
                        "\twrite size = " + w_size,
                        "\tread size  = " + r_size});
                    return (FAIL);
                }
                r_value = new float[r_size * 2];
                field.getValue(r_value);
                for (int i = 0; i < r_value.length; i++) {
                    if (w_value[i] != r_value[i]) {
                        control.logMessage(tMessageType.ERROR, new String[]{
                            nodeName + ":" + fieldName,
                            "\tdata mismatch at array index: " + i,
                            "\twrote [ " + w_value[i] + " ]",
                            "\tread  [ " + r_value[i] + " ]"});
                        return (FAIL);
                    }
                }
                break;
            default:
                control.logMessage(tMessageType.ERROR, nodeName + ":" + fieldName + " invalid access type: " + access);
                return (FAIL);
        }
        control.logMessage(tMessageType.SUCCESS, nodeName + ":" + fieldName);
        return (SUCCESS);
    }

    /**
     * Return the field value in an encoded string
     *
     * @param source the identifier of the source of the value to encode
     * @param encode the identifier of encoding scheme
     * @return the field value in an encoded string
     */
    @Override
    public String encode(final tValue source, final tEncode encode) {
        float[] r_value = null;
        if (source == tValue.FIELD) {
            final int r_size = field.getSize();
            r_value = new float[2 * r_size];
            field.getValue(r_value);
        } else if (source == tValue.SMOKE) {
            r_value = smoke_value;
        }
        if (encode == tEncode.XML) {
            return (tEncodingUtils.encodeXML(fieldName, r_value));
        } else if (encode == tEncode.CLASSIC) {
            return (tEncodingUtils.encodeClassic(fieldName, r_value));
        } else {
            return (null);
        }
    }
}