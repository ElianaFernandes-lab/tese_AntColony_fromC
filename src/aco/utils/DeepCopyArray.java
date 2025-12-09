package aco.utils;

import java.lang.reflect.Array;


public class DeepCopyArray {

    /**
     * Deep copies an array of any dimension and any element type.
     * Supports primitive arrays and object arrays.
     */
    @SuppressWarnings("unchecked")
    public static <T> T deepCopy(T array) {
        if (array == null) return null;

        Class<?> type = array.getClass();

        if (!type.isArray()) {
            // Base case: not an array → return element directly
            return array;
        }

        int length = Array.getLength(array);
        Class<?> componentType = type.getComponentType();

        // Create a new array of same type and length
        T copy = (T) Array.newInstance(componentType, length);

        // Recursively copy elements
        for (int i = 0; i < length; i++) {
            Object element = Array.get(array, i);
            Object copiedElement = deepCopy(element);
            Array.set(copy, i, copiedElement);
        }

        return copy;
    }
}

