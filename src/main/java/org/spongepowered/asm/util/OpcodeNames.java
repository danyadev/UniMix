package org.spongepowered.asm.util;

import org.objectweb.asm.Opcodes;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class OpcodeNames {

    private static final IntConstant[] ALL_INT_CONSTANTS = loadIntConstants();
    private static final Map<String, FieldMappings> MAPPINGS_BY_START = new HashMap<>();

    private OpcodeNames() {}

    public static String getOpcodeName(int opcode, String startFrom, int min) {
        if (opcode >= min) {
            String name = getLookup(startFrom).valueToName.get(opcode);
            if (name != null) {
                return name;
            }
        }

        return opcode >= 0 ? Integer.toString(opcode) : "UNKNOWN";
    }

    public static int parseOpcodeName(String opcodeName, String startFrom) {
        if (opcodeName == null) {
            return -1;
        }

        Integer opcode = getLookup(startFrom).nameToValue.get(opcodeName.toUpperCase(Locale.ROOT));
        return opcode != null ? opcode : -1;
    }

    private static IntConstant[] loadIntConstants() {
        Field[] fields = Opcodes.class.getDeclaredFields();
        List<IntConstant> result = new ArrayList<>(fields.length);

        for (Field field : fields) {
            if (field.getType() != Integer.TYPE) {
                continue;
            }

            try {
                result.add(new IntConstant(field.getName(), field.getInt(null)));
            } catch (Exception ignored) {
                // public interface constants, so this should not happen
            }
        }

        return result.toArray(new IntConstant[0]);
    }

    /**
     * @param startFrom The name of the field in {@code Opcodes} from which name/value collection should begin
     * @return {@link FieldMappings} which contains nameToValue and valueToName maps for given {@code startFrom}
     */
    private static FieldMappings getLookup(String startFrom) {
        FieldMappings cached = MAPPINGS_BY_START.get(startFrom);
        if (cached != null) {
            return cached;
        }

        boolean reachedStartField = false;
        Map<String, Integer> nameToValue = new HashMap<>();
        Map<Integer, String> valueToName = new HashMap<>();

        for (IntConstant constant : ALL_INT_CONSTANTS) {
            if (!reachedStartField) {
                if (!constant.name.equals(startFrom)) {
                    continue;
                }
                reachedStartField = true;
            }

            nameToValue.putIfAbsent(constant.name.toUpperCase(Locale.ROOT), constant.value);
            valueToName.putIfAbsent(constant.value, constant.name);
        }

        FieldMappings mappings = new FieldMappings(nameToValue, valueToName);
        MAPPINGS_BY_START.put(startFrom, mappings);
        return mappings;
    }

    private static final class IntConstant {
        final String name;
        final int value;

        IntConstant(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }

    private static final class FieldMappings {
        final Map<String, Integer> nameToValue;
        final Map<Integer, String> valueToName;

        FieldMappings(Map<String, Integer> nameToValue, Map<Integer, String> valueToName) {
            this.nameToValue = nameToValue;
            this.valueToName = valueToName;
        }
    }
}
